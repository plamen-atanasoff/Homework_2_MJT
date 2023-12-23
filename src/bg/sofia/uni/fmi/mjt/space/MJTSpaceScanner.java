package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MJTSpaceScanner implements SpaceScannerAPI {
    private final Set<Mission> missions;
    private final Set<Rocket> rockets;
    private final SecretKey secretKey;

    private <T> Set<T> readLines(Reader entitiesReader, Function<String, T> func) {
        try (BufferedReader reader = new BufferedReader(entitiesReader)) {
            reader.readLine();
            return reader
                .lines()
                .map(func)
                .collect(Collectors.toSet());
        } catch(IOException e) {
            throw new UncheckedIOException("A problem occured while reading from the file", e);
        }
    }

    public MJTSpaceScanner(Reader missionsReader, Reader rocketsReader, SecretKey secretKey) {
        missions = readLines(missionsReader, Mission::of);
        rockets = readLines(rocketsReader, Rocket::of);

        this.secretKey = secretKey;
    }

    @Override
    public Collection<Mission> getAllMissions() {
        return missions;
    }

    @Override
    public Collection<Mission> getAllMissions(MissionStatus missionStatus) {
        return null;
    }

    @Override
    public String getCompanyWithMostSuccessfulMissions(LocalDate from, LocalDate to) {
        return null;
    }

    @Override
    public Map<String, Collection<Mission>> getMissionsPerCountry() {
        return null;
    }

    @Override
    public List<Mission> getTopNLeastExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus) {
        return null;
    }

    @Override
    public Map<String, String> getMostDesiredLocationForMissionsPerCompany() {
        return null;
    }

    @Override
    public Map<String, String> getLocationWithMostSuccessfulMissionsPerCompany(LocalDate from, LocalDate to) {
        return null;
    }

    @Override
    public Collection<Rocket> getAllRockets() {
        return rockets;
    }

    @Override
    public List<Rocket> getTopNTallestRockets(int n) {
        return null;
    }

    @Override
    public Map<String, Optional<String>> getWikiPageForRocket() {
        return null;
    }

    @Override
    public List<String> getWikiPagesForRocketsUsedInMostExpensiveMissions(int n, MissionStatus missionStatus,
                                                                          RocketStatus rocketStatus) {
        return null;
    }

    @Override
    public void saveMostReliableRocket(OutputStream outputStream, LocalDate from, LocalDate to) throws CipherException {

    }
}
