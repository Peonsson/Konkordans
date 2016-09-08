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

        //TODO: implement the actual search logic: java Konkordans <word>

        String arg = args[0];
        String substring = arg.substring(0,3);
        System.out.println(substring);
        Long position = latmanhash.get(substring);

        RandomAccessFile indexfile = new RandomAccessFile("index", "r");

        indexfile.seek(position);
        String line;
        String[] strings = null;

        Long lposition = 0l;
        ArrayList<Long> al = new ArrayList<Long>(100);

        //Getting korpus indexes
        while((line = indexfile.readLine()) != null){
            strings = line.split(" ");  
            if(!strings[0].equals(arg)){
                break;
            }
            al.add(Long.parseLong(strings[1]));
        }   

        System.out.println("Found " + al.size() + " entries of " + arg);

        //Check if more than 25
        if(al.size() > 25){
            System.out.print("Found more than 25 entries. Would you like to print them in the console? (y/n): ");
            char input;
            Scanner s = new Scanner(System.in);
            while(true){
                input = s.next().charAt(0);
                if(input == 'n'){
                    return;
                }else if(input == 'y'){
                    break;
                }
            }
        }
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
}
