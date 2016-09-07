import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Hashtable;

/**
 * Created by Peonsson on 06/09/16.
 */
public class Konkordans {

    public static void main(String[] args) {

        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile("/Users/Peonsson/WORK/tokenizer", "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String word = "hej";
        String line = "init";
        String prev = null;
        String position = null;
        int i = 0;
        Hashtable<String, Long> numbers = new Hashtable<String, Long>(30 * 30 * 30);

        while(line != null && i < 15) {

            try {
                if((line = randomAccessFile.readLine()) == null)
                    break;

                String[] strings = line.split(" ");
                word = strings[0];
                position = strings[1];

                if (word.length() > 3)
                    word = word.substring(0, 2);

                if(word.equals(prev))
                    continue;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            prev = word;

            numbers.put(word, Long.parseLong(position));
            i++;
        }
        System.out.println(numbers.toString());
        System.out.println("numbers size: " + numbers.size());
    }
}
