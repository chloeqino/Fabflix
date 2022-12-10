import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
    private static BufferedWriter writer;
    private static String filepath;
    public Logger(){
        try {
            filepath = "/Users/cloverain/Desktop/fabflix/";
            writer = new BufferedWriter(new FileWriter(filepath+"log.txt", true));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public void appendLine(String line) throws IOException {
        writer.append(line);
    }
    public void close() throws IOException {
        writer.close();
    }
}
