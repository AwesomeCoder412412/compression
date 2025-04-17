// SPDX-License-Identifier: 0BSD
// SPDX-FileCopyrightText: The XZ for Java authors and contributors
// SPDX-FileContributor: Lasse Collin <lasse.collin@tukaani.org>
// SPDX-FileContributor: Igor Pavlov <https://7-zip.org/>



import XZ.ArrayCache;

// Binary Tree match finder with 2-, 3-, and 4-byte hashing
final class MyBT4 {

    public static final int MF_HC4 = 0x04;
    public static final int MF_BT4 = 0x14;

    /**
     * Number of bytes to keep available before the current byte
     * when moving the LZ window.
     */
    private final int keepSizeBefore;

    /**
     * Number of bytes that must be available, the current byte included,
     * to make hasEnoughData return true. Flushing and finishing are
     * naturally exceptions to this since there cannot be any data after
     * the end of the uncompressed input.
     */
    private final int keepSizeAfter;

    final int matchLenMax;
    final int niceLen;

    final byte[] buf;
    final int bufSize; // To avoid buf.length with an array-cached buf.

    int readPos = -1;
    private int readLimit = -1;
    private final boolean finishing = false;
    private int writePos = 0;
    private int pendingSize = 0;


    private final Hash234 hash;
    private final int[] tree;
    private final Matches matches;
    private final int depthLimit;

    private final int cyclicSize;
    private int cyclicPos = -1;
    private int lzPos;

    static int getMemoryUsage(int dictSize) {
        return Hash234.getMemoryUsage(dictSize) + dictSize / (1024 / 8) + 10;
    }

    MyBT4(int dictSize, int beforeSizeMin, int readAheadMax,
          int niceLen, int matchLenMax, int depthLimit,
          ArrayCache arrayCache) {

        int extraSizeBefore = beforeSizeMin;
        int extraSizeAfter = readAheadMax;

        bufSize = getBufSize(dictSize, extraSizeBefore, extraSizeAfter,
                matchLenMax);

        // MatchLength.getLen might read and ignore extra bytes
        // at the end of the buffer.
        buf = arrayCache.getByteArray(bufSize + MatchLength.EXTRA_SIZE, false);


        keepSizeBefore = extraSizeBefore + dictSize;
        keepSizeAfter = extraSizeAfter + matchLenMax;

        this.matchLenMax = matchLenMax;
        this.niceLen = niceLen;

        cyclicSize = dictSize + 1;
        lzPos = cyclicSize;

        hash = new Hash234(dictSize, arrayCache);
        tree = arrayCache.getIntArray(cyclicSize * 2, false);

        // Subtracting 1 because the shortest match that this match
        // finder can find is 2 bytes, so there's no need to reserve
        // space for one-byte matches.
        matches = new Matches(niceLen - 1);

        this.depthLimit = depthLimit > 0 ? depthLimit : 16 + niceLen / 2;
    }


    public void putArraysToCache(ArrayCache arrayCache) {
        arrayCache.putArray(tree);
        hash.putArraysToCache(arrayCache);
        arrayCache.putArray(buf);
    }

    private int movePos() {
        int avail = movePos(niceLen, 4);

        if (avail != 0) {
            if (++lzPos == Integer.MAX_VALUE) {
                int normalizationOffset = Integer.MAX_VALUE - cyclicSize;
                hash.normalize(normalizationOffset);
                normalize(tree, cyclicSize * 2, normalizationOffset);
                lzPos -= normalizationOffset;
            }

            if (++cyclicPos == cyclicSize)
                cyclicPos = 0;
        }

        return avail;
    }

    /**
     * Moves to the next byte, checks if there is enough input available,
     * and returns the amount of input available.
     *
     * @param       requiredForFlushing
     *                          minimum number of available bytes when
     *                          flushing; encoding may be continued with
     *                          new input after flushing
     * @param       requiredForFinishing
     *                          minimum number of available bytes when
     *                          finishing; encoding must not be continued
     *                          after finishing or the match finder state
     *                          may be corrupt
     *
     * @return      the number of bytes available or zero if there
     *              is not enough input available
     */
    int movePos(int requiredForFlushing, int requiredForFinishing) {
        assert requiredForFlushing >= requiredForFinishing;

        ++readPos;
        int avail = writePos - readPos;

        if (avail < requiredForFlushing) {
            if (avail < requiredForFinishing || !finishing) {
                ++pendingSize;
                avail = 0;
            }
        }

        return avail;
    }

