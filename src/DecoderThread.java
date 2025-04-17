import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class DecoderThread implements Runnable {

    ConcurrentHashMap<String, ArrayList<int[]>> map;
    private final String name;
    private final String filePath;
    private final Decoder decoder;

    public DecoderThread(ConcurrentHashMap<String, ArrayList<int[]>> map, String name, String filePath, Decoder decoder) {
        this.map = map;
        this.name = name;
        this.filePath = filePath;
        this.decoder = decoder;
    }

    @Override
    public void run() {
        ThreadManager.threadCounter.incrementAndGet();

    try {
        ProcessBuilder pb = new ProcessBuilder("/Users/jacksegil/Downloads/mp4alsRM23/bin/mac/mp4alsRM23", "-x", filePath);
        pb.directory(new File(System.getProperty("user.home")));
        Process p = pb.start();
        p.waitFor();

        map.put(name, Decoder.readPCMFromWAV("/Users/jacksegil/Desktop/compression/testfiles/oneday/" + name + ".wav", decoder));


    }
    catch (Exception e) {
       e.printStackTrace();
       System.err.println(filePath);
    }
        ThreadManager.threadCounter.decrementAndGet();
    }
}
