import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by maria on 09.04.17.
 */
public class ResourceHandler {

    private BufferedReader in;

    private static Expression alpha;
    private static Expression betta;

    private static ParseExpression parser = new ParseExpression();


    ResourceHandler(String inputFileString) throws IOException {
        this.in = new BufferedReader(new FileReader(inputFileString));
    }

    public ArrayList<Expression> getAxioms() throws IOException {
        ArrayList<Expression> axioms = new ArrayList<>();
        Scanner in= new Scanner(new File("solid/axioms"));
        while (in.hasNextLine()) {
            axioms.add(parser.impl(in.nextLine()));
        }
        return axioms;
    }

    public ArrayList<Expression> getHypothesis() throws IOException {
        String title = in.readLine().replace(" ", "");
        if (title.contains("|-")) {
            ArrayList<Expression> hypothesis = new ArrayList<>();
            betta = parser.impl(title.substring(title.indexOf("|-") + 2));

            String s = title;
            int balance = 0;
            int i = 0;
            while (i < s.indexOf("|-")) {
                if (s.charAt(i) == '(') balance++;
                else if (s.charAt(i) == ')') balance--;

                if (s.charAt(i) == ',' && balance == 0) {
                    hypothesis.add(parser.impl(s.substring(0, i)));
                    s = s.substring(i + 1);
                    i = -1;
                }
                i++;
            }
            alpha = parser.impl(s.substring(0, s.indexOf("|-")));
            return hypothesis;
        }
        return null;
    }

    public Expression getAlpha() {
        return alpha;
    }

    public Expression getBetta() {
        return betta;
    }

    public String readLine() throws IOException {
        return in.readLine().replace(" ", "");
    }

}
