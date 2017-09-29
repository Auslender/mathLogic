import java.io.*;
import java.util.*;

/**
 * Created by maria on 05.01.17.
 */
public class PredicateCalculusProofChecker {

    private static int lastErrorCode = 0;

    private ArrayList<Expression> axioms = new ArrayList<>();
    private ArrayList<Expression> hypothesis = new ArrayList<>();
    private Expression alpha;
    private Expression betta;

    private HashMap<String, Expression> map = new HashMap<>();

    private static ParseExpression parser = new ParseExpression();
    private static ResourceHandler resourceHandler;
    private PrintWriter out;

    private JustificationState state;

    PredicateCalculusProofChecker(String inputFilePath, String outputFilePath) throws IOException {
        resourceHandler = new ResourceHandler(inputFilePath);
        this.axioms = resourceHandler.getAxioms();
        this.hypothesis = resourceHandler.getHypothesis();
        this.alpha = resourceHandler.getAlpha();
        this.betta = resourceHandler.getBetta();

        out = new PrintWriter(new File(outputFilePath));
    }

    public void generateFullProofOrError() throws IOException {

        ArrayList<String> output = deduction();
        if (output != null) {
            for (String line : output) {
                out.println(line);
            }
        }
        out.close();
    }

    private int checkAxioms(Expression expr) {

        //isAxiom 1-10
        for (int i = 0; i < 18; i++) {
            if (isAxiom(axioms.get(i), expr)) {
                map.clear();
                state = new JustificationState(State.AXIOM);
                return i + 1;
            }
            map.clear();
        }

        boolean ok;

        //axiom11 ∀x(ψ)->(ψ[x := θ])
        try {
            lastErrorCode = 0;
            ok = true;
            ok &= (expr.op.equals("->") && expr.left.op.equals("@"));
            trySubstitute(expr.left.right, expr.right, expr.left.left.str);
            if (ok && correct) {
                state = new JustificationState(State.AXIOM);
                return 11;
            }
            if (lastErrorCode == 1) {
                state = new JustificationState(State.ERROR, 1, forSubst, expr.left.right, expr.left.left.str);
            }
        } catch (Exception e) {}

        //axiom12 (ψ[x := θ]) → ∃x(ψ)
        try {
            lastErrorCode = 0;
            ok = true;
            ok &= (expr.op.equals("->") && expr.right.op.equals("?"));
            trySubstitute(expr.right.right, expr.left, expr.right.left.str);
            if (ok && correct) {
                state = new JustificationState(State.AXIOM);
                return 12;
            }
            if (lastErrorCode == 1) {
                state = new JustificationState(State.ERROR,1, forSubst, expr.left.right, expr.left.left.str);
            }
        } catch (Exception e) {}

        //induction (ψ[x := 0])&∀x((ψ) → (ψ)[x := x']) → (ψ)
        try {
            ok = true;
            ok &= (expr.op.equals("->") && expr.left.op.equals("&") && expr.left.right.op.equals("@")
            && expr.left.right.right.op.equals("->"));
            Expression psi0 = expr.left.left;
            Expression psi1 = expr.left.right.right.left;
            Expression psi2 = expr.left.right.right.right;
            Expression psi3 = expr.right;

            String x = expr.left.right.left.str;
            if (ok && psi1.str.equals(psi3.str) && trySubstitute(psi1, psi0, x) &&
                    forSubst.str.equals("0") && trySubstitute(psi1, psi2, x) && forSubst.str.equals(x + "'")) {
                state = new JustificationState(State.AXIOM);
                return 13;
            }
        } catch (Exception e) {}

        for (Expression hyp : hypothesis) {
            if (hyp.str.equals(expr.str)) {
                state = new JustificationState(State.HYPOTHESIS);
                return 22;
            }
        }

        return -1;
    }

    private boolean isAxiom(Expression ax, Expression expr) {
        if (ax.left == null && ax.right == null) {
            if (map.containsKey(ax.str)) {
                return map.get(ax.str).str.equals(expr.str);
            } else {
                map.put(ax.str, expr);
                return true;
            }
        }
        if (ax.op.equals(expr.op)) {
            if (ax.op.equals("!") || ax.op.equals("'")) {
                return isAxiom(ax.left, expr.left);
            }
            return isAxiom(ax.left, expr.left) && isAxiom(ax.right, expr.right);
        }
        return false;
    }

