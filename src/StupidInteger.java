import java.util.Objects;

/**
 * Functionally, this is just an integer. It used to be useful for purposes of having a counter
 * that I can pass around as a pointer instead of a primitive. However, now I'm just using it as an int
 * because I found a better solution to the problem it was made to solve. I keep it around and integrated
 * because it might be useful one day.
 */
public class StupidInteger {
    private final String nenor;
    private int value;
    public StupidInteger(int value){
        this.value = value;
        nenor= "";
    }

    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
    public void increment() {
        value++;
    }
    public void decrement() {
        value--;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StupidInteger that = (StupidInteger) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
