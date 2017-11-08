/**
 * Created by Palaf on 03/11/2017.
 */
public class Pair {
    public int P, Q;

    public Pair(int thep, int theq) {
        P = thep;
        Q = theq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (P != pair.P) return false;
        return Q == pair.Q;

    }

    @Override
    public int hashCode() {
        int result = P;
        result = 31 * result + Q;
        return result;
    }
}