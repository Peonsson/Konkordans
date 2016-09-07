import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Hashtable;

public class Konkordans {

    public static void main(String[] args) {

        RandomAccessFile randomAccessFile = null;
        try {
            //randomAccessFile = new RandomAccessFile("/Users/Peonsson/WORK/tokenizer", "r");
            randomAccessFile = new RandomAccessFile("C:\\index", "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String word = "hej";
        String line = "init";
        String prev = null;
        long position = 0;
        Hashtable<String, Long> numbers = new Hashtable<>(30 * 30 * 30);
        String[] strings;
        double start = System.currentTimeMillis();

        while(line != null) {

            try {
                position = randomAccessFile.getFilePointer();
                if((line = randomAccessFile.readLine()) == null)
                    break;

                strings = line.split(" ");
                word = strings[0];

                if (word.length() > 3)
                    word = word.substring(0, 3);

                if(word.equals(prev))
                    continue;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            prev = word;

            numbers.put(word, position);
        }
        //System.out.println(numbers.toString());
        System.out.println("numbers size: " + numbers.size());
        System.out.println((System.currentTimeMillis() - start) / 1000);
    }
}