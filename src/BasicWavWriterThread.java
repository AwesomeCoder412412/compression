import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class BasicWavWriterThread implements Runnable {

    private final ArrayList<int[]> channels;
    private final String outputPath;
    private final int bitDepth;
    private final int numChannels;
    private final int sampleRate;

    public BasicWavWriterThread(ArrayList<int[]> channels, String outputPath, int bitDepth, int numChannels, int sampleRate) {
        this.channels = channels;
        this.outputPath = outputPath;
        this.bitDepth = bitDepth;
        this.numChannels = numChannels;
        this.sampleRate = sampleRate;
    }



    @Override
    public void run() {
        ThreadManager.threadCounter.incrementAndGet();
        try {
            AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(LRCParser.writePCMToByteArray(channels, bitDepth, numChannels)), new AudioFormat(sampleRate, bitDepth, numChannels, true, false), channels.getFirst().length);
            FileOutputStream fileOut = new FileOutputStream(outputPath);
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, fileOut);
            fileOut.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        ThreadManager.threadCounter.decrementAndGet();
    }

}
