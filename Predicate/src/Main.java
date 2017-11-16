import parse_check.Expression;
import parse_check.ExpressionParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static ExpressionParser parser = new ExpressionParser();
    private static List<Expression> hypotheses = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        DeductionGenerator generator;
        Expression answer;
        Expression lastGuess;
        if (args.length < 2) {
            System.out.println("Pass /input_file/ and /output_file/ as arguments");
        }

        long start = System.currentTimeMillis();
        try (PrintWriter fileWriter = new PrintWriter(args[1])) {

            Scanner in = new Scanner(new File(args[0]));
            String tmpStr = in.nextLine();
            if (tmpStr.contains("|-") && !tmpStr.split("\\|-")[0].equals("")) {
                int i = 0, balance = 0;
                String expr = "";
                while (!tmpStr.substring(i, i + 2).equals("|-")) {
                    if (tmpStr.charAt(i) == ',' && balance == 0) {
                        hypotheses.add(parser.parse(expr));
                        expr = "";
                    } else {
                        if (tmpStr.charAt(i) == '(')
                            balance++;
                        if (tmpStr.charAt(i) == ')')
                            balance--;
                        expr += tmpStr.charAt(i);
                    }
                    i++;
                }
                lastGuess = (parser.parse(expr));

                answer = parser.parse(tmpStr.split("\\|-")[1]);
                generator = new DeductionGenerator(hypotheses, lastGuess, fileWriter, answer);
            } else {
                if (tmpStr.contains("|-"))
                    generator = new DeductionGenerator(fileWriter, parser.parse(tmpStr.split("\\|-")[1]));
                else
                    generator = new DeductionGenerator(fileWriter, parser.parse(tmpStr));
            }

            System.out.println("Processing proof from file : " + args[0]);
            while (in.hasNext()) {
                generator.add(parser.parse(in.nextLine()));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (WrongProofException e) {
            try (FileWriter fileWriter = new FileWriter(args[1], false)) {
                fileWriter.write(e.getMessage());
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("The output is written to : " + args[1]  +  "\nTime: " + (end - start) + "ms");
    }
}
