package bg.sofia.uni.fmi.mjt.space.mission;

import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public record Mission(String id, String company, String location, LocalDate date, Detail detail,
                      RocketStatus rocketStatus, Optional<Double> cost, MissionStatus missionStatus) {
    private static final String REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))";
    private static final int ID_POS = 0;
    private static final int COMPANY_POS = 1;
    private static final int LOCATION_POS = 2;
    private static final int DATE_POS = 3;
    private static final int DETAIL_POS = 4;
    private static final int ROCKET_STATUS_POS = 5;
    private static final int COST_POS = 6;
    private static final int MISSION_STATUS_POS = 7;

    public static Mission of(String line) {
        String[] tokens = line.split(REGEX);

        String location = getStringWithoutQuotes(tokens[LOCATION_POS]);
        LocalDate date = getDateFormatted(tokens[DATE_POS]);
        Detail detail = Detail.of(tokens[DETAIL_POS]);
        RocketStatus rocketStatus = Arrays.stream(RocketStatus.values())
            .filter(rs -> tokens[ROCKET_STATUS_POS].equals(rs.toString()))
            .findAny()
            .get();
        Optional<Double> cost = getCost(tokens[COST_POS]);
        MissionStatus missionStatus = Arrays.stream(MissionStatus.values())
            .filter(ms -> tokens[MISSION_STATUS_POS].equals(ms.toString()))
            .findAny()
            .get();

        return new Mission(tokens[ID_POS], tokens[COMPANY_POS], location,
            date, detail, rocketStatus, cost, missionStatus);
    }

    private static Optional<Double> getCost(String costString) {
        Optional<Double> cost;

        if (costString.isEmpty()) {
            cost = Optional.empty();
        } else {
            StringBuilder costFormatted = new StringBuilder(costString);

            costFormatted
                .deleteCharAt(0)
                .deleteCharAt(costFormatted.length() - 1);

            int indexOfComma;
            if ((indexOfComma = costFormatted.indexOf(",")) != -1) {
                costFormatted.deleteCharAt(indexOfComma);
            }

            cost = Optional.of(Double.parseDouble(costFormatted.toString()));
        }

        return cost;
    }

    private static String getStringWithoutQuotes(String str) {
        return str.substring(1, str.length() - 1);
    }

    private static LocalDate getDateFormatted(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM dd, yyyy", Locale.ENGLISH);

        return LocalDate.parse(getStringWithoutQuotes(date), formatter);
    }
}
