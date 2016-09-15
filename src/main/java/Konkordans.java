import java.io.*;
import java.util.*;

public class Konkordans implements Serializable {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        /*
            Read hashtable from disk.
            NOTE: will fail if hashtable haven't been previously written to disk.
         */
        if (args.length != 1) {
            System.out.println("Illegal arguments");
            return;
        }
        FileInputStream fileInputStream = new FileInputStream("hashtable.dat");
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        Hashtable<String, Long> latmanhash = (Hashtable<String, Long>) objectInputStream.readObject();
        objectInputStream.close();
        fileInputStream.close();

        //trim the searchkey
        String arg = args[0];
        String substring;
        if (arg.length() >= 3) {
            substring = arg.substring(0, 3);
        } else {
            substring = arg;
        }

        //Get position in uniqueWords
        Long position = latmanhash.get(substring);
        if (position == null) {
            System.out.println("Found 0 entries of that word");
            return;
        }

        RandomAccessFile uniqueWords = new RandomAccessFile("uniqueWords", "r");

        uniqueWords.seek(position);
        String line;
        String[] strings = null;

        Long lposition = 0l;
        boolean foundword = false;
        boolean asked = false; //asked user if it wants all entries


        //Binary search for a position with the correct word
        long upperbound = nextSubstringPos(substring, latmanhash, uniqueWords);
        long lowerbound = position;
        long binpointer = lowerbound + (upperbound - lowerbound) / 2;
        long prevbinpointer = -1l;
        String binpointerWord = null; //the word of line binpointer is at
        char tempchar = ' ';
        boolean foundmatch = false;
        //If there is no upper bound we just linear search until the word hits
        while (upperbound != lowerbound) { //while the bounds are not the same
            //position the cursor
            uniqueWords.seek(binpointer);
            while (uniqueWords.read() != '\n') { //eats it

                if (uniqueWords.getFilePointer() == 1) {
                    uniqueWords.seek(0);
                    break;
                }

                //cursor is placed one after bin pointer
                uniqueWords.seek(uniqueWords.getFilePointer() - 2); //cursor back 2 chars
            }
            //cursor will be on first char on line
            binpointer = uniqueWords.getFilePointer();

            //If the binary pointer is the same as the last it means it is stuck
            if (prevbinpointer == binpointer) {
                String tryword = null;
                while (uniqueWords.getFilePointer() < upperbound) {
                    binpointer = uniqueWords.getFilePointer();
                    tryword = readTo(uniqueWords, ' '); //eat first word
                    if (arg.equals(tryword)) {
                        foundmatch = true;
                        break;
                    }
                    readTo(uniqueWords, '\n'); //eat whole line
                }
                break;
            }
            prevbinpointer = binpointer;

            //Check the word to a space
            binpointerWord = readTo(uniqueWords, ' ');
            //if the word equals the argument

            int compare = binpointerWord.compareTo(arg);
            if (compare < 0) {
                //if currentword is before arg in index
                lowerbound = binpointer;
                binpointer = lowerbound + (upperbound - lowerbound) / 2;
            } else if (compare > 0) {
                //if currentword is after arg in index
                upperbound = binpointer;
                binpointer = lowerbound + (upperbound - lowerbound) / 2;
            } else {
                foundmatch = true;
                break;
            }
        }
//        System.out.println("Found word?: " + foundmatch);
        uniqueWords.seek(binpointer);
//        System.out.println(binpointerWord);

        String str = uniqueWords.readLine();
        String[] uniqueWordStrings = str.split(" ");
        RandomAccessFile uniqueWordsIndex = new RandomAccessFile("indexFile", "r");
        RandomAccessFile korpus = new RandomAccessFile("korpus", "r");
        uniqueWordsIndex.seek(Long.parseLong(uniqueWordStrings[1]));
        byte c;
        StringBuffer sb = new StringBuffer();
        long korpusPos;
        ArrayList<Long> korpusIndexList = new ArrayList<>(400000);

        while (true) {
            try {
                c = uniqueWordsIndex.readByte();
            } catch (EOFException e) {
                korpusPos = Long.parseLong(sb.toString());
                korpusIndexList.add(korpusPos);
                break;
            }
            if (c == '\n') {
                korpusPos = Long.parseLong(sb.toString());
                korpusIndexList.add(korpusPos);
                break;
            } else if (c == ' ') {
                korpusPos = Long.parseLong(sb.toString());
                korpusIndexList.add(korpusPos);
                sb.setLength(0);
            }
            sb.append(c);
        }

        System.out.println("Found " + korpusIndexList.size() + " entries of " + arg);

        boolean answer = true;
        if (korpusIndexList.size() > 25)
            answer = printToUserAndAskForInput();

        if (answer == false) {
            return;
        } else {

            int bytestoread = 60 + arg.length();
            byte[] byteholder = new byte[bytestoread];

            for (Long l : korpusIndexList) {
                korpus.seek(l - 30);
                System.out.println(korpus.getFilePointer());
                korpus.readFully(byteholder, 0, bytestoread);
                for (int j = 0; j < byteholder.length; j++) {
                    if (byteholder[j] == '\n') {
                        byteholder[j] = ' ';
                    }
                }
                System.out.println(new String(byteholder, "ISO-8859-1"));
            }
        }
    }

    private static boolean printToUserAndAskForInput() {
        System.out.print("Found more than 25 entries. Would you like to print them in the console? (y/n): ");
        char input;
        Scanner s = new Scanner(System.in);
        while (true) {
            input = s.next().charAt(0);
            if (input == 'n') {
                return false;
            } else if (input == 'y') {
                return true;
            }
        }
    }

    /**
     * Checks which of the next positions exists in the hashtable
     */
    static Long nextSubstringPos(String substring, Hashtable<String, Long> hashtable,
                                 RandomAccessFile indexfile) throws IOException {
        Long firstPos = hashtable.get(substring);
        Long nextPos = Long.MAX_VALUE;
        //Grab all the values
        Collection<Long> c = hashtable.values();
        boolean foundbigger = false;
        for (Long l : c) { //Check which is the most adjacent bigger position
            if (l > firstPos && l < nextPos) {
                foundbigger = true;
                nextPos = l;
            }
        }
        if (!foundbigger) {
            indexfile.seek(indexfile.length() - 1);
            while (indexfile.read() != '\n') { //eats it
                //cursor is placed one after bin pointer
                indexfile.seek(indexfile.getFilePointer() - 2); //cursor back 2 chars
            }
            nextPos = indexfile.getFilePointer();
        }
        return nextPos;
    }


    static String readTo(RandomAccessFile indexfile, char delim) {
        StringBuilder sb = new StringBuilder(10);
        sb.setLength(0);
        char tempchar = 'a';
        try {
            while ((tempchar = (char) indexfile.read()) != delim) {
                sb.append(tempchar);
            }
        } catch (IOException e) {
            return sb.toString();
        }
        return sb.toString();

    }
}
