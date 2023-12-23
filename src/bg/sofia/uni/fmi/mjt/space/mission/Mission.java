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
            .filter(str -> tokens[rocketStatusPos].equals(str.toString()))
            .findFirst()
            .get();

        Optional<Double> cost = tokens[costPos].isEmpty() ? Optional.empty()
            : Optional.of(Double.parseDouble(tokens[costPos].substring(1, tokens[costPos].length() - 1)));

        int missionStatusFormatted = tokens[missionStatusPos].length() - 1;
        MissionStatus missionStatus = Arrays.stream(MissionStatus.values())
            .filter(str -> tokens[missionStatusPos].substring(0, missionStatusFormatted).equals(str.toString()))
            .findFirst()
            .get();

        return new Mission(tokens[idPos], tokens[companyPos], locationFormatted,
            date, detail, rocketStatus, cost, missionStatus);
    }
}
