import java.io.*;
import java.util.Hashtable;

public class KonkordansBuilder implements Serializable {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        double start = System.currentTimeMillis();
        System.out.println("starting");
        RandomAccessFile randomAccessFile = new RandomAccessFile("/var/tmp/ut", "r");

        String word;
        String prev = null;
        long position = 0;
        Hashtable<String, Long> numbers = new Hashtable<>(12000);
        StringBuffer sb = new StringBuffer();
        String line = "init";
        while (line != null) {

            position = randomAccessFile.getFilePointer();
            if ((line = randomAccessFile.readLine()) == null)
                break;

            for (int i = 0; i < 3; i++) {
                if (line.charAt(i) == ' ')
                    break;
                sb.append(line.charAt(i));
            }
            word = sb.toString();
            sb.setLength(0);

            if (word.equals(prev))
                continue;

            prev = word;
            numbers.put(word, position);
        }

        System.out.println("numbers size: " + numbers.size());
        System.out.println((System.currentTimeMillis() - start) / 1000);
        randomAccessFile.close();
        

        /*
            Write hashtable to disk.
         */
        FileOutputStream fileOut = new FileOutputStream("hashtable.dat");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(numbers);
        out.close();
        fileOut.close();
        System.out.printf("saved hashtable in hashtable.dat");
    }
}
