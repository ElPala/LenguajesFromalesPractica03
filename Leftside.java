/**
 * Created by Palaf on 03/11/2017.
 */
public class Leftside {       // a left side of a rule -- a pair (state, symbol)
    public int State;
    public char Symbol;

    public Leftside(int st, char sym) {
        State = st;
        Symbol = sym;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Leftside leftside = (Leftside) o;

        if (State != leftside.State) return false;
        return Symbol == leftside.Symbol;

    }

    @Override
    public int hashCode() {
        int result = State;
        result = 31 * result + (int) Symbol;
        return result;
    }
}