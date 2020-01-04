import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

enum OVERFITTING_AVOIDANCE_TECHNIQUE {
    NONE,
    PRE_PRUNING,
}

class Node {
    Node(int leafValue) {
        childNodes = null;
        attribute = -1;
        this.leafValue = leafValue;
    }

    Node[] childNodes;
    int attribute;
    int leafValue;
}

class Model {

    private Set<Entry> teachingSet;
    private Set<Entry> pruningSet;

    private int unknownAttr;
    private int attrCount;

    private Node root;

    Model(Set<Entry> teachingSet, Set<Entry> pruningSet, int unknownAttr, int attrCount) {
        this.teachingSet = teachingSet;
        this.pruningSet = pruningSet;
        this.unknownAttr = unknownAttr;
        this.attrCount = attrCount;
        this.root = new Node(getMostProbableTreeLeafValue(teachingSet));
    }

    private int getMostProbableTreeLeafValue(Set<Entry> entries) {
        final int unknownAttrValuesCount = Attribute.allowedValues[unknownAttr].length;
        int[] unknownAttrOccurrence = new int[unknownAttrValuesCount];

        for (Entry entry : entries) {
            int unknownAttrValue = entry.attributes[unknownAttr].getValue();
            ++unknownAttrOccurrence[unknownAttrValue];
        }

        int maxOccurrenceValue = 0;
        int maxOccurrenceCount = unknownAttrOccurrence[0];

        for (int i = 1; i < unknownAttrValuesCount; i++) {
            if (unknownAttrOccurrence[i] > maxOccurrenceCount) {
                maxOccurrenceCount = unknownAttrOccurrence[i];
                maxOccurrenceValue = i;
            }
        }

        return maxOccurrenceValue;
    }

    private double getEntropy(Set<Entry> entries) {
        final int unknownAttrValuesCount = Attribute.allowedValues[unknownAttr].length;
        int[] unknownAttrOccurrence = new int[unknownAttrValuesCount];

        for (Entry entry : entries) {
            int unknownAttrValue = entry.attributes[unknownAttr].getValue();
            ++unknownAttrOccurrence[unknownAttrValue];
        }

        int entriesCount = entries.size();
        double entropy = 0.0;

        for (int i = 0; i < unknownAttrValuesCount; i++) {
            if (unknownAttrOccurrence[i] != 0) {
                double probability = unknownAttrOccurrence[i] / (double) entriesCount;
                entropy -= probability * logBased2(probability);
            }
        }

        return entropy;
    }

    private double getConditionalEntropy(int knownAttr, Set<Entry> entries) {
        final int knownAttrValuesCount = Attribute.allowedValues[knownAttr].length;
        final int unknownAttrValuesCount = Attribute.allowedValues[unknownAttr].length;

        int[] knownAttrOccurrence = new int[knownAttrValuesCount];
        int[][] bothAttrOccurrence = new int[knownAttrValuesCount][unknownAttrValuesCount];

        for (Entry entry : entries) {
            int knownAttrValue = entry.attributes[knownAttr].getValue();
            ++knownAttrOccurrence[knownAttrValue];

            int unknownAttrValue = entry.attributes[unknownAttr].getValue();
            ++bothAttrOccurrence[knownAttrValue][unknownAttrValue];
        }

        int entriesCount = entries.size();
        double entropy = 0.0;

        for (int i = 0; i < knownAttrValuesCount; i++) {
            for (int j = 0; j < unknownAttrValuesCount; j++) {
                if (knownAttrOccurrence[i] != 0) {
                    entropy -= (bothAttrOccurrence[i][j] / (double) entriesCount)
                            * logBased2(bothAttrOccurrence[i][j] / (double) knownAttrOccurrence[i]);
                }
            }
        }

        return entropy;
    }

    private double logBased2(double input) {
        if (input == 0) {
            return 0;
        }

        return Math.log(input) / Math.log(2);
    }

    void build(OVERFITTING_AVOIDANCE_TECHNIQUE technique) {
        Set<Integer> unusedAttributes = IntStream.range(0, attrCount).boxed().collect(Collectors.toSet());
        unusedAttributes.remove(unknownAttr);

        generateTree(root, technique, unusedAttributes, teachingSet, 0);
    }

    // previous score is the score the tree has with a pruning set
    private void generateTree(Node root, OVERFITTING_AVOIDANCE_TECHNIQUE technique, Set<Integer> unusedAttributes,
                              Set<Entry> entries, double previousScore) {

        if (unusedAttributes.size() == 0) {
            return;
        }

        if (entries.size() == 0) {
            return;
        }

        double initialEntropy = getEntropy(entries);
        double minEntropy = initialEntropy;
        int bestAttribute = -1;

        for (Integer unusedAttribute : unusedAttributes) {
            double entropy = getConditionalEntropy(unusedAttribute, entries);
            if (entropy < minEntropy) {
                minEntropy = entropy;
                bestAttribute = unusedAttribute;
            }
        }

        unusedAttributes.remove(bestAttribute);

        if (Math.abs(minEntropy - initialEntropy) < 0.0001) {
            // just get a random value from the entry set. All unknownAttribute values should be the same.
            root.leafValue = entries.iterator().next().attributes[unknownAttr].getValue();
            return;
        }

        int bestAttrPossibleValuesCount = Attribute.allowedValues[bestAttribute].length;
        root.attribute = bestAttribute;


        // divide the entries into groups
        List<Set<Entry>> entryGroups = new ArrayList<>(bestAttrPossibleValuesCount);
        for (int i = 0; i < bestAttrPossibleValuesCount; i++) {
            entryGroups.add(new HashSet<>());
        }

        for (Entry entry : entries) {
            int bestAttrGroupValue = entry.attributes[bestAttribute].getValue();
            entryGroups.get(bestAttrGroupValue).add(entry);
        }

        root.childNodes = new Node[bestAttrPossibleValuesCount];
        for (int i = 0; i < bestAttrPossibleValuesCount; i++) {
            root.childNodes[i] = new Node(getMostProbableTreeLeafValue(entryGroups.get(i)));
        }

        if (technique.equals(OVERFITTING_AVOIDANCE_TECHNIQUE.PRE_PRUNING)) {
            double score = getScore(pruningSet);
            if (previousScore > score) {
                root.childNodes = null;
                return;
            }

            previousScore = score;
        }


        for (int i = 0; i < bestAttrPossibleValuesCount; i++) {
            generateTree(root.childNodes[i], technique, new HashSet<>(unusedAttributes), entryGroups.get(i), previousScore);
        }
    }

    double getScore(Set<Entry> testingSet) {
        int correct = 0;
        int total = testingSet.size();

        for (Entry entry : testingSet) {
            if (entry.attributes[unknownAttr].getValue() == getMostProbableAttributeValue(entry)) {
                ++correct;
            }
        }

        return correct / (double) total;
    }

    private int getMostProbableAttributeValue(Entry entry) {
        return searchTree(root, entry);
    }

    private static int searchTree(Node root, Entry entry) {
        if (root.childNodes == null) {
            return root.leafValue;
        }

        int transitionValue = entry.attributes[root.attribute].getValue();
        return searchTree(root.childNodes[transitionValue], entry);
    }
}
