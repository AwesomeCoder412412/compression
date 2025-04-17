import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Decoder {

    private final String filePath;
    public ConcurrentHashMap<String, ArrayList<int[]>> map;
    private int bitDepth;
    private int numChannels;
    private int sampleRate;

    public Decoder(String filePath) {
        map = new ConcurrentHashMap<>();
        this.filePath = filePath;
    }

    public static ArrayList<Integer> ToIntArrayList(int[] arr) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i : arr) {
            list.add(i);
        }
        return list;
    }

    public static int[] toIntArray(ArrayList<Integer> arr) {
        int[] toReturn = new int[arr.size()];
        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = arr.get(i);
        }
        return toReturn;
    }

    public static ArrayList<int[]> readPCMFromWAV(String file, Decoder decoder) throws IOException {
        File wavFile = new File(file);

        int bitDepth = 0;
        int numChannels = 0;
        int sampleRate = 0;

        byte[] data = new byte[(int) wavFile.length()];

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile);
            AudioFormat format = audioInputStream.getFormat();
            data = audioInputStream.readAllBytes();

            bitDepth = format.getSampleSizeInBits();
            numChannels = format.getChannels();
            sampleRate = (int) format.getSampleRate();

            decoder.bitDepth = bitDepth;
            decoder.numChannels = numChannels;
            decoder.sampleRate = sampleRate;

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

    public ArrayList<int[]> Decode() throws IOException, InterruptedException {

        String directoryPath = filePath; //assumed unzipped for now
        File directory = new File(directoryPath);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && !file.getName().endsWith(".txt") && !file.isHidden()) {
                        System.out.println("File: " + file.getName().substring(0, file.getName().length() - 4));
                        Thread.startVirtualThread(new DecoderThread(map, file.getName().substring(0, file.getName().length() - 4), filePath + "/" + file.getName(), this));
                    } else if (file.isDirectory()) {
                        System.out.println("Directory: " + file.getName());
                    }
                }
            } else {
                System.err.println("Could not list files in directory: " + directoryPath);
            }
        } else {
            System.err.println("Not a directory: " + directoryPath);
        }

        File leftMap = new File(filePath + "/mapfirstchannel.txt");
        File rightMap = new File(filePath + "/mapsecondchannel.txt");

        ArrayList<Integer> leftChannel = new ArrayList<>();
        ArrayList<Integer> rightChannel = new ArrayList<>();


        while (ThreadManager.threadCounter.get() > 0) {
            // I despise this but it works. All hail the ThreadManager.
        }

        rebuild(leftMap, leftChannel, "left");
        rebuild(rightMap, rightChannel, "right");

        ArrayList<int[]> toWrite = new ArrayList<>();
        toWrite.add(toIntArray(leftChannel));
        toWrite.add(toIntArray(rightChannel));

        System.out.println(numChannels);
        AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(LRCParser.writePCMToByteArray(toWrite, bitDepth, 2)), new AudioFormat(sampleRate, bitDepth, 2, true, false), toWrite.getFirst().length);
        FileOutputStream fileOut = new FileOutputStream("/Users/jacksegil/Desktop/compression/testfiles/output.wav");
        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, fileOut);
        fileOut.close();

        return null;
    }

    private void rebuild(File currMap, ArrayList<Integer> currChannel, String channelName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(currMap))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Process each line here
                String[] split = line.split("i");
                String key = split[0];
                int index = Integer.parseInt(split[1]);

                if (!map.containsKey(key)) {
                    key = key + "_nc";
                }
                if (!map.containsKey(key)) {
                    throw new Exception("we messed up" + key);
                }

                ArrayList<int[]> channels = map.get(key);
                int[] channel = channels.get(index);
                ArrayList<Integer> toAdd = ToIntArrayList(channel);
                currChannel.addAll(toAdd);
            }
        } catch (IOException e) {
            System.err.println("Error reading " + channelName + " map file: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

