import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Decoder {

    public ConcurrentHashMap<String, ArrayList<int[]>> map;
    private String filePath;

    public Decoder(String filePath) {
        map = new ConcurrentHashMap<>();
        this.filePath = filePath;
    }

    public ArrayList<int[]> Decode() {

        String directoryPath = filePath; //assumed unzipped for now
        File directory = new File(directoryPath);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && !file.getName().endsWith(".txt")) {
                        System.out.println("File: " + file.getName().substring(0, file.getName().length() - 4));
                        Thread.startVirtualThread(new DecoderThread(map, file.getName().substring(0, file.getName().length() - 4), filePath + "/" + file.getName()));
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



        return null;
    }

    public static ArrayList<int[]> readPCMFromWAV(String file) throws IOException {

        File wavFile = new File(file); // Replace with your file path

        int bitDepth = 0;
        int numChannels = 0;

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile);
            AudioFormat format = audioInputStream.getFormat();

            bitDepth = format.getSampleSizeInBits();
            numChannels = format.getChannels();

            System.out.println("Bit Depth: " + bitDepth + " bits");
            System.out.println("Number of Channels: " + numChannels);

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio file format: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading the audio file: " + e.getMessage());
        }

        byte[] data = Files.readAllBytes(Paths.get(file));
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

}

