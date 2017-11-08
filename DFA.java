/**
 * Created by Palaf on 03/11/2017.
 */
import java.util.HashMap;
public class DFA {
    public int StatesCount;
    public char[] Symbols;
    public Boolean[] IsFinal;
    public int FinalState;
    public int StartState;
    public HashMap<Leftside, Integer> Rules = new HashMap<Leftside, Integer>();

}