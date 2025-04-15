import XZ.*;

import java.io.*;

import XZ.lz.*;
import org.apache.commons.lang3.StringUtils;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        //implementationTesting(args);
        //XZTesting(args);
        //LifeIsPain(args);
        String megalovania = "megalovaniamidi.mid";
        String testfile = "simple.txt";
        String random = "RandomNumbers";
        String randomMax = "MaxRandomNumbers";
        String random500 = "500RandomNumbers";
        String tankBattle = "tankbattle.zip";
        String tankBattle2 = "tankbattle.7z";
        String readme = "readme.txt";
        String loremipsum = "loremipsum.txt";
        String loremipsumzip = "loremipsum.zip";
        String loremipsum7z = "loremipsum.7z";
        String tankfolder = "tankfolder";
        String spamcdmid = "spamcd.mid";
        String holygrail = "megalovania.pcm";
        String coffinnails = "coffinnails.pcm";
        String spamcd = "spamcd.pcm";

        String currFile = "stupid.midi";

        //PCMParser p = new PCMParser("/Users/jacksegil/Desktop/compression/testfiles/" + "testsong2.pcm");

        //System.out.println(Arrays.toString(l.getData("[00:02.69]", "[00:04.09]")));
        //ArrayList<int[]> weird = l.readPCM("/Users/jacksegil/Desktop/compression/testfiles/rockafellerdupe.pcm", 24, 2);
        //System.out.println(Arrays.toString(LongestCommonPattern(l.lyrics.get(0).getPcmData().getFirst(), l.lyrics.get(2).getPcmData().getFirst())));
