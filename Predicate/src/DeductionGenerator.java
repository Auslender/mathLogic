import parse_check.*;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

class DeductionGenerator {
    private static final String ALPHA_ALPHA = "lemma_templates/alpha_alpha";
    private static final String AX_HYPO = "lemma_templates/hypothesis";
    private static final String MP = "lemma_templates/modus_ponens";
    private static final String FORALL = "lemma_templates/forAll";
    private static final String EXISTS = "lemma_templates/exists";
    private static final String AXIOMS ="solid/axioms";

    private boolean generate;

    private ExpressionParser parser = new ExpressionParser();
    private List<Expression> axioms;
    private List<Expression> hypotheses;

    //mp
    private Set<Expression> implOldProof = new TreeSet<>((Comparator<Expression>) Comparator.comparing(Expression::toString));
    private Set<String> oldProof = new TreeSet<>();
    private Map<String, ArrayList<String>> proofMap = new TreeMap<>();

    private int counter = 1;
    private Expression target;

    private Checker checker = new Checker();

    private Writer newProofwriter;

    DeductionGenerator(List<Expression> hypos, Expression alpha, Writer writer, Expression answer) throws Exception {
        generate = true;
        newProofwriter = writer;
        String header = "";
        for (Expression guess : hypos)
            header += guess.toString() + ',';
        header = header.substring(0, Math.max(0, header.length() - 1)) + "|- (" + alpha.toString() + ")->" + answer.toString() + '\n';
        writer.write(header);
        target = alpha;
        hypotheses = hypos;
        hypotheses.add(alpha);
        getAxioms();
    }

    DeductionGenerator(Writer writer, Expression answer) throws Exception {
        generate = false;
        getAxioms();
        newProofwriter = writer;
        writer.write("|-" + answer.toString() + '\n');
    }

