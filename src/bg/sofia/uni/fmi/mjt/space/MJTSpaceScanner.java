package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.algorithm.SymmetricBlockCipher;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MJTSpaceScanner implements SpaceScannerAPI {
    private static final String IO_EXCEPTION_MESSAGE = "a problem occurred while reading from the file";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_MISSION_STATUS_MESSAGE = "missionStatus is null";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_ROCKET_STATUS_MESSAGE = "rocketStatus is null";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_TIME_PERIOD_MESSAGE = "from or to is null";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_OUTPUT_MESSAGE = "outputStream is null";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_N_MESSAGE = "n is less than or equal to 0";
    private static final String TIME_FRAME_MISMATCH_EXCEPTION_MESSAGE = "to is before from";
    private final Set<Mission> missions;
    private final Set<Rocket> rockets;
    private final SymmetricBlockCipher rijndael;

    public MJTSpaceScanner(Reader missionsReader, Reader rocketsReader, SecretKey secretKey) {
        missions = readLines(missionsReader, Mission::of);
        rockets = readLines(rocketsReader, Rocket::of);

        this.rijndael = new Rijndael(secretKey);
    }

    private <T> Set<T> readLines(Reader entitiesReader, Function<String, T> func) {
        try (var reader = new BufferedReader(entitiesReader)) {
            return reader.lines()
                .skip(1)
                .map(func)
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(IO_EXCEPTION_MESSAGE, e);
        }
    }

    @Override
    public Collection<Mission> getAllMissions() {
        return Collections.unmodifiableSet(missions);
    }

    @Override
    public Collection<Mission> getAllMissions(MissionStatus missionStatus) {
        if (missionStatus == null) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_MISSION_STATUS_MESSAGE);
        }

        return missions.stream()
            .filter(m -> m.missionStatus().equals(missionStatus))
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getCompanyWithMostSuccessfulMissions(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TIME_PERIOD_MESSAGE);
        }

        if (from.isAfter(to)) {
            throw new TimeFrameMismatchException(TIME_FRAME_MISMATCH_EXCEPTION_MESSAGE);
        }

        Map<String, Long> successfulMissionsCountPerCompany = missions.stream()
            .filter(m -> !m.date().isBefore(from))
            .filter(m -> !m.date().isAfter(to))
            .filter(m -> m.missionStatus().equals(MissionStatus.SUCCESS))
            .collect(Collectors.groupingBy(Mission::company, Collectors.counting()));

        return successfulMissionsCountPerCompany.entrySet().stream()
            .max(Comparator.comparingLong(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse("");
    }

    @Override
    public Map<String, Collection<Mission>> getMissionsPerCountry() {
        return missions.stream()
            .collect(Collectors.groupingBy(m -> getCountry(m.location()),
                Collectors.toCollection(HashSet<Mission>::new)));
    }

    private String getCountry(String str) {
        return str.substring(str.lastIndexOf(',') + 2);
    }

    @Override
    public List<Mission> getTopNLeastExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus) {
        if (n <= 0) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_N_MESSAGE);
        }

        if (missionStatus == null) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_MISSION_STATUS_MESSAGE);
        }
        if (rocketStatus == null) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_ROCKET_STATUS_MESSAGE);
        }

        return missions.stream()
            .filter(m -> m.cost().isPresent())
            .filter(m -> m.missionStatus().equals(missionStatus))
            .filter(m -> m.rocketStatus().equals(rocketStatus))
            .sorted((m1, m2) -> (int) (m1.cost().orElse(0.0) - m2.cost().orElse(0.0)))
            .limit(n)
            .toList();
    }

    @Override
    public Map<String, String> getMostDesiredLocationForMissionsPerCompany() {
        return getMostDesiredLocationForMissionsPerCompany(missions);
    }

    private Map<String, String> getMostDesiredLocationForMissionsPerCompany(Collection<Mission> missions) {
        Map<String, List<String>> locationsPerCompany = missions.stream()
            .collect(Collectors.groupingBy(Mission::company,
                Collectors.mapping(Mission::location, Collectors.toList())));

        return locationsPerCompany.entrySet().stream()
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), getMostDesiredLocation(e.getValue())))
            .collect(Collectors.toUnmodifiableMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private String getMostDesiredLocation(Collection<String> locations) {
        return locations.stream()
            .collect(Collectors.groupingBy(String::valueOf, Collectors.counting()))
            .entrySet().stream()
            .max(Comparator.comparingLong(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .get();
    }

    @Override
    public Map<String, String> getLocationWithMostSuccessfulMissionsPerCompany(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TIME_PERIOD_MESSAGE);
        }

        if (from.isAfter(to)) {
            throw new TimeFrameMismatchException(TIME_FRAME_MISMATCH_EXCEPTION_MESSAGE);
        }

        Set<Mission> validMissions = missions.stream()
            .filter(m -> !m.date().isBefore(from))
            .filter(m -> !m.date().isAfter(to))
            .filter(m -> m.missionStatus().equals(MissionStatus.SUCCESS))
            .collect(Collectors.toSet());

        return getMostDesiredLocationForMissionsPerCompany(validMissions);
    }

    @Override
    public Collection<Rocket> getAllRockets() {
        return Collections.unmodifiableSet(rockets);
    }

    @Override
    public List<Rocket> getTopNTallestRockets(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_N_MESSAGE);
        }

        return rockets.stream()
            .filter(r -> r.height().isPresent())
            .sorted((r1, r2) -> Double.compare(r2.height().get(), r1.height().get()))
            .limit(n)
            .toList();
    }

    @Override
    public Map<String, Optional<String>> getWikiPageForRocket() {
        return rockets.stream()
            .collect(Collectors.toUnmodifiableMap(Rocket::name, Rocket::wiki));
    }

    @Override
    public List<String> getWikiPagesForRocketsUsedInMostExpensiveMissions(int n, MissionStatus missionStatus,
                                                                          RocketStatus rocketStatus) {
        if (n <= 0) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_N_MESSAGE);
        }

        if (missionStatus == null) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_MISSION_STATUS_MESSAGE);
        }
        if (rocketStatus == null) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_ROCKET_STATUS_MESSAGE);
        }

        Map<String, Optional<String>> wikisByRocketName = rockets.stream()
            .collect(Collectors.toMap(Rocket::name, Rocket::wiki));

        return missions.stream()
            .filter(m -> m.cost().isPresent())
            .sorted((m1, m2) -> Double.compare(m2.cost().get(), m1.cost().get()))
            .limit(n)
            .filter(m -> wikisByRocketName.get(m.detail().rocketName()).isPresent())
            .map(m -> wikisByRocketName.get(m.detail().rocketName()).get())
            .distinct()
            .toList();
    }

    @Override
    public void saveMostReliableRocket(OutputStream outputStream, LocalDate from, LocalDate to) throws CipherException {
        if (outputStream == null) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_OUTPUT_MESSAGE);
        }
        if (from == null || to == null) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TIME_PERIOD_MESSAGE);
        }

        if (from.isAfter(to)) {
            throw new TimeFrameMismatchException(TIME_FRAME_MISMATCH_EXCEPTION_MESSAGE);
        }

        Map<String, List<Mission>> missionsByRocket = missions.stream()
            .collect(Collectors.groupingBy(m -> m.detail().rocketName()));

        String mostReliableRocketName = missionsByRocket.entrySet().stream()
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), calculateReliability(e.getValue())))
            .max(Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue))
            .orElse(new AbstractMap.SimpleEntry<>("", 0.0))
            .getKey();

        byte[] byteArray = mostReliableRocketName.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);

        rijndael.encrypt(inputStream, outputStream);
    }

    private double calculateReliability(List<Mission> missions) {
        int successfulMissions = 0;
        for (Mission mission : missions) {
            if (mission.missionStatus().equals(MissionStatus.SUCCESS)) {
                successfulMissions++;
            }
        }

        int unsuccessfulMissions = missions.size() - successfulMissions;
        return (double) (2 * successfulMissions + unsuccessfulMissions) / (2 * missions.size());
    }
}
