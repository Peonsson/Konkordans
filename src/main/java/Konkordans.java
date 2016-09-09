import java.io.*;
import java.util.*;

public class Konkordans implements Serializable {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        /*
            Read hashtable from disk.
            NOTE: will fail if hashtable haven't been previously written to disk.
         */
        FileInputStream fileInputStream = new FileInputStream("hashtable.dat");
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        Hashtable<String, Long> latmanhash = (Hashtable<String, Long>) objectInputStream.readObject();
        objectInputStream.close();
        fileInputStream.close();


        //scissor first 3 letters in word to search in hashtable
        String arg = args[0];
        String substring;
        if (arg.length() >= 3) {
            substring = arg.substring(0, 3);
        } else {
            substring = arg;
        }
        //get index position from hashtable
        Long position = latmanhash.get(substring);
        if (position == null) {
            return;
        }

//        RandomAccessFile indexfile = new RandomAccessFile("/var/tmp/ut", "r");
        RandomAccessFile indexfile = new RandomAccessFile("index", "r");

        // go to position in index
        indexfile.seek(position);
        String line;
        String[] strings = null;

        Long lposition = 0l;
        ArrayList<Long> al = new ArrayList<Long>(1000);
        boolean foundword = false;

        // append korpus indexes to arrayList
        while ((line = indexfile.readLine()) != null) {
            // only search aaa-aab(first)
            if (line.substring(0, substring.length()).equals(substring)) {
                strings = line.split(" ");
                // keep looking if we didn't find the right word (continue);
                // if we already found the correct word but then find a different word we have gone through all relevant words = break from loop;
                if (!strings[0].equals(arg)) {
                    if (foundword) {
                        break;
                    } else {
                        continue;
                    }
                }
                foundword = true;
                al.add(Long.parseLong(strings[1]));
            } else {
                break;
            }
        }

        System.out.println("Found " + al.size() + " entries of " + arg);

        //Check if more than 25
        if (al.size() > 25) {
            System.out.print("Found more than 25 entries. Would you like to print them in the console? (y/n): ");
            char input;
            Scanner s = new Scanner(System.in);
            while (true) {
                input = s.next().charAt(0);
                if (input == 'n') {
                    return;
                } else if (input == 'y') {
                    break;
                }
            }
        }

        int bytestoread = 60 + arg.length();
        byte[] byteholder = new byte[bytestoread];
        RandomAccessFile korpus = new RandomAccessFile("korpus", "r");

        // write to console
        for (Long l : al) {
            korpus.seek(l - 30);

            korpus.readFully(byteholder, 0, bytestoread);
            for (int i = 0; i < byteholder.length; i++) {
                if (byteholder[i] == '\n') {
                    byteholder[i] = ' ';
                }
            }
            System.out.println(new String(byteholder, "ISO-8859-1"));
        }
    }
}