    private Expression forSubst = null;
    private boolean correct = true;

    private boolean trySubstitute(Expression expr, Expression theta, String x) {
        forSubst = null;
        correct = true;

        try {
            dfs(expr, theta, x);
        } catch (Exception e) {
            correct = false;
        }
        if (correct && (forSubst != null || expr.str.equals(theta.str))) {
            return true;
        }
        correct = false;
        return false;
    }

    private void dfs(Expression expr, Expression theta, String x) {
        if (!expr.free.contains(x)) {
            return;
        }
        if (expr.left != null) {
            if (expr.left.free.contains(x)) {
                dfs(expr.left, theta.left, x);
            } else if (!expr.left.str.equals(theta.left.str)) {
                throw null;
            }
            if (expr.right != null) {
                if (expr.right.free.contains(x)) {
                    dfs(expr.right, theta.right, x);
                } else if (!expr.right.str.equals(theta.right.str)) {
                    throw  null;
                }
            }
            if (expr.op.equals("@") || expr.op.equals("?")) {
                if (!theta.free.containsAll(forSubst.free)) {
                    lastErrorCode = 1;
                    throw null;
                }
            }
        }
        if (expr.terms != null) {
            for (int i = 0; i < expr.terms.size(); i++) {
                if (expr.terms.get(i).free.contains(x)) {
                    dfs(expr.terms.get(i), theta.terms.get(i), x);
                } else if (!expr.terms.get(i).str.equals(theta.terms.get(i).str)) {
                    throw null;
                }
            }
        }
        if (expr.op.equals(x) && expr.terms == null) {
            if (forSubst == null) {
                forSubst = theta;
            } else if (!forSubst.str.equals(theta.str)) {
                correct = false;
                throw null;
            }
        }
    }