    void add(Expression expr) throws Exception {
        WrongProofException trigger = new WrongProofException("неизвестная ошибка", counter);
        if (generate && expr.isEqualTo(target)) {//same
            substToProofTemplate(ALPHA_ALPHA, expr.toString(), expr.toString(), "",target);
            accept(expr);
            return;
        }
        //axiom
        for (Expression axiom : axioms)
            if (checker.matchesScheme(expr, axiom)) {
                substToProofTemplate(AX_HYPO, expr.toString(), expr.toString(), "",target);
                accept(expr);
                return;
            }
        //hypo
        if (generate)
            for (Expression axiom : hypotheses)
                if (expr.isEqualTo(axiom)) {
                    substToProofTemplate(AX_HYPO, expr.toString(), expr.toString(), "",target);
                    accept(expr);
                    return;
                }
        //mp
        if (proofMap.containsKey(expr.toString())) {
            for (String check : proofMap.get(expr.toString()))
                if (oldProof.contains(check)) {
                    substToProofTemplate(MP, check, expr.toString(), "",target);
                    accept(expr);
                    return;
                }
        }

        //axiom12 (ψ[x := θ]) → ∃x(ψ)
        if (expr.str.equals("->")) {
            if (expr.children.get(1).str.equals("?")) {
                Expression phi = expr.children.get(1);
                Expression newf = expr.children.get(0);
                Integer status = checker.checkFreeUnderQuantifier(phi, newf);
                if (status == 1) {
                    substToProofTemplate(AX_HYPO, expr.toString(), expr.toString(), "",target);
                    accept(expr);
                    return;
                } else if (status == 0)
                    trigger = new WrongProofException("терм " + checker.targetChange.toString() + " не свободен для подстановки в формулу вместо переменной " + expr.children.get(1).children.get(0).toString(), counter);
            }
            //axiom11 ∀x(ψ)->(ψ[x := θ])
            if (expr.children.get(0).str.equals("@")) {
                Expression phi = expr.children.get(0);
                Expression newf = expr.children.get(1);
                Integer status = checker.checkFreeUnderQuantifier(phi, newf);
                if (status == 1) {
                    substToProofTemplate(AX_HYPO, expr.toString(), expr.toString(), "", target);
                    accept(expr);
                    return;
                } else if (status == 0)
                    trigger = new WrongProofException("терм " + checker.targetChange.toString() + " не свободен для подстановки в формулу вместо переменной" + expr.children.get(0).children.get(0).toString(), counter);
            }
        }


        if (expr.children.get(0).str.equals("?")) {
            for (Expression proof : implOldProof) {
                if (proof.str.equals("->")) {
                    checker.targetChange = null;
                    String variable = expr.children.get(0).children.get(0).str;
                    Expression psi0 = proof.children.get(0);
                    Expression psi1 = proof.children.get(1);
                    Expression psi2 = expr.children.get(0).children.get(1);
                    Expression psi3 = expr.children.get(1);

                    if (checker.checkEqual(psi0, psi2, variable) && psi3.isEqualTo(psi1)) {
                        if (psi3.free.contains(variable)) {
                            trigger = new WrongProofException("переменная " + variable + " входит свободно в формулу " + psi3.toString(), counter);
                        } else if (generate && target.free.contains(variable)) {
                            trigger = new WrongProofException("используется правило с квантором по переменной " + variable + ", входящей свободно в допущение " + target.toString(), counter);
                        } else {
                            if (!generate) {
                                newProofwriter.write(expr.toString() + '\n');
                            } else
                                substToProofTemplate(EXISTS, psi2.toString(), psi3.toString(), expr.children.get(0).children.get(0).toString(), target);
                            accept(expr);
                            return;
                        }
                    }
                }
            }
        }
        if (expr.children.size() > 1 && expr.children.get(1).str.equals("@")) {
            for (Expression proof : implOldProof) {
                if (proof.str.equals("->")) {
                    checker.targetChange = null;
                    String variable = expr.children.get(1).children.get(0).str;
                    Expression psi0 = proof.children.get(1);
                    Expression psi1 = proof.children.get(0);
                    Expression psi2 = expr.children.get(1).children.get(1);
                    Expression psi3 = expr.children.get(0);

                    if (checker.checkEqual(psi0, psi2, variable) && psi3.isEqualTo(psi1)) {
                        if (psi3.free.contains(variable)) {
                            trigger = new WrongProofException("переменная " + variable + " входит свободно в формулу " + psi3.toString(), counter);
                        } else if (generate && target.free.contains(variable)) {
                            trigger = new WrongProofException("используется правило с квантором по переменной " + variable + ", входящей свободно в допущение " + target.toString(), counter);
                        } else {
                            if (!generate) {
                                newProofwriter.write(expr.toString() + '\n');
                            } else
                                substToProofTemplate(FORALL, psi3.toString(), psi2.toString(), expr.children.get(1).children.get(0).toString(), target);
                            accept(expr);
                            return;
                        }

                    }
                }
            }
        }
        try {
            Integer tmp = checker.quantifierFreeUnique(expr);
            if (tmp != -1) {
                substToProofTemplate(AX_HYPO, expr.toString(), expr.toString(), "",target);
                accept(expr);
                return;

            }
        } catch (Exception e) {
            throw trigger;
        }
        throw trigger;
    }


    private void accept(Expression expr) {
        counter++;
        if (expr.str.equals("->"))
            implOldProof.add(expr);
        oldProof.add(expr.toString());
        if (expr.str.equals("->")) {
            ArrayList<String> tmp;
            if (proofMap.containsKey(expr.children.get(1).toString()))
                tmp = proofMap.get(expr.children.get(1).toString());
            else
                tmp = new ArrayList<>();

            tmp.add(expr.children.get(0).toString());

            proofMap.put(expr.children.get(1).toString(), tmp);
        }
    }

    private void substToProofTemplate(String fileDir, String Aexpr, String Bexpr, String dexpr, Expression target) {
        try {
            if (!generate) {
                newProofwriter.write(Bexpr + '\n');
                return;
            }
            Scanner proofIn = new Scanner(new File(fileDir));
            while (proofIn.hasNext()) {
                newProofwriter.write(proofIn.nextLine().replace("1", Aexpr).replace("3", target.toString()).replace("2", Bexpr).replace("4", dexpr) + '\n');
            }
            proofIn.close();
        } catch (IOException e) {
            System.out.print("File /" + fileDir + "/ is not found");
        }
    }


    private void getAxioms() throws Exception {
        Scanner axioms1 = new Scanner(new File(AXIOMS));
        axioms = new ArrayList<>();
        while (axioms1.hasNext()) {
            axioms.add(parser.parse(axioms1.nextLine()));
        }
    }
}