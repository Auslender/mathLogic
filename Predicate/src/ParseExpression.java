import java.util.ArrayList;

/**
 * Created by maria on 03.01.17.
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
            expr = new Expression(left, right, "->");
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
            Expression right = and(expr.notParsed.substring(1));
            expr = new Expression(left, right, "|");
        }
        return expr;
    }

    private Expression and(String s) {
        Expression expr = unary(s);
        while(!expr.notParsed.isEmpty()) {
            if (expr.notParsed.charAt(0) != '&') {
                return expr;
            }
            Expression left = expr;
            Expression right = unary(expr.notParsed.substring(1));
            expr = new Expression(left, right, "&");
        }
        return expr;
    }

    private Expression unary(String s) {
        if (s.charAt(0) == '?' || s.charAt(0) == '@') {
            return quantifier(s);
        }
        if (s.charAt(0) == '!') {
            return neg(s);
        }
        if (s.charAt(0) == '(') {
            return getFullPredicate(s);
        }
        return predicate(s);
    }

    private Expression quantifier(String s) {
        Expression expr = new Expression();
        expr.op = Character.toString(s.charAt(0));
        expr.left = getVar(s.substring(1));
        expr.right = unary(expr.left.notParsed);
        expr.str = "(" + expr.op + expr.left.str + expr.right.str + ")";
        expr.notParsed = expr.right.notParsed;

        expr.free.addAll(expr.right.free);
        expr.free.remove(expr.left.str);
        return expr;
    }

    private Expression neg(String s) {
        Expression expr = new Expression();
        expr.op = "!";
        expr.left = unary(s.substring(1));
        expr.str = "(" + expr.op + expr.left.str + ")";
        expr.notParsed = expr.left.notParsed;

        expr.free = expr.left.free;
        return expr;
    }

    //checking expression in parenthesis
    private Expression getFullPredicate(String s) {
        int pos = getParenthesis(s, 1);
        int i = pos;
        int balance = 0;
        while (i < s.length()) {
            if (s.charAt(i) == '(') balance++;
            else if (s.charAt(i) == ')') balance--;
            Character c = s.charAt(i);
            //Ready to be full predicate? If not yet, then split again
            if (c != '\'' && c != '+' && c != '*' && c != '(' && c != ')' && c != '=' && !(c >= '0' && c <= '9') && !(c >= 'a' && c <= 'z')) {
                break;
            }
            //Ready to be predicate <term> ‘=’ <term>?
            if (c == '=' && balance == 0) {
                return predicate(s);
            }
            i++;
        }
        Expression expr = impl(s.substring(1, pos - 1));
        expr.notParsed = s.substring(pos);
        return expr;
    }


    private Expression predicate(String s) {
        Expression expr = new Expression();
        //(‘A’ . . . ‘Z’) {‘0’ . . . ‘9’}∗ [‘(’<term> {‘,’ <term>}∗ ‘)’]
        if (s.charAt(0) >= 'A' && s.charAt(0) <= 'Z' || (s.charAt(0) > 48 && s.charAt(0) < 58)) {
            return getTerms(s);
        }
        //<term> ‘=’ <term>
        expr.op = "=";
        expr.left = term(s);
        expr.right = term(expr.left.notParsed.substring(1));
        expr.str =  "(" + expr.left.str + expr.op + expr.right.str + ")";
        expr.notParsed = expr.right.notParsed;

        expr.free.addAll(expr.left.free);
        expr.free.addAll(expr.right.free);
        return expr;
    }

    private Expression term(String s) {
        Expression expr = addend(s);
        while (expr.notParsed.length() != 0) {
            if (expr.notParsed.charAt(0) != '+') {
                return expr;
            }
            Expression left = expr;
            Expression right = addend(expr.notParsed.substring(1));
            expr = new Expression(left, right, "+");
        }
        return expr;
    }

    private Expression addend(String s) {
        Expression expr = multiplicand(s);
        while (expr.notParsed.length() != 0) {
            if (expr.notParsed.charAt(0) != '*') {
                return expr;
            }
            Expression left = expr;
            Expression right = multiplicand(expr.notParsed.substring(1));
            expr = new Expression(left, right, "*");
        }
        return expr;
    }

    private Expression multiplicand(String s) {
        Expression expr = new Expression();
        //‘(’ <term> ‘)’
        if (s.charAt(0) == '(') {
            expr = term(s.substring(1));
            expr.notParsed = expr.notParsed.substring(1);
        } else if (s.charAt(0) == '0') {
            expr.str = "0";
            expr.notParsed = s.substring(1);
        } else {
            //(‘a’ . . . ‘z’) {‘0’ . . . ‘9’}∗ ‘(’<term> {‘,’ <term>}∗ ‘)’ or variable
            expr = getTerms(s);
            if (expr.terms == null) {
                expr.free.add(expr.str);
            }
        }
        //multiplicand'
//        int i = 0;
//        while (i < expr.notParsed.length() && expr.notParsed.charAt(i) == '\'') {
//            ++i;
//            Expression tmp = new Expression();
//            tmp.left = expr;
//            tmp.notParsed = expr.notParsed;
//            tmp.str = expr.str + "'";
//            tmp.free = expr.free;
//            tmp.op = "'";
//            expr = tmp;
//        }
//        expr.notParsed = expr.notParsed.substring(i);
        return apostrophe(expr);
    }

    private Expression apostrophe(Expression expr) {
        int i = 0;
        while (i < expr.notParsed.length() && expr.notParsed.charAt(i) == '\'') {
            ++i;
            Expression tmp = new Expression();
            tmp.left = expr;
            tmp.notParsed = expr.notParsed;
            tmp.str = expr.str + "'";
            tmp.free = expr.free;
            tmp.op = "'";
            expr = tmp;
        }
        expr.notParsed = expr.notParsed.substring(i);
        return expr;
    }

    private Expression getTerms(String s) {
        Expression expr = new Expression();
        int pos = getFigures(s, 1);
        expr.op = s.substring(0, pos);
        expr.str = expr.op;
        expr.notParsed = s.substring(pos);
        if (pos < s.length() && s.charAt(pos) == '(') {
            expr.terms = new ArrayList<>();
            do {
                expr.terms.add(term(expr.notParsed.substring(1)));
                expr.free.addAll(expr.terms.get(expr.terms.size() - 1).free);
                expr.notParsed = expr.terms.get(expr.terms.size() - 1).notParsed;
            } while (expr.notParsed != null && expr.notParsed.charAt(0) == ',');
            expr.notParsed = expr.notParsed.substring(1);
            expr.str += "(";
            for (int i = 0; i < expr.terms.size() - 1; i++) {
                expr.str += expr.terms.get(i).str + ",";
            }
            expr.str += expr.terms.get(expr.terms.size() - 1).str + ")";
        }
        return expr;
    }

    private Expression getVar(String s) {
        Expression expr = new Expression();
        int pos = getFigures(s, 1);
        expr.str = s.substring(0, pos);
        expr.notParsed = s.substring(pos);
        return expr;
    }


    private int getParenthesis(String s, int pos) {
        int balance = 1;
        for (;balance != 0; pos++) {
            if (s.charAt(pos) == '(') balance++;
            else if (s.charAt(pos) == ')') balance--;
        }
        return pos;
    }

    private int getFigures(String s, int pos) {
        while (pos < s.length() && (s.charAt(pos) >= '0' && s.charAt(pos) <= '9')) {
            pos++;
        }
        return pos;
    }
}
