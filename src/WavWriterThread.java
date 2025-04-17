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

public class WavWriterThread implements Runnable {

    private final ArrayList<int[]> channels;
    private final String outputPath;
    private final int bitDepth;
    private final int numChannels;
    private final int sampleRate;

    public WavWriterThread(ArrayList<int[]> channels, String outputPath, int bitDepth, int numChannels, int sampleRate) {
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

            AudioInputStream stream2 = new AudioInputStream(new ByteArrayInputStream(LRCParser.writePCMToByteArray(channels, bitDepth, numChannels)), new AudioFormat(sampleRate, bitDepth, numChannels, true, false), channels.getFirst().length);
            FileOutputStream fileOut2 = new FileOutputStream(outputPath.replaceFirst(".wav", "") + "_nc.wav");
            AudioSystem.write(stream2, AudioFileFormat.Type.WAVE, fileOut2);
            fileOut2.close();

            /*
            AudioInputStream stream3 = new AudioInputStream(new ByteArrayInputStream(LRCParser.writePCMToByteArray(channels, bitDepth, numChannels)), new AudioFormat(sampleRate, bitDepth, numChannels, true, false), channels.getFirst().length);
            FileOutputStream fileOut3 = new FileOutputStream(outputPath.replaceFirst(".wav", "") + "_s.wav");
            AudioSystem.write(stream3, AudioFileFormat.Type.WAVE, fileOut3);
            fileOut3.close();*/


            ProcessBuilder pb = new ProcessBuilder("/Users/jacksegil/Downloads/mp4alsRM23/bin/mac/mp4alsRM23", "-t" + numChannels, "-7", outputPath);
            pb.directory(new File(System.getProperty("user.home")));
            Process p = pb.start();


            ProcessBuilder pbnc = new ProcessBuilder("/Users/jacksegil/Downloads/mp4alsRM23/bin/mac/mp4alsRM23", "-7", outputPath.replaceFirst(".wav", "") + "_nc.wav");
            pbnc.directory(new File(System.getProperty("user.home")));
            Process pnc = pbnc.start();

/*
            ProcessBuilder pbs = new ProcessBuilder("/Users/jacksegil/Downloads/mp4alsRM23/bin/mac/mp4alsRM23", "-s" + numChannels, "-7", outputPath.replaceFirst(".wav", "") + "_s.wav");
            pbs.directory(new File(System.getProperty("user.home")));
            Process ps = pbs.start();
            */



            p.waitFor();
            pnc.waitFor();
           // ps.waitFor();


            try(var stdout = pnc.getInputStream()) {
                stdout.transferTo(System.out);
            }
            //System.out.println(pnc.isAlive());

           // System.out.println(pnc.isAlive());
            if (outputPath.equals("/Users/jacksegil/Desktop/compression/testfiles/oneday/60101l6p1.wav")) {
                System.out.println("deleted the problem file");
            }

            Files.delete(Paths.get(outputPath));
            Files.delete(Paths.get(outputPath.replaceFirst(".wav", "") + "_nc.wav"));
            //Files.delete(Paths.get(outputPath.replaceFirst(".wav", "") + "_s.wav"));

            String alsPath = outputPath.replaceFirst("wav", "als");

            String alsPathNC = outputPath.replaceFirst(".wav", "") + "_nc.als";

            int c = Files.readAllBytes(Paths.get(alsPath)).length;
            int nc = Files.readAllBytes(Paths.get(alsPathNC)).length;
            //int s = Files.readAllBytes(Paths.get(alsPathS)).length;
            System.out.println(alsPath + " is " + c + " bytes");
            System.out.println(alsPathNC + " is " + nc + " bytes");
          //  System.out.println(alsPathS + " is " + s + " bytes");
            int s = 99999999;
            //System.out.println("compression savings of " + (nc-c) + " bytes. compressed file had " + numChannels + " channels.");

            int winner = Math.min(s, Math.min(c, nc));



            if (c == winner) {
                System.out.println("winner was c. compression savings of " + (nc-c)  + " bytes for c vs nc."  + "compression savings of " + (s-c)  + " bytes for c vs s. compressed file had"  + numChannels + " channels.");
                Files.delete(Paths.get(alsPathNC));
                //Files.delete(Paths.get(alsPathS));
            } else if (nc == winner) {
                System.out.println("winner was nc. compression savings of " + (c-nc)  + " bytes for nc vs c."  + "compression savings of " + (s-nc)  + " bytes for nc vs s. compressed file had"  + numChannels + " channels.");
                Files.delete(Paths.get(alsPath));
                //Files.delete(Paths.get(alsPathS));
            } else {
                System.out.println("winner was s. compression savings of " + (nc-s)  + " bytes for s vs nc."  + "compression savings of " + (c-s)  + " bytes for s vs c. compressed file had"  + numChannels + " channels.");
                Files.delete(Paths.get(alsPath));
                Files.delete(Paths.get(alsPathNC));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        ThreadManager.threadCounter.decrementAndGet();
    }

}
