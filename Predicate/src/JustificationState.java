/**
 * Created by maria on 09.04.17.
 */
public class JustificationState {

    class Error {
        int errorCode;
        Expression errorTerm;
        Expression errorExpression;
        String errorVar;

        Error(int errorCode, Expression errorTerm, Expression errorExpression, String errorVar) {
            this.errorCode = errorCode;
            this.errorTerm = errorTerm;
            this.errorExpression = errorExpression;
            this.errorVar = errorVar;
        }
    }

    State state;
    Error error;

    JustificationState(State state) {
        this.state = state;
    }

    JustificationState(State state, int errorCode, Expression errorTerm, Expression errorExpression, String errorVar) {
        this.state = state;
        this.error = new Error(errorCode, errorTerm, errorExpression, errorVar);
    }
}


