package bg.sofia.uni.fmi.mjt.space.mission;

import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public record Mission(String id, String company, String location, LocalDate date, Detail detail,
                      RocketStatus rocketStatus, Optional<Double> cost, MissionStatus missionStatus) {
    private static final int idPos = 0;
    private static final int companyPos = 1;
    private static final int locationPos = 2;
    private static final int datePos = 3;
    private static final int detailPos = 4;
    private static final int rocketStatusPos = 5;
    private static final int costPos = 6;
    private static final int missionStatusPos = 7;

    public static Mission of(String line) {
        String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))");

        String locationFormatted = tokens[locationPos].substring(1, tokens[locationPos].length() - 1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM dd, yyyy", Locale.ENGLISH);
        LocalDate date = LocalDate.parse(tokens[datePos].substring(1, tokens[datePos].length() - 1), formatter);

        Detail detail = Detail.of(tokens[detailPos]);

        RocketStatus rocketStatus = Arrays.stream(RocketStatus.values())
            .filter(rs -> tokens[rocketStatusPos].equals(rs.toString()))
            .findAny()
            .get();

        Optional<Double> cost;
        if (tokens[costPos].isEmpty()) {
            cost = Optional.empty();
        } else {
            StringBuilder costFormatted = new StringBuilder(tokens[costPos]);
            costFormatted
                .deleteCharAt(0)
                .deleteCharAt(costFormatted.length() - 1);
            int indexOfComma;
            if ((indexOfComma = costFormatted.indexOf(",")) != -1) {
                costFormatted.deleteCharAt(indexOfComma);
            }
            cost = Optional.of(Double.parseDouble(costFormatted.toString()));
        }

        MissionStatus missionStatus = Arrays.stream(MissionStatus.values())
            .filter(ms -> tokens[missionStatusPos].equals(ms.toString()))
            .findAny()
            .get();

        return new Mission(tokens[idPos], tokens[companyPos], locationFormatted,
            date, detail, rocketStatus, cost, missionStatus);
    }
}