    public ArrayList<String> deduction() {
        ArrayList<String> res = new ArrayList<>();
        ArrayList<Expression> proved = new ArrayList<>();
        String s;
        Expression expr;
        int lineNum = -1;
        String header = "";
        for (Expression h : hypothesis) {
            header = header.concat(h.str);
        }
        header = header.concat("|-").concat(alpha.str).concat("->").concat(betta.str);
        res.add(header);
        try {
            while(true) {
                s = resourceHandler.readLine();
                lineNum++;
                expr = parser.impl(s);
                proved.add(expr);
                int axiom = checkAxioms(expr);
                boolean prove = (axiom > 0);

                ArrayList<Expression> boundedArgs= new ArrayList<>();
                //if expr is an axiom or hypothesis
                if (prove) {
                    res.add(expr.str);
                    if (alpha != null) {
                        boundedArgs.add(expr);
                        boundedArgs.add(alpha);
                        res.addAll(getProofPart("lemma_template/axiom_or_hypothesis", boundedArgs));
                    }
                }

                //if expr equals alpha
                if (!prove && alpha != null & alpha.str.equals(expr.str)) {
                    boundedArgs.clear();
                    boundedArgs.add(expr);
                    Expression temp = new Expression(expr, expr, "->");
                    boundedArgs.add(temp);
                    res.addAll(getProofPart("lemma_template/alpha->alpha", boundedArgs));
                    prove = true;
                }

                //if expr is MP
                if (!prove) {
                    boundedArgs.clear();
                    boundedArgs.add(alpha);
                    String left;
                    for (int i = proved.size() - 2; i >= 0 && !prove; i--) {
                        if (proved.get(i).left == null || proved.get(i).right == null) continue;;
                        left = proved.get(i).left.str;
                        if (proved.get(i).op.equals("->") && proved.get(i).right.str.equals(expr.str)) {
                            for (int j = proved.size() - 1; j >= 0 && !prove; j--) {
                                    if (proved.get(j).str.equals(left)) {
                                        boundedArgs.add(proved.get(i).left);
                                        boundedArgs.add(proved.get(i));
                                        boundedArgs.add(expr);
                                        if (alpha != null) {
                                            res.addAll(getProofPart("lemma_template/modus_ponens", boundedArgs));
                                        } else {
                                        res.add(expr.str);
                                    }
                                    state = new JustificationState(State.MP);
                                    prove = true;
                                }
                            }
                        }
                    }
                }

                //1st predicate rule FORALL
                if (!prove && expr.op.equals("->") && expr.right.op.equals("@")) {
                    for(int i = proved.size() - 1; i >= 0 && !prove; i--) {
                        if (proved.get(i).left == null || proved.get(i).right == null) {
                            continue;
                        }
                        if (proved.get(i).left.str.equals(expr.left.str) && proved.get(i).right.str.equals(expr.right.right.str)) {
                            if (expr.left.free.contains(expr.right.left.str)) {
                                state = new JustificationState(State.ERROR,2, null, expr.left, expr.right.left.str);
                            } else if (alpha != null && alpha.free.contains(expr.right.left.str)) {
                                state = new JustificationState(State.ERROR,3, null, alpha, expr.right.left.str);
                            } else {
                                if (alpha != null) {
                                    boundedArgs.clear();
                                    boundedArgs.add(alpha);
                                    boundedArgs.add(expr.left);
                                    boundedArgs.add(expr.right.right);
                                    res.addAll(getProofPart("lemma_template/ImplToConj", boundedArgs));
                                    res.add(parser.impl("1&2->3").bind(boundedArgs).str);
                                    boundedArgs.set(2, expr.right);
                                    res.add(parser.impl("1&2->3").bind(boundedArgs).str);
                                    res.addAll(getProofPart("lemma_template/ConjToImpl", boundedArgs));
                                }
                                state = new JustificationState(State.ANY_RULE);
                                prove = true;
                            }
                        }
                    }
                }

                //2nd predicate rule. EXIST
                if (!prove && expr.op.equals("->") && expr.left.op.equals("?")) {
                    for(int i = proved.size() - 1; i >= 0 && !prove; i--) {
                        if (proved.get(i).left == null || proved.get(i).right == null) {
                            continue;
                        }
                        if (proved.get(i).left.str.equals(expr.left.right.str) && proved.get(i).right.str.equals(expr.right.str)) {
                            if (expr.left.free.contains(expr.left.left.str)) {
                                state = new JustificationState(State.ERROR,2, null, expr.right, expr.left.left.str);
                            } else if (alpha != null && alpha.free.contains(expr.left.left.str)) {
                                state = new JustificationState(State.ERROR,3, null, alpha, expr.right.left.str);
                            } else {
                                if (alpha != null) {
                                    boundedArgs.clear();
                                    boundedArgs.add(alpha);
                                    boundedArgs.add(expr.left.right);
                                    boundedArgs.add(expr.right);
                                    res.addAll(getProofPart("lemma_template/ImplLemma", boundedArgs));
                                    res.add(parser.impl("2->1->3").bind(boundedArgs).str);
                                    boundedArgs.clear();
                                    boundedArgs.add(expr.left);
                                    boundedArgs.add(alpha);
                                    boundedArgs.add(expr.right);
                                    res.add(parser.impl("1->2->3").bind(boundedArgs).str);
                                    res.addAll(getProofPart("lemma_template/ImplLemma", boundedArgs));
                                }
                                state = new JustificationState(State.EXIST_RULE);
                                prove = true;
                            }
                        }
                    }
                }
                if (!prove) {
                    out.println("Вывод некорректен начиная с формулы номер " + (lineNum + 1));
                    switch (state.error.errorCode) {
                        case 1:
                            out.println(": терм " + state.error.errorTerm.str + " не свободен для подстановки в формулу " + state.error.errorExpression.str + " вместо переменной " + state.error.errorVar + ".");
                            break;
                        case 2:
                            out.print(": переменная " + state.error.errorVar + " входит свободно в формулу " + state.error.errorExpression.str + ".");
                            break;
                        case 3:
                            out.print(": используется правило с квантором по переменной " + state.error.errorVar + ", входящей свободно в допущение " + state.error.errorExpression.str + ".");
                            break;
                    }
                    return null;
                }
                if (alpha != null) {
                    Expression tmp = new Expression(alpha, expr, "->");
                    res.add(tmp.str);
                }
            }
        } catch (Exception e) {
            //System.out.println(e.getMessage());
        }
        return res;
    }

    private static ArrayList<String> getProofPart (String lemma, ArrayList<Expression> boundedArgs) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(lemma));
        String line;
        ArrayList<String> proofPart = new ArrayList<>();
        while ((line = bf.readLine()) != null) {
            proofPart.add(parser.impl(line).bind(boundedArgs).str);
        }
        return proofPart;
    }

}
