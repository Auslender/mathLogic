import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ProofGenerator
{
    public static void main (String[] args) {
        int a = new Integer(args[0]);
        int b = new Integer(args[1]);
        try (PrintWriter out = new PrintWriter("compare_proof")) {
            if (a <= b) {
                less(a, b, out);
            } else {
                not_less(a, b, out);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    //a < b
    private static void less(int a, int b, PrintWriter out) throws  IOException {
        String ap = toPeanoNumber(a, "0");
        String bp = toPeanoNumber(b, "0");
        out.println("|-?p(" + ap + "+p=" + bp + ")");
        initPlaceholders("inter_proofs/plus_one", out, "", "", "");
        initPlaceholders("inter_proofs/less_base", out, ap, "", "");
        for (int i = 0; i < b - a; i++) {
            initPlaceholders("inter_proofs/less_induction_step", out, ap, toPeanoNumber(i, "0"), toPeanoNumber(a + i, "0"));
        }
        initPlaceholders("inter_proofs/less_finally", out, ap, bp, toPeanoNumber(b - a, "0"));
    }

    //a >= b
    private static void not_less(int a, int b, PrintWriter out) throws IOException {
        String ap = toPeanoNumber(a, "0");
        String bp = toPeanoNumber(b, "0");
        out.println("|-@p(!(p+" + ap + "=" + bp + "))");
        initPlaceholders("inter_proofs/plus_one", out, "", "", "");
        initPlaceholders("inter_proofs/more_base", out, toPeanoNumber(a - b - 1, "0"), "", "");
        String temp = "(p+" + toPeanoNumber(a - b - 1, "0") + ")\'";
        for (int i = 0; i< b; i++) {
            initPlaceholders("inter_proofs/more_init", out, temp, toPeanoNumber(i, "0"), "");
            temp += "\'";
        }
        initPlaceholders("inter_proofs/more_check", out, toPeanoNumber(a - b - 1, "0"), "", "");
        for (int i = 0; i < b; i++) {
            String temp1 = "(p+" + toPeanoNumber(a - b - 1, "0") + ")'";
            String temp2 = "p+" + toPeanoNumber(a - b - 1, "0") + "'";
            initPlaceholders("inter_proofs/more_induction_step", out, toPeanoNumber(i, temp1), toPeanoNumber(i, temp2), "");
        }
        String temp3 = "(p+" + toPeanoNumber(a - b - 1, "0") + ")";
        String temp4 = "p+" + ap;
        initPlaceholders("inter_proofs/more_finally", out, toPeanoNumber(b + 1, temp3), temp4, bp);
    }

    private static String toPeanoNumber(int a, String str) {
        String peano = str;
        for (int i = 0; i < a; i++) {
            peano += '\'';
        }
        return peano;
    }

    private static void initPlaceholders(String proof, PrintWriter out, String a, String b, String c) throws IOException {
        String str;
        try (BufferedReader in = new BufferedReader(new FileReader(proof))) {
            while ((str = in.readLine()) != null) {
                out.println(str.replace("_A", a).replace("_B", b).replace("_C", c));
            }
        }
    }
}
