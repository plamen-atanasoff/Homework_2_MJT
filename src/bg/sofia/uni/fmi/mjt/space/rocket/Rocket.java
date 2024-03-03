package bg.sofia.uni.fmi.mjt.space.rocket;

import java.util.Optional;

public record Rocket(String id, String name, Optional<String> wiki, Optional<Double> height) {
    private static final String REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))";
    private static final int ID_POS = 0;
    private static final int NAME_POS = 1;
    private static final int WIKI_POS = 2;
    private static final int HEIGHT_POS = 3;
    private static final int DATA_MEMBERS_COUNT = 4;

    public static Rocket of(String line) {
        String[] tokens = line.split(REGEX);

        String name = isSurroundedByQuotationMarks((tokens[NAME_POS])) ?
            getNameFormatted(tokens[NAME_POS]) : tokens[NAME_POS];

        Optional<String> wiki;
        if (wikiExists(WIKI_POS < tokens.length ? tokens[WIKI_POS] : null, tokens.length)) {
            wiki = getWiki(tokens[WIKI_POS]);
        } else {
            wiki = Optional.empty();
        }

        Optional<Double> height;
        if (heightExists(tokens.length)) {
            height = getHeight(tokens[HEIGHT_POS]);
        } else {
            height = Optional.empty();
        }

        return new Rocket(tokens[ID_POS], name, wiki, height);
    }

    private static Optional<String> getWiki(String wiki) {
        return Optional.of(wiki);
    }

    private static boolean wikiExists(String wiki, int tokensCount) {
        return (tokensCount == DATA_MEMBERS_COUNT - 1) || (tokensCount == DATA_MEMBERS_COUNT && !wiki.isEmpty());
    }

    private static Optional<Double> getHeight(String height) {
        int trailingLength = 2;
        int heightWithoutTrailingInd = height.length() - trailingLength;
        String heightFormatted = height.substring(0, heightWithoutTrailingInd);

        return Optional.of(Double.parseDouble(heightFormatted));
    }

    private static boolean heightExists(int tokensCount) {
        return tokensCount == DATA_MEMBERS_COUNT;
    }

    private static boolean isSurroundedByQuotationMarks(String str) {
        return str.charAt(0) == '"';
    }

    private static String getNameFormatted(String name) {
        return name.substring(1, name.length() - 1);
    }
}