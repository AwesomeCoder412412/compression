import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class LRCParser {

    private int[] pcmData;
    public ArrayList<String> lyrics;


    public LRCParser(String pcmPath, String lrcPath) {
        try {
          /*  FileInputStream inFile = new FileInputStream(pcmPath);
            pcmData = GetSamples(inFile.readAllBytes());
            inFile.close(); */

            FileReader input = new FileReader(lrcPath);
            BufferedReader bufRead = new BufferedReader(input);
            String myLine = null;

            lyrics = new ArrayList<>();

            while ( (myLine = bufRead.readLine()) != null)
            {
                String[] array1 = myLine.split("] ");
                if (array1.length == 2) {
                    lyrics.add(array1[1]);
                } else {
                    lyrics.add("SILENCE");
                }

            }

            bufRead.close();
            System.out.println(lyrics);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public int[] GetSamples(byte[] input) {
        IntBuffer intBuf =
                ByteBuffer.wrap(input)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .asIntBuffer();
        int[] array = new int[intBuf.remaining()];
        intBuf.get(array);
        return array;
    }
}
