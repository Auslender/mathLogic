package parse_check;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {
    private char[] levels = {'>', '|', '&', '!', '+', '*'};
    private String str;

    public Expression parse(String str1) {
        str = str1.replaceAll(" ", "");
        return impl(0, str.length());

    }

    private Expression impl(int l, int r) {//0
        int balance = 0;
        for (int i = l; i < r; i++) {
            if (str.charAt(i) == ')')
                balance--;
            if (str.charAt(i) == '(')
                balance++;
            if (str.charAt(i) == '>' && balance == 0)
                return new Expression("->", next(l, i - 1, 1), impl(i + 1, r));
        }
        return next(l, r, 1);
    }

    private Expression next(int l, int r, int level) {
        if (level == 3)
            return unary(l, r);
        if (level == 6)
            return variable(l, r);
        int balance = 0;
        for (int i = r - 1; i >= l; i--) {
            if (str.charAt(i) == ')')
                balance--;
            if (str.charAt(i) == '(')
                balance++;
            if (str.charAt(i) == levels[level] && balance == 0)
                return new Expression("" + str.charAt(i), next(l, i, level), next(i + 1, r, level + 1));
        }
        return next(l, r, level + 1);

    }

    private Expression unary(int l, int r) {//3
        if (str.charAt(l) == '!')
            return new Expression("!", unary(l + 1, r));
        if (str.charAt(l) == '@' || str.charAt(l) == '?') {
            String tmp = "";
            int i = l + 1;
            while (isFromVar(str.charAt(i))) {
                tmp += str.charAt(i);
                i++;
            }
            return new Expression("" + str.charAt(l), new Expression(tmp), unary(i, r));
        }
        if (str.charAt(l) == '(') {
            int balance = 0;
            for (int i = l; i < r - 1; i++) {
                if (str.charAt(i) == '(')
                    balance++;
                if (str.charAt(i) == ')')
                    balance--;
                if ((str.charAt(i) == ')') && balance == 0)
                    return predicate(l, r);
            }
            return impl(l + 1, r - 1);
        }

        return predicate(l, r);
    }

    private Expression predicate(int l, int r) {
        String predS = "";
        int i = l;
        if (isFromPred(str.charAt(i), true)) {
            while (i != r && isFromPred(str.charAt(i), false)) {
                predS += str.charAt(i);
                i++;
            }
            if (i != r && str.charAt(i) == '(') {
                i++;
                r--;
            }
            List<Expression> tmpList = new ArrayList<>();
            int j, balance = 0;
            for (j = i; j != r + 1; j++) {
                if (j == r) {
                    tmpList.add(next(i, j, 4));
                    i = j + 1;
                    continue;
                }
                if (str.charAt(j) == ')') balance++;
                if (str.charAt(j) == '(') balance--;
                if (str.charAt(j) == ',' && balance == 0) {
                    tmpList.add(next(i, j, 4));
                    i = j + 1;
                }
            }
            return new Expression(predS, tmpList);//predicate P(...)
        } else {
            for (int j = l; j < r; j++)
                if (str.charAt(j) == '=')
                    return new Expression("=", next(l, j, 4), next(j + 1, r, 4));
        }
        return null;

    }

    private Expression variable(int l, int r) {//6
        if (l == r)
            return new Expression(str.substring(l, r));
        if (str.charAt(r - 1) == '\'')
            return new Expression("\'", variable(l, r - 1));
        if (str.charAt(l) == '0')
            return new Expression("0");
        if (str.charAt(l) == '(')
            return next(l + 1, r - 1, 4);
        if (str.charAt(r - 1) == ')') {
            String varS = "";
            int i = l;
            while (isFromVar(str.charAt(i))) {
                varS += str.charAt(i);
                i++;
            }
            if (i != r && str.charAt(i) == '(') {
                i++;
                r--;
            }
            List<Expression> tmpList = new ArrayList<>();
            int j, balance = 0;
            for (j = i; j != r + 1; j++) {
                if (j == r) {
                    tmpList.add(next(i, j, 4));
                    i = j + 1;
                    continue;
                }
                if (str.charAt(j) == ')') balance++;
                if (str.charAt(j) == '(') balance--;
                if (str.charAt(j) == ',' && balance == 0) {
                    tmpList.add(next(i, j, 4));
                    i = j + 1;
                }
            }
            return new Expression(varS, tmpList);// p(...)

        }
        return new Expression(str.substring(l, r));
    }

    private boolean isFromVar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
    }

    private boolean isFromPred(char c, boolean first) {
        if (first)
            return (c >= 'A' && c <= 'Z');
        return (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }

}
