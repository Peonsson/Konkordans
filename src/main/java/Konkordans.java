import java.io.*;
import java.util.*;
public class Konkordans implements Serializable {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        /*
            Read hashtable from disk.
            NOTE: will fail if hashtable haven't been previously written to disk.
         */
        if(args.length != 1){
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
        if(arg.length() >= 3){
            substring = arg.substring(0,3);
        }else{
            substring = arg;
        }



        //Get position in indexfile
        Long position = latmanhash.get(substring);
        if(position == null){
            return;
        }

        RandomAccessFile indexfile = new RandomAccessFile("/var/tmp/ut", "r");


        indexfile.seek(position);
        String line;
        String[] strings = null;

        Long lposition = 0l;
        ArrayList<Long> al = new ArrayList<Long>(100);
        boolean foundword = false;
        boolean asked = false; //asked user if it wants all entries


        //Binary search for a position with the correct word
        long upperbound = nextSubstringPos(substring, latmanhash);
        long lowerbound = position;
        long binpointer = lowerbound + (upperbound-lowerbound)/2 ;
        long prevbinpointer = -1l;
        String binpointerWord = null; //the word of line binpointer is at
        char tempchar = ' ';
        boolean foundmatch = false;
        if(upperbound == -1){

        }else{
            while(upperbound != lowerbound){ //while the bounds are not the same
                //position the cursor
                indexfile.seek(binpointer);
                while(indexfile.read() != '\n'){ //eats it
                    //cursor is placed one after bin pointer
                    indexfile.seek(indexfile.getFilePointer()-2); //cursor back 2 chars
                }
                //cursor will be on first char on line
                binpointer = indexfile.getFilePointer();

                //If the binary pointer is the same as the last it means it is stuck
                if(prevbinpointer == binpointer){
                    String tryword = null;
                    while(indexfile.getFilePointer() < upperbound){
                        binpointer = indexfile.getFilePointer();
                        tryword = readTo(indexfile, ' '); //eat first word
                        readTo(indexfile, '\n'); //eat whole line
                        if(arg.equals(tryword)){
                            foundmatch = true;
                            break;
                        }
                    }
                    break;
                }
                prevbinpointer = binpointer;

                //Check the word to a space
                binpointerWord = readTo(indexfile, ' ');
                //if the word equals the argument
                
                int compare = binpointerWord.compareTo(arg);
                if(compare < 0){
                    //if currentword is before arg in index
                    lowerbound = binpointer;
                    binpointer = lowerbound + (upperbound - lowerbound)/2;
                }else if(compare > 0){
                    //if currentword is after arg in index
                    upperbound = binpointer;
                    binpointer = lowerbound + (upperbound - lowerbound)/2;
                }else{
                    foundmatch = true;
                    break;
                }
            }
        }
        System.out.println("Found word?: " + foundmatch);
        indexfile.seek(binpointer);
        System.out.println(binpointerWord);
        System.out.println(indexfile.readLine());
        

        //Getting korpus indexes
//        while((line = indexfile.readLine()) != null){
//            //aaa->aab
//            if(line.substring(0,substring.length()).equals(substring)){
//                strings = line.split(" ");  
//                if(!strings[0].equals(arg)){
//                    if(foundword){
//                        break;
//                    }else{
//                        continue;
//                    }
//                }
//                foundword = true;
//                al.add(Long.parseLong(strings[1]));
//
//                //Check if more than 25
//                if(!asked && al.size() > 25){
//                    System.out.print("Found more than 25 entries. Would you like to print them in the console? (y/n): ");
//                    char input;
//                    Scanner s = new Scanner(System.in);
//                    while(true){
//                        input = s.next().charAt(0);
//                        if(input == 'n'){
//                            return;
//                        }else if(input == 'y'){
//                            asked = true;
//                            break;
//                        }
//                    }
//                }
//            }else{
//                break;
//            }
//        }   
//
        System.out.println("Found " + al.size() + " entries of " + arg);

        int bytestoread = 60 + arg.length();
        byte[] byteholder = new byte[bytestoread];
        RandomAccessFile korpus = new RandomAccessFile("korpus", "r");
        for(Long l: al){
            korpus.seek(l-30);

            korpus.readFully(byteholder, 0, bytestoread);
            for(int i = 0; i < byteholder.length; i++){
                if(byteholder[i] == '\n'){
                    byteholder[i] = ' ';
                }
            }
            System.out.println(new String(byteholder, "ISO-8859-1"));
        }

    }

    /**
     * Checks which of the next positions exists in the hashtable
     *
     */
    static Long nextSubstringPos(String substring, Hashtable<String,Long> hashtable){
        Long firstPos = hashtable.get(substring);
        Long nextPos = Long.MAX_VALUE;
        //Grab all the values
        Collection<Long> c = hashtable.values();
        boolean foundbigger = false;
        for(Long l: c){ //Check which is the most adjacent bigger position
            if(l > firstPos && l < nextPos){
                foundbigger = true;
                nextPos = l;
            }
        }
        if(!foundbigger){
            nextPos = -1l;
        }
        return nextPos;
    }


    static String readTo(RandomAccessFile indexfile, char delim) throws IOException{
        StringBuilder sb = new StringBuilder(10);
        sb.setLength(0);
        char tempchar = 'a';
        while((tempchar = (char) indexfile.read()) != delim){
            sb.append(tempchar);
        }
        return sb.toString();

    }
}
