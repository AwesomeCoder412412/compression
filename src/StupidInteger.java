import java.util.Objects;

public class StupidInteger {
    private int value;
    private String nenor;
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
