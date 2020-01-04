import java.util.Arrays;

public class Entry {
    Attribute[] attributes;

    Entry(Attribute[] attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "attributes=" + Arrays.toString(attributes) +
                '}';
    }
}
