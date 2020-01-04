import java.util.*;

public class Main {
    private static Scanner input = new Scanner(System.in);
    private static Random random = new Random();

    private Entry[] entries;
    private SampleGroup[] groups;
    private int entriesCount;
    private int groupsCount;
    private int attributesCount;

    private Main(Entry[] entries, int groupsCount, int attributesCount) {
        this.entries = entries;
        this.entriesCount = entries.length;
        this.groupsCount = groupsCount;
        this.attributesCount = attributesCount;
        this.groups = new SampleGroup[groupsCount];

        generateGroups();
    }

    private void generateGroups() {
        final int groupSize = entriesCount / groupsCount;
        int firstIndex = 0;
        int lastIndex = groupSize - 1;

        for (int i = 0; i < groupsCount; i++) {
            if (i == (groupsCount - 1)) {
                lastIndex = entriesCount - 1;
            }

            groups[i] = new SampleGroup(entries, firstIndex, lastIndex);

            firstIndex = lastIndex + 1;
            lastIndex += groupSize;
        }
    }

    static private void randomizeSample(Entry[] entries) {
        Entry tmp;
        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < entries.length; i++) {
                int newIndex = random.nextInt(entries.length);

                tmp = entries[i];
                entries[i] = entries[newIndex];
                entries[newIndex] = tmp;
            }
        }
    }

    static private Entry[] getEntriesFromInput(int membersCount, int attributesCount, int totalAttributesCount) {
        Entry[] entries = new Entry[membersCount];


        for (int i = 0; i < membersCount; i++) {

            Attribute[] attributes = new Attribute[attributesCount];

            for (int j = 0; j < totalAttributesCount; j++) {
                String str = input.next();

                if (j < attributesCount) {
                    attributes[j] = new Attribute(j);
                    attributes[j].setValue(str);
                }
            }
            entries[i] = new Entry(attributes);
        }

        return entries;
    }

    private double getAverageScore(OVERFITTING_AVOIDANCE_TECHNIQUE technique) {
        double bestScore = 0.0;
        Model bestModel = null;

        double scoreSum = 0;

        for (int i = 0; i < groupsCount; i++) {
            int teachingGroupId = i;
            int pruningGroupId = (i + 1) % groupsCount;

            Set<Entry> teachingSet = groups[teachingGroupId].toSet();
            Set<Entry> pruningSet = groups[pruningGroupId].toSet();
            Set<Entry> testingSet = new HashSet<>();

            for (int j = 0; j < groupsCount; j++) {
                if (j != teachingGroupId && j != pruningGroupId) {
                    testingSet.addAll(groups[j].toSet());
                }
            }

            // the class index is null, because we want to test for recurrence events
            Model model = new Model(teachingSet, pruningSet, 0, attributesCount);
            model.build(technique);

            double score = model.getScore(testingSet);

//            System.out.println("score = " + score);

            if (score > bestScore) {
                bestScore = score;
                bestModel = model;
            }

            scoreSum += score;
        }

        System.out.println("Best score using overfitting technique " + technique.toString() + " is = " + bestScore);

        return scoreSum / groupsCount;
    }

    public static void main(String[] args) {
        int groupsCount = Integer.parseInt(args[0]);
        int membersCount = input.nextInt();
        int attributesCount = input.nextInt();
        int totalAttributesCount = input.nextInt();

//        System.out.println(groupsCount);
//        System.out.println(membersCount);
//        System.out.println(attributesCount);
//        System.out.println(totalAttributesCount);

        Entry[] entries = getEntriesFromInput(membersCount, attributesCount, totalAttributesCount);

        randomizeSample(entries);

//        for (Entry entry : entries) {
//            System.out.println(entry);
//        }

        Main id3 = new Main(entries, groupsCount, attributesCount);

        System.out.println("average score = " + id3.getAverageScore(OVERFITTING_AVOIDANCE_TECHNIQUE.NONE));
        System.out.println("average score = " + id3.getAverageScore(OVERFITTING_AVOIDANCE_TECHNIQUE.PRE_PRUNING));
        System.out.println("average score = " + id3.getAverageScore(OVERFITTING_AVOIDANCE_TECHNIQUE.MINIMUM_INFORMATION_GAIN));
    }
}