/*
        PCMParser pp = new PCMParser("/Users/jacksegil/Downloads/misery1.pcm", "/Users/jacksegil/Downloads/misery2.pcm");
        try {
            pp.calculateDiffSavings();
            //System.out.println(PCMParser.countUsedBits(10) + " " + PCMParser.countUsedBits2(-1));
        } catch (Exception e) {
            e.printStackTrace();
        }*/


        //System.out.println();
        //p.printMaximaMinima();
        // p.lastResort();
        //p.printStuff();
        //p.printTrueMaxes();
        //p.findTrueMaxesWithBias(p.theStuff);
        //System.out.println(p.trueNumZeros());
        //p.findTrueMaxesWithBiasAverage(p.theStuff);
        //System.out.println(p.calculateStandardDeviationOfMaxes());


        try {
            // LRCParser l = new LRCParser("/Users/jacksegil/Desktop/compression/testfiles/rockafeller.raw","/Users/jacksegil/Desktop/compression/testfiles/lyrics/lrc/rockafeller.lrc", 32);
            //l.compressToALS();
            CommonSubpatternDevelopment("/Users/jacksegil/Desktop/compression/testfiles/" + currFile, false, false, 1);
           /* Decoder decoder = new Decoder("/Users/jacksegil/Desktop/compression/testfiles/" + "oneday" + "");
            decoder.Decode();


            int currValue = ThreadManager.threadCounter.get();
            while (currValue > 0) {
                int temp = ThreadManager.threadCounter.get();
                if (temp != currValue) {
                    System.out.println("We have " + currValue + " threads left");
                }
                currValue = temp;
            }

            ConcurrentHashMap<String, ArrayList<int[]>> dd = decoder.map;
            System.out.println("Done!"); */
        }
        catch(Exception e){
            e.printStackTrace();
        }

        int currValue = ThreadManager.threadCounter.get();
        while (currValue > 0) {
            int temp = ThreadManager.threadCounter.get();
            if (temp != currValue) {
                System.out.println("We have " + currValue + " threads left");
            }
            currValue = temp;
        }

    }
    public static void implementationTesting(String[] args) {

        try {
            File fileData = new File(args[0]);
            FileInputStream inFile = new FileInputStream(args[0]);
            //FileOutputStream outfile = new FileOutputStream(args[0] + ".xz");

            //ArrayCache arrayCache = ArrayCache.getDefaultCache();

            System.out.println(Runtime.getRuntime().maxMemory());
            ArrayCache arrayCache = ArrayCache.getDefaultCache();

            MyBT4 myBT4 = new MyBT4(805306368, 4096, 4096, 64, 273, Integer.MAX_VALUE, arrayCache);

            BT4 bt4 = new BT4(805306368, 4096, 4096, 64, 273, Integer.MAX_VALUE, arrayCache);


            byte[] killMe = inFile.readAllBytes();

            int size = killMe.length;

            //killMe =

            bt4.fillWindow(killMe, 0, size);


            System.out.println(bt4.getMatches().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void XZTesting(String[] args) {
        try {


            File fileData = new File(args[0]);
            FileInputStream inFile = new FileInputStream(args[0]);
            FileOutputStream outfile = new FileOutputStream(args[0] + ".xz");

            LZMA2Options options = new LZMA2Options();


            //options.setPreset(9); // play with this number: 6 is default but 7 works better for mid sized archives ( > 8mb)
            //options.setDepthLimit((int) (fileData.length() / 2));
            //System.out.println((int) (fileData.length() / 2));
            options.setDepthLimit(Integer.MAX_VALUE);
            options.setNiceLen(273);
            options.setDictSize(805306368);
            //options.setNiceLen(8);

            XZOutputStream out = new XZOutputStream(outfile, options);


            boolean sizeCap = false;
            if (sizeCap) {

                int bufferSize = 8192;
                byte[] buf = new byte[bufferSize];
                int size;
                int buffersRead = 0;
                while ((size = inFile.read(buf)) != -1) {
                    out.write(buf, 0, size);
                    buffersRead++;
                    System.out.println("Currently at " + 100 * ((double) (buffersRead * bufferSize) / fileData.length()) + " percent");
                }

                out.finish();
            } else {
                byte[] killMe = inFile.readAllBytes();
                int size = killMe.length;
                out.write(killMe, 0, size);
                out.finish();
                //System.out.println(Singleton.getMatches().toString() + " matchesSet: " + Singleton.matchesSet + " totalMatches: " + Singleton.totalMatches);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Storage result;

    public static void LifeIsPain(String[] args) {

        try {
            File fileData = new File(args[0]);
            FileInputStream inFile = new FileInputStream(args[0]);
            byte[] killMe = inFile.readAllBytes();
            int size = killMe.length;
            System.out.println(size);
            //System.out.println(TheProblem(killMe, 58));
            //killMe = new byte[]{2, 5, 7, 0, 1, 3, 2, 6, 8, 9, 0, 1, 3, 5, 7 ,8, 9};
            result = new Storage(new Storage[size]);


            //ArrayList<Storage> patterns = ReplaceAllLongestRepeatingPatterns(killMe, result, 0);



            //TestMethod(args, patterns);
            //System.out.println(Arrays.deepToString(LongestCommonPattern(patterns)));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void TestMethod(String[] args, ArrayList<Storage> toCompare) {
        ArrayList<Storage> patterns = null;
        Storage[] coolResult = null;

        try {
            FileInputStream fileIn = new FileInputStream("patterns.storage");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            patterns = (ArrayList<Storage>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException ioe) {
            ioe.printStackTrace();
        }

        try {
            FileInputStream fileIn = new FileInputStream("result.storage");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            coolResult = (Storage[]) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException ioe) {
            ioe.printStackTrace();
        }


        System.out.println("The big test: ");
        System.out.println(Arrays.deepToString(patterns.toArray()));
        System.out.println(Arrays.deepEquals(patterns.toArray(), toCompare.toArray()));
        System.out.println("The bigger test: ");
        System.out.println(Arrays.deepToString(result.getPattern()));
        System.out.println(Arrays.deepEquals(result.getPattern(), coolResult));



    }

    public static void CommonSubpatternDevelopment(String filepath, boolean readFromFile, boolean writeToFile, int minPatternSize) throws IOException, InvalidMidiDataException, MidiUnavailableException, InterruptedException {
            File fileData = new File(filepath);
            FileInputStream inFile = new FileInputStream(filepath);
           // byte[] killMe = inFile.readAllBytes();
            //LRCParser l = new LRCParser("","/Users/jacksegil/Desktop/compression/testfiles/lyrics/lrc/aroundtheworld.lrc", 32);
            /*LRCParser l = new LRCParser("/Users/jacksegil/Desktop/compression/testfiles/misery.raw","/Users/jacksegil/Desktop/compression/testfiles/lyrics/lrc/misery.lrc", 16, 2, 44100, "misery");
            System.out.println(l.lyrics);
            LyricSegment[] killMe = l.lyrics.toArray(new LyricSegment[0]);
            int size = killMe.length;
            System.out.println(size);*/

            MidiParser m = new MidiParser("/Users/jacksegil/Desktop/compression/testfiles/oneday.raw", 24, 2, 44100, "oneday", false);
            ArrayList<MidiSegment> segments = m.theStuff("/Users/jacksegil/Downloads/oneday.mid");
            ArrayList<MidiSegment> segments1 = m.segments1;

        // ArrayList<String[]> tableOfContents = MidiParser.parseMidiFile("/Users/jacksegil/Downloads/mega.mid");

            MidiSegment[] midiSegments = segments.toArray(new MidiSegment[0]);
            int size = midiSegments.length;

            /* TODO: UNCOMMENT WHEN USEFUL

            //System.out.println(TheProblem(killMe, 58));
            //killMe = new byte[]{2, 5, 7, 0, 1, 3, 2, 6, 8, 9, 0, 1, 3, 5, 7 ,8, 9};
            result = new Storage(new Storage[size]);
            //double fileLength = fileData.length();
            double fileLength = size;

            //convert bytes to storage
            for (int i = 0; i < midiSegments.length; i++) {
                result.getPattern()[i] = new Storage<MidiSegment>(midiSegments[i]);
            }

            if (writeToFile) {
                result.setSubpatterns(ReplaceAllLongestRepeatingPatterns(result, minPatternSize));

                // Serialization code
                try
                {
                    FileOutputStream fileOut = new FileOutputStream(filepath + ".storage");
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(result);
                    out.close();
                    fileOut.close();
                }
                catch (IOException i)
                {
                    i.printStackTrace();
                }
            }


            if (readFromFile) {

                try {
                    FileInputStream fileIn = new FileInputStream(filepath + ".storage");
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    result = (Storage) in.readObject();
                    in.close();
                    fileIn.close();
                } catch (IOException | ClassNotFoundException ioe) {
                    ioe.printStackTrace();
                }
            }

            if (!readFromFile && !writeToFile) {
                result.setSubpatterns(ReplaceAllLongestRepeatingPatterns(result, minPatternSize));
            }

            System.out.println("Finished LRP, moving on");

          //  result.getSubpatterns().add(result);

            // ArrayList<Storage> newPatterns = ReplaceAllLongestRepeatingPatterns(); // what to name this?

            // TODO: modify storage class to store an arraylist containing the longest non-overlapping repeating subpatterns for the pattern.
            // TODO: use this instead of adding parent pattern to list of patterns when finding longest common substring in order to accurately represent space taken
            // TODO: when calculating space taken, should iterate over each patterns list as they weren't previously referneced anywhere else and thus mistakenly excluded from the space taken calculation.
            ReplaceAllLongestCommonPatterns(result, minPatternSize);
            AfterTheFirstRun(result, minPatternSize);


            int count = result.compressedSizeInBytes();

            System.out.println("Before: " + fileLength + ". After: " + count);
            System.out.println("Reduced to " + ((count / fileLength) * 100) + " percent of original size");
            System.out.println(result); TODO: end big uncomment */




 /* // UNCOMMENT FOR LRC
            try {
                for (LyricSegment lyricSegment : stupidSort(l.lyrics)) {
                    l.writeIntoWAVFiles(lyricSegment);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            /*System.out.println(Thread.activeCount());
            System.out.println(Arrays.toString(LongestCommonPattern(segments.get(0).data.get(1), ((MidiSegment) result.getPattern()[1].getData()).data.get(1))));
        System.out.println(LongestCommonPattern(((MidiSegment) result.getPattern()[0].getData()).data.get(1), ((MidiSegment) result.getPattern()[1].getData()).data.get(1)).length);
            System.out.println(((MidiSegment) result.getPattern()[1].getData()).data.getFirst().length);*/



        try {

            var sortedSegments = stupidSort2(segments);
           var sortedSegments1 = stupidSort2(segments1);

/*
            SegmentContainer container = new SegmentContainer(segments, 0);
            ArrayList<SegmentContainer> rr = new ArrayList<>();
            rr.add(container);
            SliceSegmentsDebug(m, segments, rr);
            rr.removeFirst();
            container = new SegmentContainer(segments1, 0);
            rr.add(container);
            SliceSegmentsDebug(m, segments1, rr);
*/
            /*
            for (MidiSegment segment : segments) {

                m.writeIntoWAVFiles(segment);
            }
            for (MidiSegment segment : segments1) {
                m.writeIntoWAVFiles(segment);
            }*/


           SliceSegments(m, segments, sortedSegments);
           SliceSegments(m, segments1, sortedSegments1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File map = new File("/Users/jacksegil/Desktop/compression/testfiles/oneday/" + "mapfirstchannel.txt");
            FileWriter myWriter = new FileWriter(map);

            for (int i = 0; i < segments.size(); i++) {
                myWriter.write(m.segmentEntry(segments.get(i)) + "\n");
                //myWriter.write(m.segmentEntry(segments1.get(i)) + "\n");
            }
            myWriter.close();

             map = new File("/Users/jacksegil/Desktop/compression/testfiles/oneday/" + "mapsecondchannel.txt");
            myWriter = new FileWriter(map);

            for (int i = 0; i < segments1.size(); i++) {
                //myWriter.write(m.segmentEntry(segments.get(i)) + "\n");
                myWriter.write(m.segmentEntry(segments1.get(i)) + "\n");
            }

            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            e.printStackTrace();

        }

            //  writeWAV(((MidiSegment) result.getPattern()[0].getData()).data, 44100, "/Users/jacksegil/Downloads/onebomb.wav", 24, 2);
      // writeWAV(((MidiSegment) result.getPattern()[1].getData()).data, 44100, "/Users/jacksegil/Downloads/twobomb.wav", 24, 2);





           /* byte[] resultAsByteArray = result.toByteArray();
            for (int i = 0; i < killMe.length; i++) {
                if (resultAsByteArray[i] != killMe[i]) {
                    System.out.println(resultAsByteArray[i] + " != " + killMe[i] + " at position " + i);
                }
            }
            System.out.println("Verification result " + Arrays.equals(resultAsByteArray, killMe));*/

    }

    private static void SliceSegments(MidiParser m, ArrayList<MidiSegment> segments, ArrayList<SegmentContainer> sortedSegments) throws Exception {

        ArrayList<String> registry = new ArrayList<>();
        int iteration = 0;
        for (SegmentContainer container : sortedSegments) {

            ArrayList<MidiSegment> instances = container.segments;
            int partNumber = 1;
            while (!instances.isEmpty()) {

                SegmentContainer firstHalves = new SegmentContainer(container.segments.getFirst(), 0);
                // System.out.println(segments.indexOf(container.segments.getFirst()));
                SegmentContainer secondHalves = new SegmentContainer(container.segments.getFirst(),0);
                firstHalves.setLengthSplit(partNumber);
                secondHalves.setLengthSplit(partNumber);

                ArrayList<int[]> data = new ArrayList<>();
                for (MidiSegment seg : instances) {
                    data.addAll(seg.data);
                }

                int length = MidiParser.getSmallestLength(data);
                for (MidiSegment instance : instances) {
                    ArrayList<int[]> tempList  = new ArrayList<int[]>();
                    tempList.add(Arrays.copyOfRange(instance.data.getFirst(), 0, length));
                    firstHalves.addMidiSegment(new MidiSegment(instance.duration, instance.notes, tempList, instance.channel));
                    if (length < instance.data.getFirst().length) {
                        ArrayList<int[]> tempList2  = new ArrayList<int[]>();//TODO: KILL TEMPLIST BY GETTING RID OF ARRAYLIST OF INT ARRAYS IN MIDISEGMENT AND JUST STORING THE GODDAMN INT ARRAY
                        tempList2.add(Arrays.copyOfRange(instance.data.getFirst(), length, instance.data.getFirst().length));
                        secondHalves.addMidiSegment(new MidiSegment(instance.duration, instance.notes, tempList2, instance.channel));
                    }
                }

                //writeWAV(firstHalves, "/Users/jacksegil/Desktop/compression/testfiles/iwonder/" + lyricSegment.getLyric() + partNumber + ".wav", bitDepth, instances.size());
                //writeWAVNoCompression(firstHalves, "/Users/jacksegil/Desktop/compression/testfiles/iwondern/" + lyricSegment.getLyric() + partNumber + ".wav", bitDepth, instances.size());

                // Thread.startVirtualThread(new WavWriterThread(firstHalves, "/Users/jacksegil/Desktop/compression/testfiles/" + fileName + "/" + midiSegment.notes + partNumber + ".wav", bitDepth, instances.size(), sampleRate));


                int position = 0;
                for (MidiSegment segment : firstHalves.segments) { //assumes firstHalves is a container containing midisegments compeltely ready for writing (outside of the lengthSplit value, which will be set here) , this loop updates the order refernence segment array for encoding purposees
                    MidiSegment segmentToSearchFor = new MidiSegment(segment.duration, segment.notes, segment.data, segment.channel, segment.index); // only index, channel, and notes are used for comparsion
                    segmentToSearchFor.lengthSplit = partNumber - 1; //non-lengthsplit segments should have a lengthSplit of zero



                    segmentToSearchFor = instances.get(position);
                    int indexOfLatestSplit = segments.indexOf(segmentToSearchFor);


                        MidiSegment segmentToAdd = new MidiSegment(segment.duration, segment.notes, segment.data, segment.channel, segment.index);
                        segmentToAdd.lengthSplit = partNumber;
                      /*  if (segments.contains(segmentToAdd)) {
                            int indexOfTheEvil = segments.indexOf(segmentToAdd);
                            MidiSegment evil = segments.get(indexOfTheEvil);
                            throw new Exception("Why the HELL does this segment already exist?");
                        }*/
                        if (partNumber == 1) {
                            segments.set(indexOfLatestSplit, segmentToAdd);
                        } else {
                            segments.add(indexOfLatestSplit + 1, segmentToAdd);
                        }

                    position++;
                }


                instances = secondHalves.segments;
                partNumber++;

                String output = firstHalves.notes + "l" + firstHalves.getLengthSplit() + "p" + firstHalves.getPerfSplit();

                if (registry.contains(output)) {
                    throw new Exception("whyyyyyyy");
                }

                registry.add(output);
                m.writeIntoWAVFiles(firstHalves);
            }


            iteration++;
        }
    }

    private static void SliceSegmentsDebug(MidiParser m, ArrayList<MidiSegment> segments, ArrayList<SegmentContainer> sortedSegments) throws Exception {

        for (SegmentContainer container : sortedSegments) {

            ArrayList<MidiSegment> instances = container.segments;
            ArrayList<Integer> toWrite = new ArrayList<>();
            for (MidiSegment segment : instances) {
                toWrite.addAll(new ArrayList<>(Arrays.asList(Arrays.stream(segment.data.getFirst()).boxed().toArray(Integer[]::new))));
            }
            ArrayList<int[]> data = new ArrayList<>();
            data.add(toWrite.stream().mapToInt(i -> i).toArray());
            MidiSegment stupid = new MidiSegment(0, container.notes, data, container.channel);
            m.writeIntoWAVFiles(stupid);
        }
    }


    public static void writeWAV(ArrayList<int[]> channels, int sampleRate, String outputPath, int bitDepth, int numChannels) throws IOException, InterruptedException {
        AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(LRCParser.writePCMToByteArray(channels, bitDepth, numChannels)), new AudioFormat(sampleRate, bitDepth, numChannels, true, false), channels.getFirst().length);

        FileOutputStream fileOut = new FileOutputStream(outputPath);
        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, fileOut);
        fileOut.close();
        ProcessBuilder pb = new ProcessBuilder("/Users/jacksegil/Downloads/mp4alsRM23/bin/mac/mp4alsRM23", "-t" + numChannels, "-7", outputPath);
        pb.directory(new File(System.getProperty("user.home")));
        Process p = pb.start();
        p.waitFor();
        Files.delete(Paths.get(outputPath));
    }



    public static ArrayList<LyricSegment> stupidSort(ArrayList<LyricSegment> lyrics) {
        ArrayList<LyricSegment> toReturn = new ArrayList<>();
        for (int i = 0; i < lyrics.size(); i++) {
            LyricSegment currLyric  = lyrics.get(i);
            if (toReturn.contains(currLyric)) {
                toReturn.get(toReturn.indexOf(currLyric)).mergeLyricSegments(currLyric);
            } else {
                toReturn.add(currLyric);
            }
        }

        return toReturn;
    }

    public static ArrayList<SegmentContainer> stupidSort2(ArrayList<MidiSegment> segments) throws Exception {
        ArrayList<SegmentContainer> toReturn = new ArrayList<>();

        int count = 0;

        for (MidiSegment currSegment : segments) {
            SegmentContainer container = new SegmentContainer(currSegment);
            if (toReturn.contains(container)) {
                int size = toReturn.get(toReturn.indexOf(container)).size();
                toReturn.get(toReturn.indexOf(container)).addMidiSegment(currSegment);
                if (toReturn.get(toReturn.indexOf(container)).size() != size + 1) {
                    System.err.println("fart");
                }
            } else {
                toReturn.add(container);
            }
            count++;
            int totalSize = 0;
            for (SegmentContainer container2 : toReturn) {
                totalSize += container2.segments.size();
            }

            if (count != totalSize) {
                throw new Exception("I am going to throw this computer into hell");
            }
        }

        int totalSize = 0;
        for (SegmentContainer container : toReturn) {
            totalSize += container.segments.size();
        }
        System.out.println("size " + totalSize);
        System.out.println("total " + segments.size());



        int limit = 200; //max number of tracks we allow to be written to a single file
        for (int i = 0; i < toReturn.size(); i++) {
            SegmentContainer currContainer = toReturn.get(i);
            int divisor = currContainer.size() / limit; //divisor must be dynamic or i will exprience incredible, incredible pain
           // int quarter = currContainer.size() / divisor; // change name, not really a quarter


            if (currContainer.size() > limit) {
                int guard = 0;
                int evilIndex = i;
                for (int k = 0 ; k < divisor ; k++) {// TODO: standardize numbers, figure out a way to include a part number even if not needed so can reliabiliy know position of each signigifant end digit
                    SegmentContainer toAdd = new SegmentContainer(new ArrayList<>(currContainer.segments.subList(k * limit, (k + 1) * limit)), k);
                    if (toReturn.contains(toAdd)) {
                        evilIndex = toReturn.indexOf(toAdd);
                        if (evilIndex != i) {
                            throw new Exception ("aaaaaaaaaaa " + evilIndex);
                        }
                        //toReturn.add(toAdd);
                        guard += toAdd.size();
                        toReturn.set(evilIndex, toAdd);
                    } else {
                        toReturn.add(toAdd);
                        guard += toAdd.size();
                    }
                }
                if (currContainer.size() % limit > 0) {
                    SegmentContainer toAdd = new SegmentContainer(new ArrayList<>(currContainer.segments.subList(limit * (divisor), currContainer.size())), divisor);
                    toReturn.add(toAdd);
                    guard += toAdd.size();
                }

                if (evilIndex != i) {
                    throw new Exception ("aaaaaaaaaaa " + evilIndex);
                }
                if (guard != currContainer.size()) {
                    throw new Exception ("aaaadaaaaaaa " + guard);
                }
                //toReturn.remove(i);

                i--;
            }
        }

        return toReturn;
    }


    public static void AfterTheFirstRun(Storage pointer, int minPatternSize) {
        ArrayList<Storage> patterns = pointer.getSubpatterns();
        for (int i = 0; i < patterns.size(); i++) {
            patterns.get(i).setSubpatterns(ReplaceAllLongestRepeatingPatternsWithPointers(patterns.get(i), minPatternSize));

            if (patterns.get(i).getSubpatterns().size() > 1) {
                //patterns.get(i).getSubpatterns().add(patterns.get(i));
                ReplaceAllLongestCommonPatterns(patterns.get(i), minPatternSize);
            }
            if (!patterns.get(i).getSubpatterns().isEmpty()) {
                AfterTheFirstRun(patterns.get(i), minPatternSize);
            }
            else { //TODO: very stupid and doesn't even make a difference
                //patterns.get(i).setSubpatterns(null);
            }

        }
    }



    public static void ReplaceAllLongestCommonPatterns(Storage pointer, int minPatternSize) {
        ArrayList<Storage> patterns = pointer.getSubpatterns();
       // patterns.add(new Storage (pointer.pattern));
        patterns.addFirst(pointer);

        Storage curr = LongestCommonPattern(patterns, minPatternSize);

        while (curr != null) {
            System.out.println(curr);
            boolean isDuplicate = false;

            for (Storage toCompare : patterns) {
                if (curr != toCompare) { // if this is false for when it should be true, we're not preserving pointers properly!
                    toCompare.setPattern(ReplaceAll(curr, toCompare.getPattern()));
                } else {
                    isDuplicate = true;
                }
            }
            if (!isDuplicate) {
                patterns.add(curr);
            }

            curr = LongestCommonPattern(patterns, minPatternSize);
        }

        patterns.removeFirst();

        pointer.setSubpatterns(patterns);
    }

    public static Storage LongestCommonPattern(ArrayList<Storage> matches, int minPatternSize) {
        Storage longestPattern = new Storage(new Storage[0]);
        int iFinal = 0;
        int jFinal = 0;

        for (int i = 0; i < matches.size(); i++) {
            for (int j = i + 1; j < matches.size() ; j++) {
                Storage commonPattern = LongestCommonPattern(matches.get(i), matches.get(j));
                if (commonPattern.getPattern().length > longestPattern.getPattern().length) {
                    longestPattern = commonPattern;
                    iFinal = i;
                    jFinal = j;
                }
            }
        }

        if (longestPattern.getPattern().length < minPatternSize || (longestPattern.getPattern().length == 1 && longestPattern.getPattern()[0].isPointer())) {
            return null;
        }

        return longestPattern;
    }

    // can make faster, not through ncr but doing more things in one go
    /*public static Storage[] LongestCommonPattern(Storage[] matches) {
        Storage[] longestPattern = new Storage[0];

        for (int i = 0; i < matches.length; i++) {
            for (int j = i + 1; j < matches.length ; j++) {
                Storage[] commonPattern = LongestCommonPattern(matches[i].getPattern(), matches[j].getPattern());
                if (commonPattern.length > longestPattern.length) {
                    longestPattern = commonPattern;
                }
            }
        }

        return longestPattern;
    }*/

    public static String LongestCommonSubstring(String S1, String S2)
    {
        int Start = 0;
        int Max = 0;
        for (int i = 0; i < S1.length(); i++)
        {
            for (int j = 0; j < S2.length(); j++)
            {
                int x = 0;
                while (S1.charAt(i + x) == S2.charAt(j + x))
                {
                    x++;
                    if (((i + x) >= S1.length()) || ((j + x) >= S2.length())) break;
                }
                if (x > Max)
                {
                    Max = x;
                    Start = i;
                }
            }
        }
        return S1.substring(Start, (Start + Max));
    }

    public static int[] LongestCommonPattern(int[] S1, int[] S2)
    {
        int Start = 0;
        int Max = 0;
        for (int i = 0; i < S1.length; i++)
        {
            for (int j = 0; j < S2.length; j++)
            {
                int x = 0;
                while (S1[i + x] == S2[j + x])
                {
                    x++;
                    if (((i + x) >= S1.length) || ((j + x) >= S2.length)) break;
                }
                if (x > Max)
                {
                    Max = x;
                    Start = i;
                }
            }
        }
        //return S1.substring(Start, (Start + Max));
        return Arrays.copyOfRange(S1, Start, (Start + Max));
    }

    public static Storage LongestCommonPattern(Storage S1, Storage S2)
    {
        Storage[] S1p = S1.getPattern();
        Storage[] S2p = S2.getPattern();

        int Start = 0;
        int Max = 0;
        for (int i = 0; i < S1p.length; i++)
        {
            for (int j = 0; j < S2p.length; j++)
            {
                int x = 0;
                while (S1p[i + x].equals(S2p[j + x]))
                {
                    x++;
                    if (((i + x) >= S1p.length) || ((j + x) >= S2p.length)) break;
                }
                if (x > Max)
                {
                    Max = x;
                    Start = i;
                }
            }
        }
        if (Max == S1p.length) {
            return S1;
        } else if (Max == S2p.length) {
            return S2;
        } else {
            Storage[] toReturn = new Storage[Max];
            System.arraycopy(S1p, Start, toReturn, 0, Max);
            return new Storage(toReturn);
        }
    }





    public static ArrayList<Storage> ReplaceAllLongestRepeatingPatternsWithPointers(Storage data, int minPatternSize) {
        ArrayList<Storage> toReturn = new ArrayList<Storage>();

        //Storage[] curr = new Storage[0];
        Storage curr = new Storage(LongestRepeatedPatternWithPointers(data.getPattern(), minPatternSize));

        //System.out.println("About to enter outer while loop");

        int i = 0;

        while (curr.getPattern() != null && i < 1) {
            data.setPattern(ReplaceAll(curr, data.getPattern()));

            toReturn.add(curr);

            curr = new Storage(LongestRepeatedPatternWithPointers(data.getPattern(), minPatternSize));

            //System.out.println(Arrays.deepToString(toReturn.toArray()));
            //i++;
        }
        //System.out.println("Exited outer while loop");

        return toReturn;
    }

    public static ArrayList<Storage> ReplaceAllLongestRepeatingPatterns(Storage localResult, int minPatternSize) {
        ArrayList<Storage> toReturn = new ArrayList<Storage>();
        //String curr = LongestRepeatedSubstring(dataString);


        //Storage[] dataStorage = new Storage[data.length];

        //Storage[] curr = new Storage[0];
        Storage curr = new Storage(LongestRepeatedPattern(localResult.getPattern(), minPatternSize));
        System.out.println(Arrays.deepToString(curr.getPattern()));
        //System.out.println("About to enter outer while loop");

        int i = 0;

        while (curr.getPattern() != null && i < 1) {
            localResult.setPattern(ReplaceAll(curr, localResult.getPattern()));

            toReturn.add(curr);

            curr = new Storage(LongestRepeatedPattern(localResult.getPattern(), minPatternSize));

            System.out.println(Arrays.deepToString(curr.getPattern()));
            //i++;
        }
        //System.out.println("Exited outer while loop");


       // System.out.println("Verification result " + Arrays.equals(localResult.toByteArray(), data));
       // System.out.println(Arrays.toString(localResult.toByteArray()));
        //System.out.println(Arrays.toString(data));



        return toReturn;
    }







    // Returns the longest repeating non-overlapping
    // substring in str
    static String LongestRepeatedSubstring(String str) {

        int n = str.length();
        System.out.println("called longest repeated substring");
        int[][] LCSRe = new int[n + 1][n + 1];
        System.out.println("after array creation");

        String res = ""; // To store result
        int res_length = 0; // To store length of result

        // building table in bottom-up manner
        int i, index = 0;
        for (i = 1; i <= n; i++) {
            for (int j = i + 1; j <= n; j++) {
                // (j-i) > LCSRe[i-1][j-1] to remove
                // overlapping
                if (str.charAt(i - 1) == str.charAt(j - 1)
                        && LCSRe[i - 1][j - 1] < (j - i)) {
                    LCSRe[i][j] = LCSRe[i - 1][j - 1] + 1;

                    // updating maximum length of the
                    // substring and updating the finishing
                    // index of the suffix
                    if (LCSRe[i][j] > res_length) {
                        res_length = LCSRe[i][j];
                        index = Math.max(i, index);
                    }
                } else {
                    LCSRe[i][j] = 0;
                }
            }
        }

        // If we have non-empty result, then insert all
        // characters from first character to last
        // character of String
        if (res_length > 0) {
            for (i = index - res_length + 1; i <= index; i++) {
                res += str.charAt(i - 1);
            }
        }

        LCSRe = null;
        return res;
    }

    public static Storage[] LongestRepeatedPattern(Storage[] data, int minPatternSize) {
        int n = data.length;

        // TODO: figure out how to use a 4-bit or 6-bit data type instead of a byte
        byte[][] LCSRe = new byte[n + 1][n + 1];

        Storage[] res = null; // To store result
        int res_length = 0; // To store length of result

        // building table in bottom-up manner
        int i, index = 0;
        for (i = 1; i <= n; i++) {
            for (int j = i + 1; j <= n; j++) {
                // (j-i) > LCSRe[i-1][j-1] to remove
                // overlapping
                if (data[i - 1].fastSaferEquals(data[j - 1]) && LCSRe[i - 1][j - 1] < (j - i)) {
                    LCSRe[i][j] = (byte) (LCSRe[i - 1][j - 1] + 1);

                    // updating maximum length of the
                    // substring and updating the finishing
                    // index of the suffix
                    if (LCSRe[i][j] > res_length) {
                        res_length = LCSRe[i][j];
                        index = Math.max(i, index);
                    }

                }
                else {
                    LCSRe[i][j] = 0;
                }

            }

        }

        // If we have non-empty result, then insert all
        // characters from first character to last
        // character of String
        if (res_length >= minPatternSize) {
            res = new Storage[res_length];
            int k = 0;
            for (i = index - res_length + 1; i <= index; i++) {
                res[k] = data[i - 1];
                k++;
            }
        }


        return res;
        //return res + " length: " + res_length


    }

    public static Storage[] LongestRepeatedPatternWithPointers(Storage[] data, int minPatternSize) {
        int n = data.length;
        byte LCSRe[][] = new byte[n + 1][n + 1];

        Storage[] res = null; // To store result
        int res_length = 0; // To store length of result

        // building table in bottom-up manner
        int i, index = 0;
        for (i = 1; i <= n; i++) {
            for (int j = i + 1; j <= n; j++) {
                // (j-i) > LCSRe[i-1][j-1] to remove
                // overlapping
                if (data[i - 1].equals(data[j - 1]) && LCSRe[i - 1][j - 1] < (j - i)) {
                    LCSRe[i][j] = (byte) (LCSRe[i - 1][j - 1] + 1);

                    // updating maximum length of the
                    // substring and updating the finishing
                    // index of the suffix
                    if (LCSRe[i][j] > res_length) {
                        res_length = LCSRe[i][j];
                        index = Math.max(i, index);
                    }

                }
                else {
                    LCSRe[i][j] = 0;
                }

            }

        }

        // If we have non-empty result, then insert all
        // characters from first character to last
        // character of String
        if (res_length >= minPatternSize && !(res_length == 1 && data[index - res_length].isPointer())) {
            res = new Storage[res_length];
            int k = 0;
            for (i = index - res_length + 1; i <= index; i++) {
                res[k] = data[i - 1];
                k++;
            }
        }


        return res;
        //return res + " length: " + res_length


    }

    public static Storage[] ReplaceAll(Storage patternPointer, Storage[] data) {

        ArrayList<Integer> occurrences = Occurrences(data, patternPointer.getPattern());
        if (occurrences.isEmpty()) {
            return data;
        }
        int length = patternPointer.getPattern().length;
        Storage[] toReturn = new Storage[data.length - (occurrences.size() * length) + occurrences.size()];
        int writePos = 0; // could be secretly not working because of improper use of this

        for (int i = 0; i < occurrences.size(); i++) {

        }
        
        for (int i = 0; i < occurrences.size(); i++) {
            int index = occurrences.get(i);
            int otherDataLength = 0;

            if (i == 0 && index != 0) {
                otherDataLength = index;
                System.arraycopy(data, 0, toReturn, 0, otherDataLength);
                toReturn[index] = patternPointer;
                writePos += index + 1;
            } else if (index == 0) { // implied condition of i == 0
                toReturn[0] = patternPointer;
                writePos++;
            }
            else {
                int prevIndex = occurrences.get(i - 1);
                otherDataLength = index - (prevIndex + length);
                System.arraycopy(data, prevIndex + length, toReturn, writePos, otherDataLength);
                writePos += otherDataLength;
                toReturn[writePos] = patternPointer;
                writePos++;

            }
        }

        int prevIndex = occurrences.getLast();
        int otherDataLength = data.length - (prevIndex + length);
        System.arraycopy(data, prevIndex + length, toReturn, writePos, otherDataLength);

        for (int i = 0; i < toReturn.length; i++) {
            if (toReturn[i] == null) {
                System.out.println("I messed up at " + i);
            }
        }

        return toReturn;

    }

    public int indexOf(byte[] outerArray, byte[] smallerArray) {
        for(int i = 0; i < outerArray.length - smallerArray.length+1; ++i) {
            boolean found = true;
            for(int j = 0; j < smallerArray.length; ++j) {
                if (outerArray[i+j] != smallerArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    // need to make this non-overlapping!
    public static ArrayList<Integer> Occurrences(Storage[] outerArray, Storage[] smallerArray) {
        ArrayList<Integer> toReturn = new ArrayList<Integer>();
        int stupidConstant = outerArray.length - smallerArray.length;
        for(int i = 0; i <= stupidConstant; i++) {
            boolean found = true;
            for(int j = 0; j < smallerArray.length; ++j) {
                //maybe replace with fastEquals?
                if (!outerArray[i+j].equals(smallerArray[j])) {
                    found = false;
                    break;
                }
            }
            if (found){
                toReturn.add(i);
                i += smallerArray.length - 1;
            };
        }
        return toReturn;
    }



    public static String TheProblem(byte[] satan, int numRankings) {
        int n = satan.length;
        int[][] rankings = new int[numRankings][3];
        int[] rankings2 = new int[numRankings];
        int[][] LCSRe = new int[n + 1][n + 1];

        String res = ""; // To store result
        int res_length = 0; // To store length of result

        // building table in bottom-up manner
        int i, index = 0;
        for (i = 1; i <= n; i++) {
            for (int j = i + 1; j <= n; j++) {
                // (j-i) > LCSRe[i-1][j-1] to remove
                // overlapping
                if (satan[i - 1] == satan[j - 1] && LCSRe[i - 1][j - 1] < (j - i)) {
                    LCSRe[i][j] = LCSRe[i - 1][j - 1] + 1;

                    // updating maximum length of the
                    // substring and updating the finishing
                    // index of the suffix
                    if (LCSRe[i][j] > res_length) {
                        res_length = LCSRe[i][j];
                        index = Math.max(i, index);

                       if (res_length > rankings[0][0]) {
                            rankings[0][0] = res_length;
                            rankings[0][2] = i;
                            Arrays.sort(rankings, (a, b) -> Integer.compare(a[0], b[0]));
                        }
                        if (res_length > rankings2[0]) {
                            rankings2[0] = res_length;
                            Arrays.sort(rankings2);
                        }
                    }

                }
                else {
                        LCSRe[i][j] = 0;
                    }

            }

        }

        // If we have non-empty result, then insert all
        // characters from first character to last
        // character of String
        if (res_length > 0) {
            for (i = index - res_length + 1; i <= index; i++) {
                res += satan[i - 1];
            }
        }

        String satansString = ByteArrayToString(satan);

        int[] champ = new int[3];
        for (int b = 0; b < numRankings; b++) {
            // 1st element is length, 2nd element is number of matches, 3rd element is index
            rankings[b][1] = StringUtils.countMatches(satansString, rankingToString(satan, rankings[b][2], rankings[b][0]));

            if (champ[0] * champ[1] < rankings[b][0] * rankings[b][1]) {
                champ = rankings[b].clone();
            }
        }


        return "top lengths: " + Arrays.deepToString(rankings) + "\n most optimal: " + Arrays.toString(champ);
        //return res;
        //return res + " length: " + res_length + " end of data: " + Arrays.toString(Arrays.copyOfRange(satan, n + 1 - res_length, n + 1));
    }

    public static String rankingToString(byte[] satan, int index, int res_length) {
        String res = "";
        for (int i = index - res_length + 1; i <= index; i++) {
            res += satan[i - 1];
        }
        return res;
    }

    public static String ByteArrayToString(byte[] data) {
        String toReturn = "";
        for (int k = 0; k < data.length; k++) {
            toReturn += data[k];
        }
        return toReturn;
    }

    public static byte[] StringToByteArray(String data) {
        byte[] toReturn = new byte[data.length()];
        for (int k = 0; k < data.length(); k++) {
            toReturn[k] = Byte.parseByte(String.valueOf(data.charAt(k)));
        }
        return toReturn;
    }



}