    public Matches getMatches() {
        //stupid
        cyclicPos++;

        readPos++;

        writePos = 8192;

        readLimit = 3823;

        // end stupid

        matches.count = 0;

        int matchLenLimit = matchLenMax;
        int niceLenLimit = niceLen;
        int avail = movePos();

        if (avail < matchLenLimit) {
            if (avail == 0)
                return matches;

            matchLenLimit = avail;
            if (niceLenLimit > avail)
                niceLenLimit = avail;
        }

        hash.calcHashes(buf, readPos);
        int delta2 = lzPos - hash.getHash2Pos();
        int delta3 = lzPos - hash.getHash3Pos();
        int currentMatch = hash.getHash4Pos();
        hash.updateTables(lzPos);

        int lenBest = 0;

        // See if the hash from the first two bytes found a match.
        // The hashing algorithm guarantees that if the first byte
        // matches, also the second byte does, so there's no need to
        // test the second byte.
        if (delta2 < cyclicSize && buf[readPos - delta2] == buf[readPos]) {
            lenBest = 2;
            matches.len[0] = 2;
            matches.dist[0] = delta2 - 1;
            matches.count = 1;
        }

        // See if the hash from the first three bytes found a match that
        // is different from the match possibly found by the two-byte hash.
        // Also here the hashing algorithm guarantees that if the first byte
        // matches, also the next two bytes do.
        if (delta2 != delta3 && delta3 < cyclicSize
                && buf[readPos - delta3] == buf[readPos]) {
            lenBest = 3;
            matches.dist[matches.count++] = delta3 - 1;
            delta2 = delta3;
        }

        // If a match was found, see how long it is.
        if (matches.count > 0) {
            lenBest = MatchLength.getLen(buf, readPos, delta2,
                    lenBest, matchLenLimit);
            matches.len[matches.count - 1] = lenBest;

            // Return if it is long enough (niceLen or reached the end of
            // the dictionary).
            if (lenBest >= niceLenLimit) {
                skip(niceLenLimit, currentMatch);
                return matches;
            }
        }

        // Long enough match wasn't found so easily. Look for better matches
        // from the binary tree.
        if (lenBest < 3)
            lenBest = 3;

        int depth = depthLimit;

        int ptr0 = (cyclicPos << 1) + 1;
        int ptr1 = cyclicPos << 1;
        int len0 = 0;
        int len1 = 0;

        while (true) {
            int delta = lzPos - currentMatch;

            // Return if the search depth limit has been reached or
            // if the distance of the potential match exceeds the
            // dictionary size.
            if (depth-- == 0 || delta >= cyclicSize) {
                tree[ptr0] = 0;
                tree[ptr1] = 0;
                return matches;
            }

            int pair = (cyclicPos - delta
                    + (delta > cyclicPos ? cyclicSize : 0)) << 1;
            int len = Math.min(len0, len1);

            if (buf[readPos + len - delta] == buf[readPos + len]) {
                len = MatchLength.getLen(buf, readPos, delta,
                        len + 1, matchLenLimit);

                if (len > lenBest) {
                    lenBest = len;
                    matches.len[matches.count] = len;
                    matches.dist[matches.count] = delta - 1;
                    ++matches.count;

                    if (len >= niceLenLimit) {
                        tree[ptr1] = tree[pair];
                        tree[ptr0] = tree[pair + 1];
                        return matches;
                    }
                }
            }

            if ((buf[readPos + len - delta] & 0xFF)
                    < (buf[readPos + len] & 0xFF)) {
                tree[ptr1] = currentMatch;
                ptr1 = pair + 1;
                currentMatch = tree[ptr1];
                len1 = len;
            } else {
                tree[ptr0] = currentMatch;
                ptr0 = pair;
                currentMatch = tree[ptr0];
                len0 = len;
            }
        }
    }

    private void skip(int niceLenLimit, int currentMatch) {
        int depth = depthLimit;

        int ptr0 = (cyclicPos << 1) + 1;
        int ptr1 = cyclicPos << 1;
        int len0 = 0;
        int len1 = 0;

        while (true) {
            int delta = lzPos - currentMatch;

            if (depth-- == 0 || delta >= cyclicSize) {
                tree[ptr0] = 0;
                tree[ptr1] = 0;
                return;
            }

            int pair = (cyclicPos - delta
                    + (delta > cyclicPos ? cyclicSize : 0)) << 1;
            int len = Math.min(len0, len1);

            if (buf[readPos + len - delta] == buf[readPos + len]) {
                // No need to look for longer matches than niceLenLimit
                // because we only are updating the tree, not returning
                // matches found to the caller.
                len = MatchLength.getLen(buf, readPos, delta,
                        len + 1, niceLenLimit);
                if (len == niceLenLimit) {
                    tree[ptr1] = tree[pair];
                    tree[ptr0] = tree[pair + 1];
                    return;
                }
            }

            if ((buf[readPos + len - delta] & 0xFF)
                    < (buf[readPos + len] & 0xFF)) {
                tree[ptr1] = currentMatch;
                ptr1 = pair + 1;
                currentMatch = tree[ptr1];
                len1 = len;
            } else {
                tree[ptr0] = currentMatch;
                ptr0 = pair;
                currentMatch = tree[ptr0];
                len0 = len;
            }
        }
    }

