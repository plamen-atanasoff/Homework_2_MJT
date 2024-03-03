package bg.sofia.uni.fmi.mjt.space.mission;

public record Detail(String rocketName, String payload) {
    private static final String REGEX = "\\s*\\|\\s*";
    private static final int ROCKET_NAME_POS = 0;
    private static final int PAYLOAD_POS = 1;

    public static Detail of(String line) {
        if (isSurroundedByQuotationMarks(line)) {
            line = line.substring(1, line.length() - 1);
        }

        String[] tokens = line.split(REGEX);

        return new Detail(tokens[ROCKET_NAME_POS], tokens[PAYLOAD_POS]);
    }

    private static boolean isSurroundedByQuotationMarks(String str) {
        return str.charAt(0) == '"';
    }
}
