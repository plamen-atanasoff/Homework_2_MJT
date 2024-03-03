package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Disabled
public class MJTSpaceScannerBigFileTest {
    private static MJTSpaceScanner spaceScanner;
    private static SecretKey secretKey;

    @BeforeAll
    static void initializeMJTSpaceScannerBigFile() {
        Path missionPath = Path.of("F:\\Java\\Projects\\Homework_2_MJT\\all-missions-from-1957.csv");
        Path rocketPath = Path.of("F:\\Java\\Projects\\Homework_2_MJT\\all-rockets-from-1957.csv");

        Reader missionReader = null;
        Reader rocketReader = null;
        try {
            missionReader = Files.newBufferedReader(missionPath);
            rocketReader = Files.newBufferedReader(rocketPath);
        } catch (IOException e) {
            fail("IOException when reading from files", e);
        }

        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            fail(e);
        }

        keyGenerator.init(128);
        secretKey = keyGenerator.generateKey();

        spaceScanner = new MJTSpaceScanner(missionReader, rocketReader, secretKey);
    }
    @Test
    void testMJTSpaceScannerReadersInitializesCorrectly() {
        Collection<Mission> missions = spaceScanner.getAllMissions();
        Collection<Rocket> rockets = spaceScanner.getAllRockets();

        assertEquals(4324, missions.size());
        assertEquals(416, rockets.size());
    }

    @Test
    void testGetAllMissionsWithStatusFailure() {
        Collection<Mission> missions = spaceScanner.getAllMissions(MissionStatus.FAILURE);
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissions() {
        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to = LocalDate.of(2020, 12, 31);

        String missions = spaceScanner.getCompanyWithMostSuccessfulMissions(from, to);
    }

    @Test
    void testGetMissionsPerCountry() {
        Map<String, Collection<Mission>> missions = spaceScanner.getMissionsPerCountry();
    }

    @Test
    void testGetTopNLeastExpensiveMissions() {
        List<Mission> missions = spaceScanner.getTopNLeastExpensiveMissions(12,
            MissionStatus.SUCCESS, RocketStatus.STATUS_RETIRED);
    }

    @Test
    void testGetMostDesiredLocationForMissionsPerCompany() {
        Map<String, String> missions = spaceScanner.getMostDesiredLocationForMissionsPerCompany();
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompany() {
        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to = LocalDate.of(2020, 12, 31);

        Map<String, String> missions = spaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(from, to);
    }

    @Test
    void testGetTopNTallestRockets() {
        List<Rocket> rockets = spaceScanner.getTopNTallestRockets(12);
    }

    @Test
    void testGetWikiPageForRocket() {
        Map<String, Optional<String>> rockets = spaceScanner.getWikiPageForRocket();
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissions() {
        List<String> rockets = spaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(25,
            MissionStatus.SUCCESS, RocketStatus.STATUS_RETIRED);
    }

    @Test
    void testSaveMostReliableRocket() {
        Rijndael rijndael = new Rijndael(secretKey);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        LocalDate from = LocalDate.of(2020, 6, 17);
        LocalDate to = LocalDate.of(2020, 12, 31);

        try {
            spaceScanner.saveMostReliableRocket(outputStream, from, to);
        } catch (CipherException e) {
            fail(e);
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        ByteArrayOutputStream decryptedName = new ByteArrayOutputStream();
        try {
            rijndael.decrypt(inputStream, decryptedName);
        } catch (CipherException e) {
            fail(e);
        }
    }
}