    public void skip(int len) {
        while (len-- > 0) {
            int niceLenLimit = niceLen;
            int avail = movePos();

            if (avail < niceLenLimit) {
                if (avail == 0)
                    continue;

                niceLenLimit = avail;
            }

            hash.calcHashes(buf, readPos);
            int currentMatch = hash.getHash4Pos();
            hash.updateTables(lzPos);

            skip(niceLenLimit, currentMatch);
        }
    }

    /**
     * Gets the size of the LZ window buffer that needs to be allocated.
     */
    private static int getBufSize(
            int dictSize, int extraSizeBefore, int extraSizeAfter,
            int matchLenMax) {
        int keepSizeBefore = extraSizeBefore + dictSize;
        int keepSizeAfter = extraSizeAfter + matchLenMax;
        int reserveSize = Math.min(dictSize / 2 + (256 << 10), 512 << 20);
        return keepSizeBefore + keepSizeAfter + reserveSize;
    }



    static void normalize(int[] positions, int positionsCount,
                          int normalizationOffset) {
        for (int i = 0; i < positionsCount; ++i) {
            if (positions[i] <= normalizationOffset)
                positions[i] = 0;
            else
                positions[i] -= normalizationOffset;
        }
    }

    /**
     * Copies new data into the LZEncoder's buffer.
     */
    public int fillWindow(byte[] in, int off, int len) {
        assert !finishing;

        // Move the sliding window if needed.
        if (readPos >= bufSize - keepSizeAfter)
            moveWindow();

        // Try to fill the dictionary buffer. If it becomes full,
        // some of the input bytes may be left unused.
        if (len > bufSize - writePos)
            len = bufSize - writePos;

        System.arraycopy(in, off, buf, writePos, len);
        writePos += len;

        // Set the new readLimit but only if there's enough data to allow
        // encoding of at least one more byte.
        if (writePos >= keepSizeAfter)
            readLimit = writePos - keepSizeAfter;

        processPendingBytes();

        // Tell the caller how much input we actually copied into
        // the dictionary.
        return len;
    }

    /**
     * Process pending bytes remaining from preset dictionary initialization
     * or encoder flush operation.
     */
    private void processPendingBytes() {
        // After flushing or setting a preset dictionary there will be
        // pending data that hasn't been ran through the match finder yet.
        // Run it through the match finder now if there is enough new data
        // available (readPos < readLimit) that the encoder may encode at
        // least one more input byte. This way we don't waste any time
        // looping in the match finder (and marking the same bytes as
        // pending again) if the application provides very little new data
        // per write call.
        if (pendingSize > 0 && readPos < readLimit) {
            readPos -= pendingSize;
            int oldPendingSize = pendingSize;
            pendingSize = 0;
            skip(oldPendingSize);
            assert pendingSize < oldPendingSize;
        }
    }

    /**
     * Moves data from the end of the buffer to the beginning, discarding
     * old data and making space for new input.
     */
    private void moveWindow() {
        // Align the move to a multiple of 16 bytes. LZMA2 needs this
        // because it uses the lowest bits from readPos to get the
        // alignment of the uncompressed data.
        int moveOffset = (readPos + 1 - keepSizeBefore) & ~15;
        int moveSize = writePos - moveOffset;
        System.arraycopy(buf, moveOffset, buf, 0, moveSize);

        readPos -= moveOffset;
        readLimit -= moveOffset;
        writePos -= moveOffset;
    }

}

final class MatchLength {
    static final int EXTRA_SIZE = 0;

    static int getLen(byte[] buf, int off, int delta, int len, int lenLimit) {
        assert off >= 0;
        assert delta > 0;
        assert len >= 0;
        assert lenLimit >= len;

        lenLimit += off;
        int i = off + len;

        while (i < lenLimit && buf[i] == buf[i - delta])
            ++i;

        return i - off;
    }
}

final class Matches {
    public final int[] len;
    public final int[] dist;
    public int count = 0;

    Matches(int countMax) {
        len = new int[countMax];
        dist = new int[countMax];
    }

    public String toString() {
        return "len: " + len + ", dist: " + dist + ", count: " + count;
    }
}