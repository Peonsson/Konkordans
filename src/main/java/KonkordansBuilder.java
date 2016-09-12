import java.io.*;
import java.util.Hashtable;

public class KonkordansBuilder implements Serializable {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        double start = System.currentTimeMillis();
        System.out.println("starting");
        RandomAccessFile randomAccessFile = new RandomAccessFile("/var/tmp/ut", "r");
        RandomAccessFile uniqueWords = new RandomAccessFile("uniqueWords", "rw");
        RandomAccessFile indexFile = new RandomAccessFile("indexFile", "rw");

        //Three word
        String threeCharWord;
        String prevThree = null;
        long position = 0;

        //Full word
        String fullWord = null;
        String prevFull = null;
        long indexposition = 0;

        Hashtable<String, Long> numbers = new Hashtable<>(12000);
        StringBuffer sb = new StringBuffer();
        String line = "init";
        while (line != null) {

            position = randomAccessFile.getFilePointer();
            if ((line = randomAccessFile.readLine()) == null)
                break;

            //adding to new indexfile
            String[] lineWords = line.split(" ");
            fullWord = lineWords[0];
            if(!fullWord.equals(prevFull)){
                //if not the first word then cut the line when new word begins
                //in the indexfile
                if(prevFull!=null){
                    indexFile.writeByte((byte) '\n');
                }
                //Write word in uniquewords
                int length = fullWord.length();
                char[] charArray = fullWord.toCharArray();
                for(int i = 0; i < length; i++){
                    uniqueWords.writeByte((byte) charArray[i]);
                }
                uniqueWords.writeByte((byte) ' ');

                //Write index to indexfile in uniqueWords
                charArray = Long.toString(indexFile.getFilePointer()).toCharArray();
                for(int i = 0; i < charArray.length; i++){
                    uniqueWords.writeByte((byte) charArray[i]);
                }
                uniqueWords.writeByte((byte) '\n');

                //Write this index in indexfile
                charArray = lineWords[1].toCharArray();
                for(int i = 0; i < charArray.length; i++){
                    indexFile.writeByte((byte) charArray[i]);
                }
            }else{
                //Write the korpus index in indexFile
                char[] charArray = lineWords[1].toCharArray();
                indexFile.writeByte((byte) ' ');
                for(int i = 0; i < charArray.length; i++){
                    indexFile.writeByte((byte) charArray[i]);
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
            numbers.put(threeCharWord, position);
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
