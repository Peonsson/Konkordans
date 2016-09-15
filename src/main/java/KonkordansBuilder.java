import java.io.*;
import java.util.Hashtable;

public class KonkordansBuilder implements Serializable {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        double start = System.currentTimeMillis();
        System.out.println("starting");
//        DataInputStream ut = new DataInputStream(new BufferedInputStream(new FileInputStream("/var/tmp/ut")));
        DataInputStream ut = new DataInputStream(new BufferedInputStream(new FileInputStream("index")));
        DataOutputStream uniqueWords = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("uniqueWords")));
        DataOutputStream indexFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("indexFile")));

        //Three word
        String threeCharWord;
        String prevThree = null;
        long position = 0;

        //Full word
        String fullWord = null;
        String prevFull = null;
        long indexposition = 0; //where the pointer in indexfile is
        long wordfileposition = 0; //where the pointer in uniqueword is
        long hashposition = 0; //where we should store the word in hashmap

        Hashtable<String, Long> numbers = new Hashtable<>(12000);
        StringBuffer sb = new StringBuffer();
        String line = "init";
        while (line != null) {
            hashposition = wordfileposition;

            //Read the 
            if ((line = ut.readLine()) == null)
                break;
            position += line.length() + 1;

            //adding to new indexfile
            String[] lineWords = line.split(" ");
            fullWord = lineWords[0];
            if (!fullWord.equals(prevFull)) {
                //if not the first word then cut the line when new word begins
                //in the indexfile
                if (prevFull != null) {
                    indexFile.writeByte((byte) '\n');
                    indexposition++;
                }
                //Write word in uniquewords
                int length = fullWord.length();
                char[] charArray = fullWord.toCharArray();
                for (int i = 0; i < length; i++) {
                    uniqueWords.writeByte((byte) charArray[i]);
                    wordfileposition++;
                }
                uniqueWords.writeByte((byte) ' ');
                wordfileposition++;

                //Write index to indexfile in uniqueWords
                charArray = Long.toString(indexposition).toCharArray();
                for (int i = 0; i < charArray.length; i++) {
                    uniqueWords.writeByte((byte) charArray[i]);
                    wordfileposition++;
                }
                uniqueWords.writeByte((byte) '\n');
                wordfileposition++;

                //Write this index in indexfile
                charArray = lineWords[1].toCharArray();
                for (int i = 0; i < charArray.length; i++) {
                    indexFile.writeByte((byte) charArray[i]);
                    indexposition++;
                }
            } else {
                //Write the korpus index in indexFile
                char[] charArray = lineWords[1].toCharArray();
                indexFile.writeByte((byte) ' ');
                indexposition++;
                for (int i = 0; i < charArray.length; i++) {
                    indexFile.writeByte((byte) charArray[i]);
                    indexposition++;
                }
            }
            prevFull = fullWord;

            //Three first characters in word
            for (int i = 0; i < 3; i++) {
                if (line.charAt(i) == ' ')
                    break;
                sb.append(line.charAt(i));
            }
            threeCharWord = sb.toString();
            sb.setLength(0);

            if (threeCharWord.equals(prevThree))
                continue;

            prevThree = threeCharWord;
            numbers.put(threeCharWord, hashposition);
        }

        uniqueWords.close();
        indexFile.close();
        System.out.println("numbers size: " + numbers.size());
        System.out.println((System.currentTimeMillis() - start) / 1000);
        ut.close();
        

        /*
            Write hashtable to disk.
         */
        FileOutputStream fileOut = new FileOutputStream("hashtable.dat");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(numbers);
        out.close();
        fileOut.close();
        System.out.printf("saved hashtable in hashtable.dat\n");
    }
}
