/**
 * Created by maria on 22.10.16.
 */
import java.io.*;
import java.util.*;
import pair.Pair;

public class Check {

    private HashMap<String, Expression> map = new HashMap<>();
    private HashMap<String, Integer> done = new HashMap<>();
    private HashMap<String, ArrayList<Pair<Expression, Integer>>> modusPonens = new HashMap<>();

    public String check(Expression proof, int index) throws FileNotFoundException {
        for (int i = 0; i < 10; i++) {
            if (checkAxioms(Main.axioms.get(i), proof)) {
                map.clear();
                addToDone(proof, index);
                return  proof.str + " (Сх. акс. " + (i + 1) + ")";
            }
            map.clear();
        }
        for (int i = 0; i < Main.hypothesis.size(); i++) {
            if (checkHypothesis(Main.hypothesis.get(i), proof)) {
                addToDone(proof, index);
                return proof.str + " (Предп. " + (i + 1) + ")";
            }
        }
        if (modusPonens.containsKey(proof.str)) {
            for (Pair<Expression, Integer> left: modusPonens.get(proof.str)) {
                if (done.containsKey(left.expr.str)) {
                    addToDone(proof, index);
                    return proof.str + " (M.P. " + (done.get(left.expr.str) + 1) + ", " + (left.index + 1) + ")";
                }
            }
        }
        return proof.str + " (Не доказано)";
    }

    private boolean checkAxioms(Expression ax, Expression expr) {
        if (ax.left == null && ax.right == null) {
            if (map.containsKey(ax.str)) {
                return map.get(ax.str).str.equals(expr.str);
            } else {
                map.put(ax.str, expr);
                return true;
            }
        }
        if (ax.op == expr.op) {
            if (ax.op == '!') {
                return checkAxioms(ax.left, expr.left);
            }
            return checkAxioms(ax.left, expr.left) && checkAxioms(ax.right, expr.right);
        }
        return false;
    }

    private boolean checkHypothesis(Expression hyp, Expression expr) {
        if (hyp.left == null && hyp.right == null) {
            return hyp.str.equals(expr.str);
        }
        if (hyp.op == expr.op) {
            if (hyp.op == '!') {
                return checkHypothesis(hyp.left, expr.left);
            }
            return checkHypothesis(hyp.left, expr.left) && checkHypothesis(hyp.right, expr.right);
        }
        return false;
    }

    private void addToDone(Expression proof, int index) {
        done.put(proof.str, index);
        if (proof.op == '-') {
            if (!modusPonens.containsKey(proof.right.str)) {
                modusPonens.put(proof.right.str, new ArrayList<>());
            }
            modusPonens.get(proof.right.str).add(new Pair<>(proof.left, index));
        }
    }
}


