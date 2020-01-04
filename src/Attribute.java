public class Attribute {
    static final String[][] allowedValues =
            {
                    {"no-recurrence-events", "recurrence-events", "?"},
                    {"10-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80-89", "90-99", "?"},
                    {"lt40", "ge40", "premeno", "?"},
                    {"0-4", "5-9", "10-14", "15-19", "20-24", "25-29", "30-34",
                            "35-39", "40-44", "45-49", "50-54", "55-59", "?"},
                    {"0-2", "3-5", "6-8", "9-11", "12-14", "15-17", "18-20",
                            "21-23", "24-26", "27-29", "30-32", "33-35", "36-39", "?"},
                    {"yes", "no", "?"},
                    {"1", "2", "3", "?"},
                    {"left", "right", "?"},
                    {"left_up", "left_low", "right_up", "right_low", "central", "?"},
                    {"yes", "no", "?"}
            };

    static private final String[] types = {"Recurrence", "Age", "Menopause", "Tumor-size",
            "Inv-nodes", "Node-caps", "Deg-malig", "Breast", "Breast-quad", "Irradiat"};

    private int type;
    private int value;

    public Attribute(int type) {
        this.type = type;
    }

    int getValue() {
        return value;
    }

    void setValue(String str) {
        int i;

        for (i = 0; i < allowedValues[type].length; i++) {
            if (allowedValues[type][i].equals(str)) {
                value = i;
                break;
            }
        }

        if (i == allowedValues[type].length) {
            throw new IllegalArgumentException(str);
        }
    }

    @Override
    public String toString() {
//        return "Attribute{" +
//                "type=" + types[type] +
//                ", value=" + allowedValues[type][value] +
//                '}';
        return allowedValues[type][value];
    }
}
