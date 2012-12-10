package log;


import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class FileLog {

    File file;

    public FileLog(File file) {
        this.file = file;
    }

    public void writeLine(String msg) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(msg);
            out.newLine();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String read() throws IOException {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            stream.close();
        }
        return null;
    }

}
