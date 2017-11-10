/**
 * Created by maria on 03.01.17.
 */
import java.io.*;
import java.util.HashSet;


public class Main {

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        PredicateCalculusProofChecker checker = new PredicateCalculusProofChecker(args[0], args[1]);
        System.out.println("Processing proof from file : " + args[0]);
        checker.generateFullProofOrError();
        long end = System.currentTimeMillis();
        System.out.println("The output is written to : " + args[1]  +  "\nTime: " + (end - start) + "ms");
        System.out.println("-------------------------------------------------------------------------");

//        File dir = new File("proofs_in/");
//        File[] dirFiles = dir.listFiles();
//        if (dirFiles != null) {
//            for (File file : dirFiles) {
//                PredicateCalculusProofChecker checker = new PredicateCalculusProofChecker();
//            }
//        }
    }
}
