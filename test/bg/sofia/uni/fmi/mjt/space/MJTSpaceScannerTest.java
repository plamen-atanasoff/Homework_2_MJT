package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class MJTSpaceScannerTest {
    private static final String missionsData = "Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," +
        "\" Rocket\",Status Mission\n" +
        "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\"," +
        "Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Success\n" +
        "1,CASC,\"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China\",\"Thu Aug 06, 2020\"," +
        "Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,\"29.75 \",Success\n" +
        "2,SpaceX,\"Pad A, Boca Chica, Texas, USA\",\"Tue Aug 04, 2020\",Starship Prototype | 150 Meter Hop," +
        "StatusActive,,Failure\n" +
        "3,Roscosmos,\"Site 200/39, Baikonur Cosmodrome, Kazakhstan\",\"Thu Jul 30, 2020\"," +
        "Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,\"65.0 \",Success\n" +
        "4,ULA,\"SLC-41, Cape Canaveral AFS, Florida, USA\",\"Thu Jul 30, 2020\",Atlas V 541 | Perseverance," +
        "StatusActive,\"145.0 \",Success\n";
    private static final String rocketsData = """
        "",Name,Wiki,Rocket Height
        0,Tsyklon-3,https://en.wikipedia.org/wiki/Tsyklon-3,39.0 m
        1,Tsyklon-4M,https://en.wikipedia.org/wiki/Cyclone-4M,38.7 m
        2,Unha-2,https://en.wikipedia.org/wiki/Unha,28.0 m
        3,Unha-3,https://en.wikipedia.org/wiki/Unha,32.0 m
        4,Vanguard,https://en.wikipedia.org/wiki/Vanguard_(rocket),23.0 m
        5,Vector-H,https://en.wikipedia.org/wiki/Vector-H,18.3 m
        6,Vector-R,https://en.wikipedia.org/wiki/Vector-R,13.0 m
        """;
    private static MJTSpaceScanner spaceScanner;

    private static void initializeMJTSpaceScanner() {
        spaceScanner = new MJTSpaceScanner(new StringReader(missionsData), new StringReader(rocketsData), null);
    }

    private static void initializeMJTSpaceScanner(String missionsData) {
        spaceScanner = new MJTSpaceScanner(new StringReader(missionsData), new StringReader(rocketsData), null);
    }

    private static void initializeMJTSpaceScanner(String missionsData, String rocketsData) {
        spaceScanner = new MJTSpaceScanner(new StringReader(missionsData), new StringReader(rocketsData), null);
    }

    private static void initializeMJTSpaceScanner(String missionsData, SecretKey secretKey) {
        spaceScanner = new MJTSpaceScanner(new StringReader(missionsData), new StringReader(rocketsData), secretKey);
    }

    @Test
    void testMJTSpaceScannerReadersInitializeCorrectlyMissionsAndReaders() {
        initializeMJTSpaceScanner();

        Collection<Mission> missions = spaceScanner.getAllMissions();
        Collection<Rocket> rockets = spaceScanner.getAllRockets();

        assertEquals(5, missions.size());
        assertEquals(7, rockets.size());
    }

    @Test
    void testGetAllMissionsReturnsCorrectCollectionWhenPassedSuccess() {
        initializeMJTSpaceScanner();

        Collection<Mission> successfulMissions = spaceScanner.getAllMissions(MissionStatus.SUCCESS);

        assertEquals(4, successfulMissions.size());
        successfulMissions.forEach(m -> assertEquals(MissionStatus.SUCCESS, m.missionStatus()));
    }

    @Test
    void testGetAllMissionsReturnsCorrectCollectionWhenPassedFailure() {
        initializeMJTSpaceScanner();

        Collection<Mission> successfulMissions = spaceScanner.getAllMissions(MissionStatus.FAILURE);

        assertEquals(1, successfulMissions.size());
        successfulMissions.forEach(m -> assertEquals(MissionStatus.FAILURE, m.missionStatus()));
    }

    @Test
    void testGetAllMissionsThrowsWhenPassedNull() {
        initializeMJTSpaceScanner();

        assertThrows(IllegalArgumentException.class, () -> spaceScanner.getAllMissions(null));
    }

    @Test
    void testGetAllMissionsReturnsEmptyCollectionWhenNoMissionsExistWithGivenStatus() {
        initializeMJTSpaceScanner();

        Collection<Mission> unsuccessfulMissions = spaceScanner.getAllMissions(MissionStatus.PARTIAL_FAILURE);

        assertTrue(unsuccessfulMissions.isEmpty());
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsReturnsCorrectCompany() {
        String missionsData = "Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," +
            "\" Rocket\",Status Mission\n" +
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\"," +
            "Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Success\n" +
            "1,CASC,\"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China\",\"Thu Aug 06, 2020\"," +
            "Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,\"29.75 \",Success\n" +
            "2,SpaceX,\"Pad A, Boca Chica, Texas, USA\",\"Tue Aug 04, 2020\",Starship Prototype | 150 Meter Hop," +
            "StatusActive,,Failure\n" +
            "3,Roscosmos,\"Site 200/39, Baikonur Cosmodrome, Kazakhstan\",\"Thu Jul 30, 2020\"," +
            "Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,\"65.0 \",Success\n" +
            "5,CASC,\"LC-9, Taiyuan Satellite Launch Center, China\",\"Sat Jul 25, 2020\"," +
            "\"Long March 4B | Ziyuan-3 03, Apocalypse-10 & NJU-HKU 1\",StatusActive,\"64.68 \",Success\n" +
            "246,ULA,\"SLC-3E, Vandenberg AFB, California, USA\",\"Sat May 05, 2018\"," +
            "Atlas V 401 | InSight,StatusActive,\"109.0 \",Success\n" +
            "247,CASC,\"LC-2, Xichang Satellite Launch Center, China\",\"Thu May 03, 2018\"," +
            "Long March 3B/E | Apstar 6C,StatusActive,\"29.15 \",Success\n" +
            "248,Blue Origin,\"Blue Origin Launch Site, West Texas, Texas, USA\",\"Sun Apr 29, 2018\"," +
            "New Shepard | NS-8,StatusActive,,Success\n";

        initializeMJTSpaceScanner(missionsData);

        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to = LocalDate.of(2020, 12, 31);

        String company = spaceScanner.getCompanyWithMostSuccessfulMissions(from, to);

        assertEquals("CASC", company);
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsReturnsEmptyStringWhenNoMissionsExistInPassedTimePeriod() {
        initializeMJTSpaceScanner();

        LocalDate from = LocalDate.of(1000, 1, 1);
        LocalDate to = LocalDate.of(1000, 12, 31);

        String company = spaceScanner.getCompanyWithMostSuccessfulMissions(from, to);

        assertTrue(company.isEmpty());
    }


    @Test
    void testGetCompanyWithMostSuccessfulMissionsThrowsWhenPassedNull() {
        initializeMJTSpaceScanner();

        assertThrows(IllegalArgumentException.class,
            () -> spaceScanner.getCompanyWithMostSuccessfulMissions(null, null));
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionsThrowsWhenPassedInvalidTimePeriod() {
        initializeMJTSpaceScanner();

        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to = LocalDate.of(2018, 12, 31);

        assertThrows(TimeFrameMismatchException.class,
            () -> spaceScanner.getCompanyWithMostSuccessfulMissions(from, to));
    }

    @Test
    void testGetMissionsPerCountryReturnsCorrectMap() {
        initializeMJTSpaceScanner();

        Map<String, Collection<Mission>> missionsPerCountry = spaceScanner.getMissionsPerCountry();

        assertEquals(3, missionsPerCountry.size());
        assertTrue(missionsPerCountry.containsKey("USA"));
        assertTrue(missionsPerCountry.containsKey("China"));
        assertTrue(missionsPerCountry.containsKey("Kazakhstan"));
        assertEquals(3, missionsPerCountry.get("USA").size());
        assertEquals(1, missionsPerCountry.get("China").size());
        assertEquals(1, missionsPerCountry.get("Kazakhstan").size());
    }

    @Test
    void testGetMissionsPerCountryReturnsEmptyMapWhenNoMissionsExist() {
        initializeMJTSpaceScanner("");

        Map<String, Collection<Mission>> missionsPerCountry = spaceScanner.getMissionsPerCountry();

        assertTrue(missionsPerCountry.isEmpty());
    }

    @Test
    void testGetTopNLeastExpensiveMissionsReturnsCorrectListWhenPassedSmallerNThanMissionsAvailable() {
        String missionsData = "Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," +
            "\" Rocket\",Status Mission\n" +
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\"," +
            "Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Success\n" +
            "1,CASC,\"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China\",\"Thu Aug 06, 2020\"," +
            "Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,\"29.75 \",Success\n" +
            "2,SpaceX,\"Pad A, Boca Chica, Texas, USA\",\"Tue Aug 04, 2020\",Starship Prototype | 150 Meter Hop," +
            "StatusActive,\"12.0 \",Failure\n" +
            "3,Roscosmos,\"Site 200/39, Baikonur Cosmodrome, Kazakhstan\",\"Thu Jul 30, 2020\"," +
            "Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,,Success\n" +
            "5,CASC,\"LC-9, Taiyuan Satellite Launch Center, China\",\"Sat Jul 25, 2020\"," +
            "\"Long March 4B | Ziyuan-3 03, Apocalypse-10 & NJU-HKU 1\",StatusActive,\"64.68 \",Success\n" +
            "29,MHI,\"LA-Y2, Tanegashima Space Center, Japan\",\"Wed May 20, 2020\"," +
            "H-IIB | HTV-9,StatusRetired,\"112.5 \",Success\n" +
            "33,Roscosmos,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Sat Apr 25, 2020\"," +
            "Soyuz 2.1a | Progress MS-14,StatusActive,\"48.5 \",Success\n";

        initializeMJTSpaceScanner(missionsData);

        List<Mission> missions = spaceScanner.getTopNLeastExpensiveMissions(3, MissionStatus.SUCCESS,
            RocketStatus.STATUS_ACTIVE);

        assertEquals(3, missions.size());
        missions.forEach(m -> assertTrue(m.cost().isPresent()));
        missions.forEach(m -> assertTrue(m.cost().get().compareTo(0.0) > 0));
        missions.forEach(m -> assertEquals(MissionStatus.SUCCESS, m.missionStatus()));
        missions.forEach(m -> assertEquals(RocketStatus.STATUS_ACTIVE, m.rocketStatus()));
    }

    @Test
    void testGetTopNLeastExpensiveMissionsReturnsCorrectListWhenPassedBiggerNThanMissionsAvailable() {
        String missionsData = "Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," +
            "\" Rocket\",Status Mission\n" +
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\"," +
            "Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Success\n" +
            "1,CASC,\"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China\",\"Thu Aug 06, 2020\"," +
            "Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,\"29.75 \",Success\n" +
            "2,SpaceX,\"Pad A, Boca Chica, Texas, USA\",\"Tue Aug 04, 2020\",Starship Prototype | 150 Meter Hop," +
            "StatusActive,\"12.0 \",Failure\n" +
            "3,Roscosmos,\"Site 200/39, Baikonur Cosmodrome, Kazakhstan\",\"Thu Jul 30, 2020\"," +
            "Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,,Success\n" +
            "5,CASC,\"LC-9, Taiyuan Satellite Launch Center, China\",\"Sat Jul 25, 2020\"," +
            "\"Long March 4B | Ziyuan-3 03, Apocalypse-10 & NJU-HKU 1\",StatusActive,\"64.68 \",Success\n" +
            "29,MHI,\"LA-Y2, Tanegashima Space Center, Japan\",\"Wed May 20, 2020\"," +
            "H-IIB | HTV-9,StatusRetired,\"112.5 \",Success\n" +
            "33,Roscosmos,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Sat Apr 25, 2020\"," +
            "Soyuz 2.1a | Progress MS-14,StatusActive,\"48.5 \",Success\n";

        initializeMJTSpaceScanner(missionsData);

        List<Mission> missions = spaceScanner.getTopNLeastExpensiveMissions(100, MissionStatus.SUCCESS,
            RocketStatus.STATUS_ACTIVE);

        assertEquals(4, missions.size());
        missions.forEach(m -> assertTrue(m.cost().isPresent()));
        missions.forEach(m -> assertTrue(m.cost().get().compareTo(0.0) > 0));
        missions.forEach(m -> assertEquals(MissionStatus.SUCCESS, m.missionStatus()));
        missions.forEach(m -> assertEquals(RocketStatus.STATUS_ACTIVE, m.rocketStatus()));
    }

    @Test
    void testGetTopNLeastExpensiveMissionsThrowsWhenNIsNegative() {
        initializeMJTSpaceScanner();

        assertThrows(IllegalArgumentException.class, () -> spaceScanner.getTopNLeastExpensiveMissions(-3,
            MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE));
    }

    @Test
    void testGetTopNLeastExpensiveMissionsThrowsWhenPassedNull() {
        initializeMJTSpaceScanner();

        assertThrows(IllegalArgumentException.class, () -> spaceScanner.getTopNLeastExpensiveMissions(3,
            null, null));
    }

    @Test
    void testGetTopNLeastExpensiveMissionsReturnsEmptyListWhenNoMissionsWithGivenStatus() {
        initializeMJTSpaceScanner();

        List<Mission> missions = spaceScanner.getTopNLeastExpensiveMissions(3, MissionStatus.PARTIAL_FAILURE,
            RocketStatus.STATUS_ACTIVE);

        assertTrue(missions.isEmpty());
    }

    @Test
    void testGetTopNLeastExpensiveMissionsReturnsEmptyListWhenNoRocketsWithGivenStatus() {
        initializeMJTSpaceScanner();

        List<Mission> missions = spaceScanner.getTopNLeastExpensiveMissions(3, MissionStatus.SUCCESS,
            RocketStatus.STATUS_RETIRED);

        assertTrue(missions.isEmpty());
    }

    @Test
    void testGetMostDesiredLocationForMissionsPerCompanyReturnsCorrectMap() {
        String missionsData = "Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," +
            "\" Rocket\",Status Mission\n" +
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\"," +
            "Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Success\n" +
            "1,CASC,\"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China\",\"Thu Aug 06, 2020\"," +
            "Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,\"29.75 \",Success\n" +
            "2,SpaceX,\"Pad A, Boca Chica, Texas, USA\",\"Tue Aug 04, 2020\",Starship Prototype | 150 Meter Hop," +
            "StatusActive,\"12.0 \",Failure\n" +
            "3,Roscosmos,\"Site 200/39, Baikonur Cosmodrome, Kazakhstan\",\"Thu Jul 30, 2020\"," +
            "Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,,Success\n" +
            "5,CASC,\"LC-9, Taiyuan Satellite Launch Center, China\",\"Sat Jul 25, 2020\"," +
            "\"Long March 4B | Ziyuan-3 03, Apocalypse-10 & NJU-HKU 1\",StatusActive,\"64.68 \",Success\n" +
            "29,MHI,\"LA-Y2, Tanegashima Space Center, Japan\",\"Wed May 20, 2020\"," +
            "H-IIB | HTV-9,StatusRetired,\"112.5 \",Success\n" +
            "33,Roscosmos,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Sat Apr 25, 2020\"," +
            "Soyuz 2.1a | Progress MS-14,StatusActive,\"48.5 \",Success\n" +
            "60,CASC,\"LC-9, Taiyuan Satellite Launch Center, China\",\"Wed Jan 15, 2020\"," +
            "Long March 2D | Jilin-1 Wideband 01 & ??uSat-7/8,StatusActive,\"29.75 \",Success\n" +
            "61,CASC,\"LC-2, Xichang Satellite Launch Center, China\",\"Tue Jan 07, 2020\"," +
            "Long March 3B/E | TJSW-5,StatusActive,\"29.15 \",Success\n" +
            "62,SpaceX,\"SLC-40, Cape Canaveral AFS, Florida, USA\",\"Tue Jan 07, 2020\"," +
            "Falcon 9 Block 5 | Starlink V1 L2,StatusActive,\"50.0 \",Success\n" +
            "63,CASC,\"LC-101, Wenchang Satellite Launch Center, China\",\"Fri Dec 27, 2019\"," +
            "Long March 5 | Shijian-20,StatusActive,,Success\n" +
            "65,Roscosmos,\"Site 81/24, Baikonur Cosmodrome, Kazakhstan\",\"Tue Dec 24, 2019\"," +
            "Proton-M/DM-3 | Elektro-L n†\u00AD3,StatusActive,\"65.0 \",Success\n" +
            "76,Roscosmos,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Fri Dec 06, 2019\"," +
            "Soyuz 2.1a | Progress MS-13 (74P),StatusActive,\"48.5 \",Success\n";

        initializeMJTSpaceScanner(missionsData);

        Map<String, String> mostDesiredLocationPerCompany = spaceScanner.getMostDesiredLocationForMissionsPerCompany();

        assertEquals(4, mostDesiredLocationPerCompany.size());
        assertEquals("Site 31/6, Baikonur Cosmodrome, Kazakhstan",
            mostDesiredLocationPerCompany.get("Roscosmos"));
        assertEquals("LC-9, Taiyuan Satellite Launch Center, China",
            mostDesiredLocationPerCompany.get("CASC"));
    }

    @Test
    void testGetMostDesiredLocationForMissionsPerCompanyReturnsEmptyMapWhenNoMissionsAvailable() {
        initializeMJTSpaceScanner("");

        Map<String, String> mostDesiredLocationPerCompany = spaceScanner.getMostDesiredLocationForMissionsPerCompany();

        assertTrue(mostDesiredLocationPerCompany.isEmpty());
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyReturnsCorrectCollection() {
        String missionsData = "Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," +
            "\" Rocket\",Status Mission\n" +
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\"," +
            "Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Success\n" +
            "1,CASC,\"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China\",\"Thu Aug 06, 2020\"," +
            "Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,\"29.75 \",Success\n" +
            "2,SpaceX,\"SLC-40, Cape Canaveral AFS, Florida, USA\",\"Tue Aug 04, 2020\"," +
            "Starship Prototype | 150 Meter Hop,StatusActive,\"12.0 \",Failure\n" +
            "3,Roscosmos,\"Site 200/39, Baikonur Cosmodrome, Kazakhstan\",\"Thu Jul 30, 2020\"," +
            "Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,,Success\n" +
            "5,CASC,\"LC-9, Taiyuan Satellite Launch Center, China\",\"Sat Jul 25, 2020\"," +
            "\"Long March 4B | Ziyuan-3 03, Apocalypse-10 & NJU-HKU 1\",StatusActive,\"64.68 \",Success\n" +
            "29,MHI,\"LA-Y2, Tanegashima Space Center, Japan\",\"Wed May 20, 2020\"," +
            "H-IIB | HTV-9,StatusRetired,\"112.5 \",Success\n" +
            "33,Roscosmos,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Sat Apr 25, 2020\"," +
            "Soyuz 2.1a | Progress MS-14,StatusActive,\"48.5 \",Success\n" +
            "60,CASC,\"LC-9, Taiyuan Satellite Launch Center, China\",\"Wed Jan 15, 2020\"," +
            "Long March 2D | Jilin-1 Wideband 01 & ??uSat-7/8,StatusActive,\"29.75 \",Success\n" +
            "61,CASC,\"LC-2, Xichang Satellite Launch Center, China\",\"Tue Jan 07, 2020\"," +
            "Long March 3B/E | TJSW-5,StatusActive,\"29.15 \",Success\n" +
            "62,SpaceX,\"SLC-40, Cape Canaveral AFS, Florida, USA\",\"Tue Jan 07, 2020\"," +
            "Falcon 9 Block 5 | Starlink V1 L2,StatusActive,\"50.0 \",Success\n" +
            "63,CASC,\"LC-101, Wenchang Satellite Launch Center, China\",\"Fri Dec 27, 2019\"," +
            "Long March 5 | Shijian-20,StatusActive,,Success\n" +
            "65,Roscosmos,\"Site 81/24, Baikonur Cosmodrome, Kazakhstan\",\"Tue Dec 24, 2019\"," +
            "Proton-M/DM-3 | Elektro-L n†\u00AD3,StatusActive,\"65.0 \",Success\n" +
            "76,Roscosmos,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Fri Dec 06, 2019\"," +
            "Soyuz 2.1a | Progress MS-13 (74P),StatusActive,\"48.5 \",Success\n" +
            "37,Roscosmos,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Thu Apr 09, 2020\"," +
            "Soyuz 2.1a | Soyuz MS-16,StatusActive,\"48.5 \",Success\n";

        initializeMJTSpaceScanner(missionsData);

        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to = LocalDate.of(2020, 12, 31);

        Map<String, String> mostSuccessfulMissionsPerCompany =
            spaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(from, to);

        assertEquals(4, mostSuccessfulMissionsPerCompany.size());
        assertTrue(mostSuccessfulMissionsPerCompany.containsKey("SpaceX"));
        assertTrue(mostSuccessfulMissionsPerCompany.containsKey("CASC"));
        assertTrue(mostSuccessfulMissionsPerCompany.containsKey("Roscosmos"));
        assertTrue(mostSuccessfulMissionsPerCompany.containsKey("MHI"));
        assertEquals("SLC-40, Cape Canaveral AFS, Florida, USA",
            mostSuccessfulMissionsPerCompany.get("SpaceX"));
        assertEquals("LC-9, Taiyuan Satellite Launch Center, China",
            mostSuccessfulMissionsPerCompany.get("CASC"));
        assertEquals("Site 31/6, Baikonur Cosmodrome, Kazakhstan",
            mostSuccessfulMissionsPerCompany.get("Roscosmos"));
        assertEquals("LA-Y2, Tanegashima Space Center, Japan",
            mostSuccessfulMissionsPerCompany.get("MHI"));
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyThrowsWhenPassedNull() {
        initializeMJTSpaceScanner();

        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to = LocalDate.of(2000, 12, 31);

        assertThrows(TimeFrameMismatchException.class,
            () -> spaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(from, to));
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyThrowsWhenInvalidTimePeriod() {
        initializeMJTSpaceScanner();

        assertThrows(IllegalArgumentException.class,
            () -> spaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(null, null));
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyReturnsEmptyMapWhenNoMissionsAvailable() {
        initializeMJTSpaceScanner("");

        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to = LocalDate.of(2020, 12, 31);

        Map<String, String> mostSuccessfulMissionsPerCompany =
            spaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(from, to);

        assertTrue(mostSuccessfulMissionsPerCompany.isEmpty());
    }

    @Test
    void testGetTopNTallestRocketsReturnsCorrectCollection() {
        initializeMJTSpaceScanner();

        List<Rocket> topNTallestRockets = spaceScanner.getTopNTallestRockets(4);

        assertEquals(4, topNTallestRockets.size());
        assertEquals(39.0, topNTallestRockets.get(0).height().get());
        assertEquals(38.7, topNTallestRockets.get(1).height().get());
        assertEquals(32.0, topNTallestRockets.get(2).height().get());
        assertEquals(28.0, topNTallestRockets.get(3).height().get());
    }

    @Test
    void testGetTopNTallestRocketsThrowsWhenPassedNegativeNumber() {
        initializeMJTSpaceScanner();

        assertThrows(IllegalArgumentException.class, () -> spaceScanner.getTopNTallestRockets(-4));
    }

    @Test
    void testGetWikiPagePerRocketReturnsCorrectCollection() {
        String rocketsData = """
            "",Name,Wiki,Rocket Height
            0,Tsyklon-3,https://en.wikipedia.org/wiki/Tsyklon-3,39.0 m
            1,Tsyklon-4M,https://en.wikipedia.org/wiki/Cyclone-4M,38.7 m
            2,Unha-2,,28.0 m
            """;
        initializeMJTSpaceScanner("", rocketsData);

        Map<String, Optional<String>> wikiPagePerRocket = spaceScanner.getWikiPageForRocket();

        assertEquals(3, wikiPagePerRocket.size());
        assertTrue(wikiPagePerRocket.containsKey("Tsyklon-3"));
        assertTrue(wikiPagePerRocket.containsKey("Tsyklon-4M"));
        assertTrue(wikiPagePerRocket.containsKey("Unha-2"));
        assertTrue(wikiPagePerRocket.get("Tsyklon-3").isPresent());
        assertTrue(wikiPagePerRocket.get("Tsyklon-4M").isPresent());
        assertEquals("https://en.wikipedia.org/wiki/Tsyklon-3", wikiPagePerRocket.get("Tsyklon-3").get());
        assertEquals("https://en.wikipedia.org/wiki/Cyclone-4M", wikiPagePerRocket.get("Tsyklon-4M").get());
        assertTrue(wikiPagePerRocket.get("Unha-2").isEmpty());
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissions() {
        String missionsData = "Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," +
            "\" Rocket\",Status Mission\n" +
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\"," +
            "Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Success\n" +
            "1,CASC,\"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China\",\"Thu Aug 06, 2020\"," +
            "Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,\"29.75 \",Success\n" +
            "2,SpaceX,\"Pad A, Boca Chica, Texas, USA\",\"Tue Aug 04, 2020\",Starship Prototype | 150 Meter Hop," +
            "StatusActive,,Failure\n" +
            "3,Roscosmos,\"Site 200/39, Baikonur Cosmodrome, Kazakhstan\",\"Thu Jul 30, 2020\"," +
            "Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,\"65.0 \",Success\n" +
            "4,ULA,\"SLC-41, Cape Canaveral AFS, Florida, USA\",\"Thu Jul 30, 2020\",Atlas V 541 | Perseverance," +
            "StatusActive,\"145.0 \",Success\n" +
            "2430,RVSN USSR,\"Site 132/1, Plesetsk Cosmodrome, Russia\",\"Fri Jan 16, 1981\"," +
            "Cosmos-3M (11K65M) | Cosmos 1238,StatusRetired,,Success\n" +
            "62,SpaceX,\"SLC-40, Cape Canaveral AFS, Florida, USA\",\"Tue Jan 07, 2020\"," +
            "Falcon 9 Block 5 | Starlink V1 L2,StatusActive,\"51.0 \",Success\n" +
            "63,CASC,\"LC-101, Wenchang Satellite Launch Center, China\",\"Fri Dec 27, 2019\"," +
            "Long March 5 | Shijian-20,StatusActive,,Success\n";

        String rocketsData = """
            "",Name,Wiki,Rocket Height
            169,Falcon 9 Block 5,https://en.wikipedia.org/wiki/Falcon_9,70.0 m
            213,Long March 2D,https://en.wikipedia.org/wiki/Long_March_2D,41.06 m
            371,Starship Prototype,https://en.wikipedia.org/wiki/SpaceX_Starship,50.0 m
            294,Proton-M/Briz-M,https://en.wikipedia.org/wiki/Proton-M,58.2 m
            103,Atlas V 541,https://en.wikipedia.org/wiki/Atlas_V,62.2 m
            230,Long March 5,https://en.wikipedia.org/wiki/Long_March_5,57.0 m
            """;

        initializeMJTSpaceScanner(missionsData, rocketsData);

        List<String> wikiPagesForRocketsUsedInMostExpensiveMissions =
            spaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(3,
                MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE);

        assertEquals(3, wikiPagesForRocketsUsedInMostExpensiveMissions.size());
        assertTrue(wikiPagesForRocketsUsedInMostExpensiveMissions.containsAll(
            List.of("https://en.wikipedia.org/wiki/Falcon_9", "https://en.wikipedia.org/wiki/Proton-M",
                "https://en.wikipedia.org/wiki/Atlas_V")));
    }

    @Test
    void testSaveMostReliableRocketWorksCorrectly() {
        String missionsData = "Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," +
            "\" Rocket\",Status Mission\n" +
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\"," +
            "Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Failure\n" +
            "1,CASC,\"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China\",\"Thu Aug 06, 2020\"," +
            "Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,\"29.75 \",Failure\n" +
            "2,SpaceX,\"Pad A, Boca Chica, Texas, USA\",\"Tue Aug 04, 2020\"," +
            "Starship Prototype | 150 Meter Hop,StatusActive,,Failure\n" +
            "3,Roscosmos,\"Site 200/39, Baikonur Cosmodrome, Kazakhstan\",\"Thu Jul 30, 2020\"," +
            "Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,\"65.0 \",Failure\n" +
            "4,ULA,\"SLC-41, Cape Canaveral AFS, Florida, USA\",\"Thu Jul 30, 2020\"," +
            "Atlas V 541 | Perseverance,StatusActive,\"145.0 \",Failure\n" +
            "2430,RVSN USSR,\"Site 132/1, Plesetsk Cosmodrome, Russia\",\"Fri Jan 16, 1981\"," +
            "Cosmos-3M (11K65M) | Cosmos 1238,StatusRetired,,Failure\n" +
            "62,SpaceX,\"SLC-40, Cape Canaveral AFS, Florida, USA\",\"Tue Jan 07, 2020\"," +
            "Falcon 9 Block 5 | Starlink V1 L2,StatusActive,\"51.0 \",Success\n" +
            "63,CASC,\"LC-101, Wenchang Satellite Launch Center, China\",\"Fri Dec 27, 2019\"," +
            "Long March 5 | Shijian-20,StatusActive,,Failure\n";

        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            fail(e);
        }

        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();

        Rijndael rijndael = new Rijndael(secretKey);

        initializeMJTSpaceScanner(missionsData, secretKey);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        LocalDate from = LocalDate.of(2020, 1, 1);
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

        assertEquals("Falcon 9 Block 5", decryptedName.toString());
    }
}
