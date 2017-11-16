package parse_check;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Checker {
    private Map<String, String> map;

    public boolean matchesScheme(Expression expr, Expression axiom) {
        map = new HashMap<>();
        return checkAxioms(expr, axiom);
    }

    private boolean checkAxioms(Expression expr, Expression axiom) {
        if ((axiom.n == 0 && !axiom.str.equals("!")) || (axiom.n != 0 && axiom.children.get(0).str.equals(""))) {
            map.putIfAbsent(axiom.parsed, expr.parsed);
            return map.get(axiom.parsed).equals(expr.parsed);
        }
        if (expr.str.equals(axiom.str) && expr.children.size() == axiom.children.size()) {
            for (int i = 0; i < expr.children.size(); i++) {
                if (!checkAxioms(expr.children.get(i), axiom.children.get(i)))
                    return false;
            }
            return true;
        }
        return false;
    }

    private Set<String> getFreeVariables(Expression oper) {
        return oper.free;
    }

    public Integer checkFreeUnderQuantifier(Expression quantor, Expression nw) {
        targetChange = null;
        if (!(quantor.str.equals("@") || quantor.str.equals(("?"))))
            return -1;
        if (!checkEqual(quantor.children.get(1), nw, quantor.children.get(0).str))
            return -1;
        if (targetChange == null)
            return 1;
        if (checkFree(quantor.children.get(1), quantor.children.get(0), getFreeVariables(targetChange)))
            return 1;
        return 0;
    }

    private boolean checkFree(Expression place, Expression guider, Set<String> comparings) {
        if (!getFreeVariables(place).contains(guider.str))
            return true;
        if (guider.isEqualTo(place)) {
            for (String tmpStr : comparings) {
                if (place.bounded.contains(tmpStr))
                    return false;
            }
            return true;
        }
        if (place.str.equals("@") && place.str.equals("?"))
            return checkFree(place.children.get(1), guider, comparings);
        boolean tmpBool = true;
        for (Expression oper : place.children) {
            if (!checkFree(oper, guider, comparings))
                tmpBool = false;
        }
        return tmpBool;
    }

    public Integer quantifierFreeUnique(Expression expr) {
        Expression psi0 = expr.children.get(0).children.get(1).children.get(1).children.get(0);
        Expression psi1 = expr.children.get(0).children.get(0);
        Expression psi2 = expr.children.get(0).children.get(1).children.get(1).children.get(1);
        Expression psi3 = expr.children.get(1);
        String targetU = expr.children.get(0).children.get(1).children.get(0).str;
        targetChange = new Expression("0");
        if (!checkEqual(psi0, psi1, targetU))
            return -1;
        targetChange = new Expression("(" + targetU + ")\'");
        if (!checkEqual(psi0, psi2, targetU))
            return -1;
        targetChange = null;
        if (!checkEqual(psi0, psi3, targetU)) {
            return -1;
        }
        return 0;
    }

    public Expression targetChange;

    public boolean checkEqual(Expression origin, Expression change, String target) {
        if (origin.str.equals(target)) {
            if (targetChange == null)
                targetChange = change;
            return targetChange.toString().equals(change.toString());
        } else {
            if (!origin.str.equals(change.str))
                return false;
            for (int i = 0; i < origin.n; i++) {
                if (!checkEqual(origin.children.get(i), change.children.get(i), target))
                    return false;
            }
            return true;
        }
    }

}