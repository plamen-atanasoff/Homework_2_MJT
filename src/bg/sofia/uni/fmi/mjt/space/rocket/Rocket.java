package bg.sofia.uni.fmi.mjt.space.rocket;

import java.util.Optional;

import static java.util.Optional.empty;

public record Rocket(String id, String name, Optional<String> wiki, Optional<Double> height) {
    private static final int idPos = 0;
    private static final int namePos = 1;
    private static final int wikiPos = 2;
    private static final int heightPos = 3;
    private static final int dataMembersCount = 4;

    public static Rocket of(String line) {
        String[] tokens = line.trim().split(",");

        Optional<String> wiki;
        if (tokens.length == dataMembersCount - 1 || tokens.length == dataMembersCount && !tokens[wikiPos].isEmpty()) {
            wiki = Optional.of(tokens[wikiPos]);
        } else {
            wiki = Optional.empty();
        }

        Optional<Double> height;
        if (tokens.length == dataMembersCount) {
            int trailingLength = 3;
            int heightWithoutTrailingInd = tokens[heightPos].length() - trailingLength;
            String heightFormatted = tokens[heightPos].substring(0, heightWithoutTrailingInd);
            height = Optional.of(Double.parseDouble(heightFormatted));
        } else {
            height = Optional.empty();
        }

        return new Rocket(tokens[idPos], tokens[namePos], wiki, height);
    }
}