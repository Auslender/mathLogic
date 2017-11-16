package parse_check;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Expression {
    int n;
    String parsed;
    private int hashCovStr = 0;
    public List<Expression> children;
    public Set<String> free;
    public String str;
    Set<String> bounded;
    
    Expression(String s) {
        n = 0;
        str = s;
        children = null;
        parsed = s;
        free = new HashSet<>();
        free.add(s);
        bounded = new HashSet<>();
    }

    Expression(String s, Expression Child) {
        n = 1;
        str = s;
        children = new ArrayList<>(n);
        children.add(Child);
        if (s.equals("\'"))
            parsed = '(' + Child.toString() + ')' + s;
        else
            parsed = s + '(' + Child.toString() + ')';
        free = new HashSet<>(Child.free);
        bounded = new HashSet<>();
    }

    Expression(String s, Expression left, Expression right) {
        n = 2;
        str = s;
        children = new ArrayList<>(2);
        children.add(left);
        children.add(right);
        bounded = new HashSet<>();
        if (s.equals("@") || s.equals(("?"))) {
            parsed = s + left.toString() + '(' + right.toString() + ')';
            free = new HashSet<>(right.free);
            free.remove(left.str);
            bounded.add(left.toString());
            children.get(1).remove_from_free(left.toString());
        } else {
            parsed = '(' + left.toString() + s + right.toString() + ')';
            free = new HashSet<>(left.free);
            free.addAll(right.free);
        }

    }

    Expression(String s, List<Expression> Children) {
        n = Children.size();
        str = s;
        children = new ArrayList<>(Children);
        parsed = s + '(';
        for (Expression tmp : Children)
            parsed += (tmp.toString()+',');
        parsed=parsed.substring(0,parsed.length()-1);
        parsed += ')';
        free = new HashSet<>();
        for (Expression tmp : Children)
            free.addAll(tmp.free);
        bounded = new HashSet<>();
    }

    private int getHash() {
        if (hashCovStr == 0)
            hashCovStr = parsed.hashCode();
        return hashCovStr;
    }

    public boolean isEqualTo(Expression other) {
        return (getHash() == other.getHash()) && (parsed.equals(other.parsed));
    }

    public String toString() {
        if (children == null)
            return parsed;
        return parsed;
    }

    private void remove_from_free(String var) {
        bounded.add(var);
        if (free.contains(var))
            free.remove(var);
        if (children == null)
            return;
        for (Expression tmp : children) {
            tmp.remove_from_free(var);
        }
    }

}
