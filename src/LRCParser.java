import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.AudioFileWriter;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class LRCParser {

    private final ArrayList<int[]> pcmData;
    public ArrayList<LyricSegment> lyrics;
    private final int sampleRate;
    private final int bitDepth;
    private final String fileName;




    public LRCParser(String pcmPath, String lrcPath, int bitDepth, int numChannels, int initSampleRate, String fileName) {
        this.bitDepth = bitDepth;
        sampleRate = initSampleRate;
        try {
            FileInputStream inFile = new FileInputStream(pcmPath);
            pcmData = readPCM(pcmPath, bitDepth, numChannels);
            inFile.close();

            FileReader input = new FileReader(lrcPath);
            BufferedReader bufRead = new BufferedReader(input);
            String myLine = null;

            lyrics = new ArrayList<>();


            ArrayList<String> lyricBuilder = new ArrayList<>();

            int i = 0;
            String mostRecentTimestamp = "";

            while ( (myLine = bufRead.readLine()) != null)
            {
                String[] lineContents = myLine.split("]");

                if (lineContents.length == 1) {
                    lineContents = new String[] {lineContents[0], "SILENCE"};
                }


                if (i == 0 && lineContents.length > 1 && !lineContents[0].equals("[00:00.00")) {
                    lyrics.add(new LyricSegment("SILENCE", getData("[00:00.00",lineContents[0])));
                    i++;
                } else {
                    i++;
                }


                for (String s : lineContents) {
                    if (s.charAt(0) == '[') {
                        mostRecentTimestamp = s;
                    }
                    lyricBuilder.add(s);
                    if (lyricBuilder.size() == 3) {
                        lyrics.add(new LyricSegment(lyricBuilder.get(1), getData(lyricBuilder.get(0), s)));
                        lyricBuilder.clear();
                        lyricBuilder.add(s);
                    }
                }

            }
            if (lyricBuilder.size() == 2) { // if we end on a lyric without an end timestamp
                lyrics.add(new LyricSegment(lyricBuilder.get(1), getData(mostRecentTimestamp, pcmData.getFirst().length)));
            } else if (numSeconds(mostRecentTimestamp) * sampleRate < pcmData.getFirst().length) { //if the last timestamp doens't go all the way to the end of the song
                lyrics.add(new LyricSegment("SILENCE", getData(lyricBuilder.getFirst(), pcmData.getFirst().length)));
            }

            bufRead.close();



            this.fileName = fileName;

            writeWAV(pcmData, "/Users/jacksegil/Desktop/compression/testfiles/" + fileName + ".wav", bitDepth, numChannels);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }




    public ArrayList<int[]> getData(String start, String end) { // start is inclusive, end is exclusive
        double startIndex = numSeconds(start) * sampleRate;
        double endIndex = numSeconds(end) * sampleRate;

        if (startIndex != (int)startIndex) {
            System.out.println("Start index isn't exact! It's " + startIndex + ". Rounding to " + Math.round(startIndex));
            startIndex = Math.round(startIndex);
        }
        if (endIndex != (int)endIndex) {
            System.out.println("End index isn't exact! It's " + endIndex + ". Rounding to " + Math.round(endIndex));
            endIndex = Math.round(endIndex);
        }

        ArrayList<int[]> toReturn = new ArrayList<>();
        for (int[] arr : pcmData) {
            toReturn.add(Arrays.copyOfRange(arr, (int) startIndex, (int) endIndex));
        }
        return toReturn;
    }

    public ArrayList<int[]> getData(String start, int end) { // start is inclusive, end is exclusive
        double startIndex = numSeconds(start) * sampleRate;
        double endIndex = end;

        if (startIndex != (int)startIndex) {
            System.out.println("Start index isn't exact! It's " + startIndex + ". Rounding to " + Math.round(startIndex));
            startIndex = Math.round(startIndex);
        }
        if (endIndex != (int)endIndex) {
            System.out.println("End index isn't exact! It's " + endIndex + ". Rounding to " + Math.round(endIndex));
            endIndex = Math.round(endIndex);
        }

        ArrayList<int[]> toReturn = new ArrayList<>();
        for (int[] arr : pcmData) {
            toReturn.add(Arrays.copyOfRange(arr, (int) startIndex, (int) endIndex));
        }
        return toReturn;
    }

    public double numSeconds(String timestamp) {
        double toReturn = 0;
        toReturn += 60 * Double.parseDouble(timestamp.substring(1, 3));
        toReturn += Double.parseDouble(timestamp.substring(4, 9));
        return toReturn;
    }





    public void writeIntoWAVFiles(LyricSegment lyricSegment) throws IOException, InterruptedException {
        if (!Files.isDirectory(Paths.get("/Users/jacksegil/Desktop/compression/testfiles/" + fileName + "/"))) {
            Files.createDirectory(Paths.get("/Users/jacksegil/Desktop/compression/testfiles/" + fileName + "/"));
        }

        ArrayList<int[]> instances = lyricSegment.getPcmData();
        int partNumber = 0;
        while (!instances.isEmpty()) {
            ArrayList<int[]> secondHalves = new ArrayList<>();
            ArrayList<int[]> firstHalves = new ArrayList<>();
            int length = getSmallestLength(instances);
            for (int[] instance : instances) {
                firstHalves.add(Arrays.copyOfRange(instance, 0, length));
                if (length < instance.length) {
                    secondHalves.add(Arrays.copyOfRange(instance, length, instance.length));
                }
            }

            //writeWAV(firstHalves, "/Users/jacksegil/Desktop/compression/testfiles/iwonder/" + lyricSegment.getLyric() + partNumber + ".wav", bitDepth, instances.size());
            //writeWAVNoCompression(firstHalves, "/Users/jacksegil/Desktop/compression/testfiles/iwondern/" + lyricSegment.getLyric() + partNumber + ".wav", bitDepth, instances.size());

            Thread.startVirtualThread(new WavWriterThread(firstHalves, "/Users/jacksegil/Desktop/compression/testfiles/" + fileName + "/" + lyricSegment.getLyric() + partNumber + ".wav", bitDepth, instances.size(), sampleRate));


            instances = secondHalves;
            partNumber++;
        }
    }

    private int getSmallestLength(ArrayList<int[]> instances) {
        int toReturn = instances.getFirst().length;
        for (int i = 1; i < instances.size(); i++) {
            if (instances.get(i).length < toReturn) {
                toReturn = instances.get(i).length;
            }
        }
        return toReturn;
    }

    public ArrayList<int[]> readPCM(String filePath, int bitDepth, int numChannels) throws IOException {
        byte[] data = Files.readAllBytes(Paths.get(filePath));
        int bytesPerSample = bitDepth / 8;
        int totalSamples = data.length / bytesPerSample;
        int samplesPerChannel = totalSamples / numChannels;

        ArrayList<int[]> channels = new ArrayList<>();
        for (int i = 0; i < numChannels; i++) {
            channels.add(new int[samplesPerChannel]);
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // PCM files usually use little-endian format, at least the one's I'm making

        for (int i = 0; i < samplesPerChannel; i++) {
            for (int ch = 0; ch < numChannels; ch++) {
                int sample = 0;
                switch (bitDepth) {
                    case 8:
                        sample = (buffer.get() & 0xFF) - 128; // 8-bit PCM is unsigned
                        break;
                    case 16:
                        sample = buffer.getShort();
                        break;
                    case 24:
                        sample = (buffer.get() & 0xFF) | ((buffer.get() & 0xFF) << 8) | ((buffer.get() << 16));
                        if ((sample & 0x800000) != 0) sample |= 0xFF000000; // Sign extend if negative
                        break;
                    case 32:
                        sample = buffer.getInt();
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported bit depth: " + bitDepth);
                }
                channels.get(ch)[i] = sample;
            }
        }

        return channels;
    }

    public void writePCM2(String outPath, ArrayList<int[]> sampleData, int bitDepth, int numChannels) throws IOException {

        int bytesPerSample = bitDepth / 8;
        int samplesPerChannel = sampleData.getFirst().length;
        int bytesPerChannel = bytesPerSample * samplesPerChannel;
       // int totalSamples = samplesPerChannel * numChannels;
        int byteLength = bytesPerChannel * numChannels;

        byte[] data = new byte[byteLength];

        ByteBuffer buffer = ByteBuffer.wrap(data);

        buffer.order(ByteOrder.LITTLE_ENDIAN); // PCM files usually use little-endian format, at least the one's I'm making

        FileOutputStream fileOut = new FileOutputStream(outPath);

        DataOutputStream out = new DataOutputStream(fileOut);
        //ObjectOutputStream out = new ObjectOutputStream(fileOut);



        for (int i = 0; i < samplesPerChannel; i++) {
            for (int ch = 0; ch < numChannels; ch++) {

                int sample = sampleData.get(ch)[i];
                switch (bitDepth) {
                    case 8:
                        sample = (buffer.get() & 0xFF) - 128; // 8-bit PCM is unsigned
                        break;
                    case 16:
                        sample = buffer.getShort();
                        break;
                    case 24:
                        sample = (buffer.get() & 0xFF) | ((buffer.get() & 0xFF) << 8) | ((buffer.get() << 16));
                        if ((sample & 0x800000) != 0) sample |= 0xFF000000; // Sign extend if negative
                        break;
                    case 32:
                        //sample = buffer.getInt();
                        //buffer.putInt(sample);
                        out.writeInt(sample);

                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported bit depth: " + bitDepth);
                }

            }
        }
        out.close();
        fileOut.close();

    }

    public static void writePCM(ArrayList<int[]> channels, String outputPath, int bitDepth, int numChannels) throws IOException {
        int samplesPerChannel = channels.get(0).length;
        ByteBuffer buffer = ByteBuffer.allocate(samplesPerChannel * numChannels * (bitDepth / 8));
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < samplesPerChannel; i++) {
            for (int ch = 0; ch < numChannels; ch++) {
                int sample = channels.get(ch)[i];
                switch (bitDepth) {
                    case 8:
                        buffer.put((byte) (sample + 128)); // Convert back to unsigned
                        break;
                    case 16:
                        buffer.putShort((short) sample);
                        break;
                    case 24:
                        buffer.put((byte) (sample & 0xFF));
                        buffer.put((byte) ((sample >> 8) & 0xFF));
                        buffer.put((byte) ((sample >> 16) & 0xFF));
                        break;
                    case 32:
                        buffer.putInt(sample);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported bit depth: " + bitDepth);
                }
            }
        }

        Files.write(Paths.get(outputPath), buffer.array());
    }

    public static boolean checkLengthEquality (ArrayList<int[]> channels) {
        int testLength = channels.getFirst().length;
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).length != testLength) {
                return false;
            }
        }
        return true;
    }

    public static byte[] writePCMToByteArray(ArrayList<int[]> channels, int bitDepth, int numChannels) throws IOException {
        if (!checkLengthEquality(channels)) {
            throw new IllegalArgumentException("All channels need to have the same length! " + numChannels);
        }

        int samplesPerChannel = channels.get(0).length;
        int capacity = samplesPerChannel * numChannels * (bitDepth / 8);
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < samplesPerChannel; i++) {
            for (int ch = 0; ch < numChannels; ch++) {
                int sample = channels.get(ch)[i];
                switch (bitDepth) {
                    case 8:
                        buffer.put((byte) (sample + 128)); // Convert back to unsigned
                        break;
                    case 16:
                        buffer.putShort((short) sample);
                        break;
                    case 24:
                        buffer.put((byte) (sample & 0xFF));
                        buffer.put((byte) ((sample >> 8) & 0xFF));
                        buffer.put((byte) ((sample >> 16) & 0xFF));
                        break;
                    case 32:
                        buffer.putInt(sample);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported bit depth: " + bitDepth);
                }
            }
        }

        return buffer.array();
    }

    public static void writeOptimalWAV(ArrayList<int[]> channels, String outputPath, int bitDepth, int numChannels) throws IOException, InterruptedException {
        int c = Files.readAllBytes(Paths.get(outputPath)).length;
        int nc = Files.readAllBytes(Paths.get(outputPath + "_nc")).length;
        if (c >= nc) {
            Files.delete(Paths.get(outputPath));
        } else {
            Files.delete(Paths.get(outputPath + "_nc"));
        }
    }

    /*public static void writeOptimalWAVSinglethreaded(ArrayList<int[]> channels, String outputPath, int bitDepth, int numChannels) throws IOException, InterruptedException {
        int c = writeWAV(channels, outputPath, bitDepth, numChannels);
        int nc = writeWAVNoCompression(channels, outputPath + "_nc", bitDepth, numChannels);
        if (c >= nc) {
            Files.delete(Paths.get(outputPath));
        } else {
            Files.delete(Paths.get(outputPath + "_nc"));
        }
    }*/

    public void writeWAV(ArrayList<int[]> channels, String outputPath, int bitDepth, int numChannels) throws IOException, InterruptedException {
        AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(writePCMToByteArray(channels, bitDepth, numChannels)), new AudioFormat(sampleRate, bitDepth, numChannels, true, false), channels.getFirst().length);

        FileOutputStream fileOut = new FileOutputStream(outputPath);
        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, fileOut);
        fileOut.close();
        ProcessBuilder pb = new ProcessBuilder("/Users/jacksegil/Downloads/mp4alsRM23/bin/mac/mp4alsRM23", "-t" + numChannels, "-7", outputPath);
        pb.directory(new File(System.getProperty("user.home")));
        Process p = pb.start();
        p.waitFor();
        Files.delete(Paths.get(outputPath));
    }

    public int writeWAVNoCompression(ArrayList<int[]> channels, String outputPath, int bitDepth, int numChannels) throws IOException, InterruptedException {
        AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(writePCMToByteArray(channels, bitDepth, numChannels)), new AudioFormat(sampleRate, bitDepth, numChannels, true, false), channels.getFirst().length);

        FileOutputStream fileOut = new FileOutputStream(outputPath);
        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, fileOut);
        fileOut.close();
        ProcessBuilder pb = new ProcessBuilder("/Users/jacksegil/Downloads/mp4alsRM23/bin/mac/mp4alsRM23", "-7", outputPath);
        pb.directory(new File(System.getProperty("user.home")));
        Process p = pb.start();
        p.waitFor();
        Files.delete(Paths.get(outputPath));
        return Files.readAllBytes(Paths.get(outputPath)).length;
    }



}



