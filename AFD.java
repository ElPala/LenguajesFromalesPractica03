
import java.util.ArrayList;

public class AFD {
    String[][] d;
    ArrayList<String> abecedario;
    String inicial;
    int estados;
    String[] finales;
    ArrayList<String> estadosA;

    public AFD(String[][] d, ArrayList<String> abecedario, String inicial, int estados, String[] finales, ArrayList<String> estadosA) {
        this.d = d;
        this.abecedario = abecedario;
        this.inicial = inicial;
        this.estados = estados;
        this.finales = finales;
        this.estadosA = estadosA;
    }

    public boolean checkWord(String w) {
        String next = this.inicial;
        String letter = "";
        for (int i = 0; i < w.length(); i++) {//        i is to check letters
            letter = "" + w.charAt(i);
            next = this.d[this.estadosA.indexOf(next)][this.abecedario.indexOf(letter)];
        }
        for (int i = 0; i < finales.length; i++) {
            if (next.equals(finales[i])) {
                return true;
            }
        }
        return false;
    }
}