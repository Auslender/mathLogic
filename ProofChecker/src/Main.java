/**
 * Created by maria on 20.10.16.
 */
import java.io.*;
import java.util.*;

public class Main {
    private static BufferedReader in;
    private static PrintWriter out;

    public static ArrayList<Expression> hypothesis;
    public static Expression statement;
    public static ArrayList<Expression> axioms;
    public static ArrayList<Expression> proof;
    private static String input;

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();

        in = new BufferedReader(new FileReader("test3.in"));
        out = new PrintWriter(new File("proof.out"));

        new Main().read();

        Check checkExpr = new Check();
        out.println(input);
        int i = 1;
        for (Expression p: proof) {
            out.println("(" + i + ") " + checkExpr.check(p, i - 1));
            i++;
        }
        out.close();

        System.out.println(System.currentTimeMillis() - startTime + " ms");
    }

    private void read() throws IOException {
        ParseExpression parse = new ParseExpression();

        input = in.readLine().replace(" ", "");
        String s;
        hypothesis = new ArrayList<>();
        if (input.charAt(0) == '|') {
            s = input.substring(2);
        } else {
            String[] h = input.split(",");
            String[] last = h[h.length - 1].split("\\|-");
            s = last[1];
            h[h.length - 1] = last[0];

            for (int i = 0; i < h.length; i++) {
                hypothesis.add(parse.impl(h[i]));
            }
        }

        statement = parse.impl(s);

        axioms = new ArrayList<>();
        Scanner in1 = new Scanner(new File("axioms.txt"));
        while (in1.hasNextLine()) {
            axioms.add(parse.impl(in1.nextLine()));
        }

        proof = new ArrayList<>();
        while (in.ready()) {
            proof.add(parse.impl(in.readLine().replace(" ", "")));
        }
    }
}
