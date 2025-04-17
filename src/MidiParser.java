import javax.sound.midi.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MidiParser {

    public static ArrayList<int[]> pcmData;
    public ArrayList<MidiSegment> segments;
    public ArrayList<MidiSegment> segments1;
    private static int sampleRate;
    private int bitDepth;
    private String fileName;
    public ArrayList<String> registry;
    Random rand;

    public MidiParser(String pcmPath, int bitDepth, int numChannels, int initSampleRate, String fileName, boolean processOG) throws IOException, InterruptedException {
        this.bitDepth = bitDepth;
        sampleRate = initSampleRate;
        try {
            FileInputStream inFile = new FileInputStream(pcmPath);
            pcmData = readPCMFromWAV(pcmPath);



            inFile.close();

            Thread.startVirtualThread(new BasicWavWriterThread(pcmData, "/Users/jacksegil/Desktop/compression/testfiles/stupid.wav", bitDepth, numChannels, sampleRate));

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(pcmPath));
            AudioFormat format = audioInputStream.getFormat();
            byte[] data = audioInputStream.readAllBytes();
            byte[] dota = LRCParser.writePCMToByteArray(pcmData, bitDepth, numChannels)
            ;
            for (int i = 0; i < dota.length; i++) {
                if (dota[i] != data[i]) {
                    System.out.println("Error in reading PCM");
                }
            }
            if (Arrays.equals(dota, data)) {
                System.out.println("Successfully read PCM from " + pcmPath);
            }


        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.fileName = fileName;
        if (processOG) {
            Main.writeWAV(pcmData, sampleRate, "/Users/jacksegil/Desktop/compression/testfiles/" + fileName + ".wav", bitDepth, numChannels);
        }
        registry = new ArrayList<>();
        rand = new Random();
        rand.setSeed(0);
    }


    public static ArrayList<String[]> parseMidiFile(String filePath) throws InvalidMidiDataException, IOException, MidiUnavailableException {
        Sequence sequence = MidiSystem.getSequence(new File(filePath));
        TreeMap<Long, HashSet<String>> activeNotes = new TreeMap<>();
        TreeMap<Long, Long> tempoMap = new TreeMap<>(); // Stores tick -> microseconds per quarter note

        // Default tempo (120 BPM = 500,000 microseconds per quarter note)
        tempoMap.put(0L, 500000L);

        int trackNumber = 0;
        for (Track track : sequence.getTracks()) {
            HashMap<String, Long> noteStartTimes = new HashMap<>();
            trackNumber++;

            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                if (message instanceof MetaMessage) {
                    MetaMessage meta = (MetaMessage) message;
                    if (meta.getType() == 0x51) { // Set Tempo
                        byte[] data = meta.getData();
                        long tempo = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                        tempoMap.put(event.getTick(), tempo);
                    }
                } else if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    int command = sm.getCommand();
                    int key = sm.getData1();
                    long timestamp = event.getTick();
                    String noteIdentifier = key + "-T" + trackNumber; // Unique identifier per track

                    if (command == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                        noteStartTimes.put(noteIdentifier, timestamp);
                    } else if (command == ShortMessage.NOTE_OFF || (command == ShortMessage.NOTE_ON && sm.getData2() == 0)) {
                        if (noteStartTimes.containsKey(noteIdentifier)) {
                            long start = noteStartTimes.remove(noteIdentifier);
                            activeNotes.putIfAbsent(start, new HashSet<>());
                            activeNotes.get(start).add(noteIdentifier);
                            activeNotes.putIfAbsent(timestamp, new HashSet<>());
                        }
                    }
                }
            }
        }

        ArrayList<String[]> tableOfContents = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(activeNotes.keySet());

        for (int i = 0; i < timestamps.size() - 1; i++) {
            long start = ticksToMicroseconds(timestamps.get(i), sequence.getResolution(), tempoMap);
            long end = ticksToMicroseconds(timestamps.get(i + 1), sequence.getResolution(), tempoMap);
            Set<String> notes = activeNotes.get(timestamps.get(i));
            if (notes != null && !notes.isEmpty()) {
                String noteData = generateNoteIdentifier(notes);
                tableOfContents.add(new String[]{String.valueOf(start), String.valueOf(end), noteData});
            }
        }

      //  Synthesizer synth = MidiSystem.getSynthesizer();
       // synth.open();
       // synth.

        return tableOfContents;
    }

    private static long ticksToMicroseconds(long tick, int resolution, TreeMap<Long, Long> tempoMap) {
        long previousTick = 0;
        long totalTime = 0;
        long currentTempo = 500000L; // Default microseconds per quarter note (120 BPM)

        for (Map.Entry<Long, Long> entry : tempoMap.entrySet()) {
            long tempoTick = entry.getKey();
            if (tempoTick > tick) break;

            totalTime += ((tempoTick - previousTick) * currentTempo) / resolution;
            previousTick = tempoTick;
            currentTempo = entry.getValue();
        }

        totalTime += ((tick - previousTick) * currentTempo) / resolution;
        return totalTime;
    }

    private static String generateNoteIdentifier(Set<String> notes) {
        List<String> sortedNotes = new ArrayList<>(notes);
        Collections.sort(sortedNotes);
        //return "Notes: " + sortedNotes.toString();
        return sortedNotes.toString().replace(" ", "").replace("[", "").replace("]", "").replace(",", "").replace("-T","");
    }

    public ArrayList<int[]> readPCMFromWAV(String filePath) throws IOException {


        File wavFile = new File(filePath); // Replace with your file path


        byte[] data = new byte[(int) wavFile.length()];

        int numChannels = 0;

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile);
            AudioFormat format = audioInputStream.getFormat();
            data = audioInputStream.readAllBytes();

            bitDepth = format.getSampleSizeInBits();
            numChannels = format.getChannels();
            sampleRate = (int) format.getSampleRate();

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio file format: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading the audio file: " + e.getMessage());
        }


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

    public static ArrayList<int[]> getData(long start, long end) { // start is inclusive, end is exclusive
        double startIndex = index(start);
        double endIndex = index(end);

        if (startIndex != (int)startIndex) {
            System.out.println("Start index isn't exact! It's " + startIndex + ". Rounding to " + Math.round(startIndex));
            startIndex = Math.round(startIndex);
        }
        if (endIndex != (int)endIndex) {
            System.out.println("End index isn't exact! It's " + endIndex + ". Rounding to " + Math.round(endIndex));
            endIndex = Math.round(endIndex);
        }

        System.out.println("SAMPLE LENGTH: " + (endIndex - startIndex));

        ArrayList<int[]> toReturn = new ArrayList<>();
        for (int[] arr : pcmData) {
            if (startIndex - 100 < 0) {
                toReturn.add(Arrays.copyOfRange(arr, (int) startIndex, (int) endIndex));
            } else {
                toReturn.add(Arrays.copyOfRange(arr, (int) startIndex, (int) endIndex));
            }
        }
        return toReturn;
    }

    private static ArrayList<int[]> getDataToEnd(long start) { // start is inclusive, end is exclusive
        double startIndex = index(start);
        double endIndex = pcmData.getFirst().length;

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
            if (startIndex - 100 < 0) {
                toReturn.add(Arrays.copyOfRange(arr, (int) startIndex, (int) endIndex));
            } else {
                toReturn.add(Arrays.copyOfRange(arr, (int) startIndex, (int) endIndex));
            }
        }
        return toReturn;
    }

    public static ArrayList<int[]> getDataMono(long start, long end, int channel) { // start is inclusive, end is exclusive
        double startIndex = index(start);
        double endIndex = index(end);

        if (startIndex != (int)startIndex) {
            System.out.println("Start index isn't exact! It's " + startIndex + ". Rounding to " + Math.round(startIndex));
            startIndex = Math.round(startIndex);
        }
        if (endIndex != (int)endIndex) {
            System.out.println("End index isn't exact! It's " + endIndex + ". Rounding to " + Math.round(endIndex));
            endIndex = Math.round(endIndex);
        }

        System.out.println("SAMPLE LENGTH: " + (endIndex - startIndex));

        ArrayList<int[]> toReturn = new ArrayList<>();
        int[] arr = pcmData.get(channel);
            if (startIndex - 100 < 0) {
                toReturn.add(Arrays.copyOfRange(arr, (int) startIndex, (int) endIndex));
            } else {
                toReturn.add(Arrays.copyOfRange(arr, (int) startIndex, (int) endIndex));
            }

        return toReturn;
    }

    private static ArrayList<int[]> getDataToEndMono(long start, int channel) { // start is inclusive, end is exclusive
        double startIndex = index(start);
        double endIndex = pcmData.getFirst().length;

        if (startIndex != (int)startIndex) {
            System.out.println("Start index isn't exact! It's " + startIndex + ". Rounding to " + Math.round(startIndex));
            startIndex = Math.round(startIndex);
        }
        if (endIndex != (int)endIndex) {
            System.out.println("End index isn't exact! It's " + endIndex + ". Rounding to " + Math.round(endIndex));
            endIndex = Math.round(endIndex);
        }

        ArrayList<int[]> toReturn = new ArrayList<>();
        int[] arr = pcmData.get(channel);


        int[] toAdd = Arrays.copyOfRange(arr, (int) startIndex, (int) endIndex);
        toReturn.add(toAdd);
        return toReturn;
    }


    private static double index(long microseconds) {
        return ((double)microseconds / 1000000) * sampleRate;
    }


    public ArrayList<MidiSegment> theStuff(String filePath) {
        ArrayList<Integer> track = new ArrayList<>();
        try {
            segments = new ArrayList<>();
            segments1 = new ArrayList<>();
            ArrayList<String[]> tableOfContents = parseMidiFile(filePath);
            for (int i = 0; i < tableOfContents.size() - 1; i++) {
                String[] entry = tableOfContents.get(i);
                String[] nextEntry = tableOfContents.get(i + 1);

                long one = Long.parseLong(entry[1]);
                long two = Long.parseLong(nextEntry[0]);

                if (one != two) {
                    System.out.println("problem");
                }

                segments.add(new MidiSegment((Long.parseLong(nextEntry[0]) - Long.parseLong(entry[0])), entry[2] + 0, getDataMono(Long.parseLong(entry[0]), Long.parseLong(nextEntry[0]), 0), 0, i)); // i = place

                track.addAll(Decoder.ToIntArrayList(getDataMono(Long.parseLong(entry[0]), Long.parseLong(nextEntry[0]), 0).get(0)));

                segments1.add(new MidiSegment((Long.parseLong(nextEntry[0]) - Long.parseLong(entry[0])), entry[2] + 1, getDataMono(Long.parseLong(entry[0]), Long.parseLong(nextEntry[0]), 1), 1, i));
                System.out.println("Start: " + entry[0] + ", End: " + entry[1] + ", Duration: " + (Long.parseLong(entry[1]) - Long.parseLong(entry[0])) +  ", Identifier: " + entry[2]);
            }
            String[] entry = tableOfContents.getLast();
            segments.add(new MidiSegment((Long.parseLong(entry[1]) - Long.parseLong(entry[0])), entry[2] + 0, getDataToEndMono(Long.parseLong(entry[0]), 0), 0, tableOfContents.size() - 1));

            track.addAll(Decoder.ToIntArrayList(getDataToEndMono(Long.parseLong(entry[0]), 0).get(0)));
            if (!Arrays.equals(pcmData.getFirst(), Decoder.toIntArray(track))) {
                System.out.println("f");
            }

            ArrayList<Integer> reconstruction = new ArrayList<>();
            for (MidiSegment segment : segments) {
                reconstruction.addAll(Decoder.ToIntArrayList(segment.data.getFirst()));
            }

            if (!Arrays.equals(pcmData.getFirst(), Decoder.toIntArray(reconstruction))) {
                System.out.println("f2");
            }

            segments1.add(new MidiSegment((Long.parseLong(entry[1]) - Long.parseLong(entry[0])), entry[2] + 1, getDataToEndMono(Long.parseLong(entry[0]), 1), 1,tableOfContents.size() - 1));
            System.out.println("Start: " + entry[0] + ", End: " + entry[1] + ", Duration: " + (Long.parseLong(entry[1]) - Long.parseLong(entry[0])) +  ", Identifier: " + entry[2]);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return segments;
    }

    public static void main(String[] args) {
        /*try {
            ArrayList<MidiSegment> segments = new ArrayList<>();
            ArrayList<String[]> tableOfContents = parseMidiFile("/Users/jacksegil/Downloads/mega.mid");
            for (String[] entry : tableOfContents) {
                segments.add(new MidiSegment((Long.valueOf(entry[1]) - Long.valueOf(entry[0])), entry[2], null));
                System.out.println("Start: " + entry[0] + ", End: " + entry[1] + ", Duration: " + (Long.parseLong(entry[1]) - Long.parseLong(entry[0])) +  ", Identifier: " + entry[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public void writeIntoWAVFiles(SegmentContainer container) throws IOException, InterruptedException {
        if (!Files.isDirectory(Paths.get("/Users/jacksegil/Desktop/compression/testfiles/" + fileName + "/"))) {
            Files.createDirectory(Paths.get("/Users/jacksegil/Desktop/compression/testfiles/" + fileName + "/"));
        }

        String outputPath = "/Users/jacksegil/Desktop/compression/testfiles/" + fileName + "/" + container.notes + "l" + container.getLengthSplit() + "p" + container.getPerfSplit() + ".wav";
        if (outputPath.equals("/Users/jacksegil/Desktop/compression/testfiles/oneday/7230753079300l1p0.wav")) {
            System.out.println("Writing to " + outputPath);
        }
        if (registry.contains(outputPath)) {
            throw new IllegalArgumentException("why the hell are we writing this file twice? " + outputPath);
        } else {
            registry.add(outputPath);
        }
        Thread.startVirtualThread(new WavWriterThread(container.rawData(), outputPath, bitDepth, container.size(), sampleRate));

    }


    public void writeIntoWAVFiles(MidiSegment segment) throws IOException, InterruptedException { //for debugging purposes only
        if (!Files.isDirectory(Paths.get("/Users/jacksegil/Desktop/compression/testfiles/" + fileName + "wav/"))) {
            Files.createDirectory(Paths.get("/Users/jacksegil/Desktop/compression/testfiles/" + fileName + "wav/"));
        }

        String outputPath = "/Users/jacksegil/Desktop/compression/testfiles/" + fileName + "wav/" + segment.notes + "l" + segment.lengthSplit + "p" + segment.perfSplit + ".wav";



        while (registry.contains(outputPath)) {
            outputPath = outputPath.substring(0, outputPath.length() - 4) +  + rand.nextInt() + ".wav";
        }
            registry.add(outputPath);


        Thread.startVirtualThread(new BasicWavWriterThread(segment.data, outputPath, bitDepth, segment.data.size(), sampleRate));

    }

    public static String segmentEntry(MidiSegment segment) {
        return segment.notes + "l" + segment.lengthSplit + "p" + segment.perfSplit + "i" + segment.index;
    }

    public static int getSmallestLength(ArrayList<int[]> instances) {
        int toReturn = instances.getFirst().length;
        for (int i = 1; i < instances.size(); i++) {
            if (instances.get(i).length < toReturn) {
                toReturn = instances.get(i).length;
            }
        }
        return toReturn;
    }

}

