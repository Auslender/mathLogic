import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by maria on 03.01.17.
 */
public class Expression {
    Expression left, right;
    String op;
    String str = "";
    String notParsed = "";
    HashSet<String> free;
    ArrayList<Expression> terms;

    Expression() {
        free = new HashSet<>();
    }

    Expression(Expression other) {
        free = new HashSet<>(other.free);
        str = other.str;
        op = other.op;
        if (other.left != null) {
            left = new Expression(other.left);
        }
        if (other.right != null) {
            right = new Expression(other.right);
        }
    }

    public Expression (Expression left, Expression right, String op) {
        free = new HashSet<>();
        this.op = op;
        this.left = left;
        this.right = right;
        this.str = "(" + this.left.str + this.op + this.right.str + ")";
        this.notParsed = this.right.notParsed;

        this.free.addAll(this.left.free);
        this.free.addAll(this.right.free);
    }

    Expression bind(ArrayList<Expression> boundedArgs) {
        if (this.left != null) {
            this.left.bind(boundedArgs);
            this.str = left.str;
            if (this.right != null) {
                right.bind(boundedArgs);
                this.str += this.op + this.right.str;

            } else {
                this.str = this.op + this.left.str;
            }
        } else {
            Expression expr = boundedArgs.get(Integer.parseInt(this.str) - 1);
            this.left = expr.left;
            this.right = expr.right;
            this.op = expr.op;
            this.str = expr.str;
        }
        return this;
    }
}
