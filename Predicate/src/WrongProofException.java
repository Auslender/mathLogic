public class WrongProofException extends Exception {

    private Integer number;
    private String message;

    WrongProofException(String m, Integer i) {
        this.message = m;
        this.number = i;
    }

    public String getMessage() {
        return "Вывод некорректен начиная с формулы номер " + number + " : " + message;
    }
}
