import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class SampleGroup {
    SampleGroup(Entry[] entries, int firstIndex, int lastIndex) {
        this.entries = entries;
        this.firstIndex = firstIndex;
        this.lastIndex = lastIndex;
    }

    @Override
    public String toString() {
        return "SampleGroup{" +
                "firstIndex=" + firstIndex +
                ", lastIndex=" + lastIndex +
                '}';
    }

    Set<Entry> toSet() {
        return new HashSet<>(Arrays.asList(entries).subList(firstIndex, lastIndex + 1));
    }

    private final Entry[] entries;
    private int firstIndex;
    private int lastIndex;
}