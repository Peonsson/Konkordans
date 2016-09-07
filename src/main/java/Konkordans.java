import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Hashtable;

/**
 * Created by Peonsson on 06/09/16.
 */
public class Konkordans {

    public static void main(String[] args) {
        String[] strblet = {"a", "b", "c", "d", "e,", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
                "t", "u", "v", "w", "x", "y", "z", "å", "ä", "ö", " "};

        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile("/Users/Peonsson/WORK/tokenizer", "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        String word = "hej";

        while(word != null) {
            String position = null;

            try {
                String line = randomAccessFile.readLine();
                if (line == null)
                    break;

                String[] strings = line.split(" ");
                word = strings[0];
                position = strings[1];

                if (word.length() > 3)
                    word = word.substring(0, 2);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Hashtable<String, Long> numbers = new Hashtable<String, Long>(30 * 30 * 30);
            numbers.put(word, Long.parseLong(position));
        }
    }
}
