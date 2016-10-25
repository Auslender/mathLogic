package pair;

/**
 * Created by maria on 23.10.16.
 */
public class Pair<T1, T2> {
    public T1 expr;
    public T2 index;

    public Pair(T1 expr, T2 index) {
        this.expr = expr;
        this.index = index;
    }
}
