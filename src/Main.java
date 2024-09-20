import org.tukaani.xz.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {




        try {


            File fileData = new File(args[0]);
            FileInputStream inFile = new FileInputStream(args[0]);
            FileOutputStream outfile = new FileOutputStream(args[0]+".xz");

            LZMA2Options options = new LZMA2Options();


            options.setNiceLen((int) (fileData.length() / 2));

           // options.setPreset(7); // play with this number: 6 is default but 7 works better for mid sized archives ( > 8mb)

            XZOutputStream out = new XZOutputStream(outfile, options);

            byte[] buf = new byte[8192];
            int size;
            while ((size = inFile.read(buf)) != -1) {
                out.write(buf, 0, size);

            }

            out.finish();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}