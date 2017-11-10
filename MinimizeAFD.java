import java.util.HashMap;
import java.util.Map;

import javax.swing.*;


public class MinimizeAFD {

    public static HashMap<Pair, Boolean> setupMarkedPairs(DFA dfa) {
        // MARK pars where where one and only one state is final
        HashMap<Pair, Boolean> pairs = new HashMap<Pair, Boolean>();

        for (int i = 1; i <= dfa.StatesCount; ++i)
            for (int j = 1; j <= dfa.StatesCount; ++j) {
                Boolean Marked = ((dfa.IsFinal[i] == true && dfa.IsFinal[j] == false) ||
                        (dfa.IsFinal[i] == false && dfa.IsFinal[j] == true));
                pairs.put(new Pair(i, j), Marked);
            }

        return pairs;
    }

    public static void processPairs(DFA dfa, HashMap<Pair, Boolean> pairs) {
        boolean found = false;
        do {
            found = false;
            for (Map.Entry<Pair, Boolean> entry : pairs.entrySet()) {
                if (entry.getValue() == false)
                    for (char a : dfa.Symbols) {
                        Leftside leftside = new Leftside(entry.getKey().P, a);
                        Integer d1 = dfa.Rules.get(leftside);
                        leftside = new Leftside(entry.getKey().Q, a);
                        Integer d2 = dfa.Rules.get(leftside);
                        if (pairs.get(new Pair(d1, d2)).booleanValue() == true) {
                            pairs.put(new Pair(entry.getKey().P, entry.getKey().Q), true);
                            found = true;
                            break;
                        }
                    }
            }
        } while (found);
    }

    public static int[] createEqClasses(DFA dfa, HashMap<Pair, Boolean> pairs) {
        // determine equivalence classes
        int[] e_class = new int[dfa.StatesCount + 1];
        for (int i = 1; i <= dfa.StatesCount; ++i)
            e_class[i] = i;

        pairs.forEach((k, v) -> {
            if (v == false)
                for (int i = 1; i <= dfa.StatesCount; ++i)
                    if (e_class[i] == k.P)
                        e_class[i] = k.Q;
        });
        return e_class;
    }

    public static String outputResults(DFA dfa, HashMap<Pair, Boolean> pairs, int[] e_class) {
        String con = "";
        // save the states of the new automation in "states" object
        // (to remove duplicates)
        HashMap<Integer, Boolean> states = new HashMap<Integer, Boolean>();
        for (int state = 1; state <= dfa.StatesCount; ++state)
            states.put(e_class[state], true);

        // save the rules of the news rules in the "rules" object
        // (to remove duplicates)
        HashMap<String, Boolean> rules = new HashMap<String, Boolean>();

        for (Map.Entry<Leftside, Integer> rule : dfa.Rules.entrySet()) {
            String rule_str =  (e_class[rule.getKey().State] - 1) + " " +
                    rule.getKey().Symbol + " " + (e_class[rule.getValue()] - 1);

            rules.put(rule_str, true);
        }

        for (int i : states.keySet())
            con += (i - 1)+" ";
        con += "\n";
        //print special states
        con += (dfa.StartState - 1) + "\n";
        for (int i = 1; i < dfa.IsFinal.length; i++) {
            for (int o : states.keySet()){
                if (dfa.IsFinal[i] == true && o == i) {
                    con += ""+(i - 1);
                    break;
                }
            }

        }
        con += "\n";

        for (String s : rules.keySet())
            con += s + "\n";
        return con;
    }


    public static DFA getDFA(JTable table) {
        DFA dfa = new DFA();
        dfa.StatesCount = table.getRowCount();
        char[] symbolTemp = new char[table.getColumnCount() - 1];
        for (int i = 1; i < table.getColumnCount(); i++) {
            symbolTemp[i - 1] = table.getColumnName(i).charAt(0);
        }
        dfa.Symbols = symbolTemp;
        Boolean[] isFanalTemp = new Boolean[table.getRowCount() + 1];

        dfa.IsFinal = isFanalTemp;
        HashMap<Leftside, Integer> rulesTemp = new HashMap<>();
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 1; j < table.getColumnCount(); j++) {
                Leftside leftside = new Leftside(i + 1, table.getColumnName(j).charAt(0));
                Integer integer = 1 + Integer.parseInt(table.getValueAt(i, j).toString());
                rulesTemp.put(leftside, integer);
            }
        }
        dfa.Rules = rulesTemp;
        return dfa;
    }
}