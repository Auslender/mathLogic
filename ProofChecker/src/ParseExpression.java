/**
 * Created by maria on 21.10.16.
 */

public class ParseExpression {

    Expression impl(String s) {
        Expression expr = or(s);
        while (!expr.notParsed.isEmpty()) {
            if (expr.notParsed.charAt(0) != '-') {
                return expr;
            }
            Expression left = expr;
            Expression right = impl(expr.notParsed.substring(2));
            expr = new Expression();
            expr.left = left;
            expr.right = right;
            expr.op = '-';
            expr.str = "(" + expr.left.str + "->" + expr.right.str + ")";
            expr.notParsed = expr.right.notParsed;
        }
        return expr;
    }

    private Expression or(String s) {
        Expression expr = and(s);
        while(!expr.notParsed.isEmpty()) {
            if (expr.notParsed.charAt(0) != '|') {
                return expr;
            }
            Expression left = expr;
            Expression right = parentheses(expr.notParsed.substring(1));
            expr = new Expression();
            expr.left = left;
            expr.right = right;
            expr.op = '|';
            expr.str ="(" + expr.left.str + expr.op + expr.right.str + ")";
            expr.notParsed = expr.right.notParsed;
        }
        return expr;
    }

    private Expression and(String s) {
        Expression expr = parentheses(s);
        while(!expr.notParsed.isEmpty()) {
            if (expr.notParsed.charAt(0) != '&') {
                return expr;
            }
            Expression left = expr;
            Expression right = parentheses(expr.notParsed.substring(1));
            expr = new Expression();
            expr.left = left;
            expr.right = right;
            expr.op = '&';
            expr.str ="(" + expr.left.str + expr.op + expr.right.str + ")";
            expr.notParsed = expr.right.notParsed;
        }
        return expr;
    }

    private Expression parentheses(String s) {
        if (s.charAt(0) == '(') {
            int i = 0, balance = 1;
            while (balance != 0) {
                i++;
                if (s.charAt(i) == '(') {
                    balance++;
                } else if (s.charAt(i) == ')') {
                    balance--;
                }
            }
            Expression expr = impl(s.substring(1, i));
            expr.notParsed = s.substring(i + 1);
            return expr;
        }
        return neg(s);
    }

    private Expression neg(String s) {
        if (s.charAt(0) == '!') {
            Expression expr = new Expression();
            expr.left = parentheses(s.substring(1));
            expr.str = "(" + "!" + expr.left.str + ")";
            expr.op = '!';
            expr.notParsed = expr.left.notParsed;
            return expr;
        }
        return splitVariable(s);
    }

    private Expression splitVariable(String s) {
        Expression expr = new Expression();
        int i = 1;
        while (i < s.length() && ((s.charAt(i) > 47 && s.charAt(i) < 58) || (s.charAt(i) > 64 && s.charAt(i) < 91))) {
            i++;
        }
        expr.str = s.substring(0, i);
        expr.notParsed = s.substring(i);
        return expr;
    }
}
