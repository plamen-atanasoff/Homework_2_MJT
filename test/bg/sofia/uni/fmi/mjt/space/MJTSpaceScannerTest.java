package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MJTSpaceScannerTest {
    @Test
    void testMJTSpaceScannerReadersInitializeCorrectlyMissionsAndReaders() {
        Reader missionReader = new StringReader("Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," +
            "\" Rocket\",Status Mission\n" +
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\"," +
            "Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Success\n" +
            "1,CASC,\"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China\",\"Thu Aug 06, 2020\"," +
            "Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,\"29.75 \",Success\n" +
            "2,SpaceX,\"Pad A, Boca Chica, Texas, USA\",\"Tue Aug 04, 2020\",Starship Prototype | 150 Meter Hop," +
            "StatusActive,,Success\n" +
            "3,Roscosmos,\"Site 200/39, Baikonur Cosmodrome, Kazakhstan\",\"Thu Jul 30, 2020\"," +
            "Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,\"65.0 \",Success\n" +
            "4,ULA,\"SLC-41, Cape Canaveral AFS, Florida, USA\",\"Thu Jul 30, 2020\",Atlas V 541 | Perseverance," +
            "StatusActive,\"145.0 \",Success\n");

        Reader rocketReader = new StringReader("\"\",Name,Wiki,Rocket Height\n" +
            "0,Tsyklon-3,https://en.wikipedia.org/wiki/Tsyklon-3,39.0 m\n" +
            "1,Tsyklon-4M,https://en.wikipedia.org/wiki/Cyclone-4M,38.7 m\n" +
            "2,Unha-2,https://en.wikipedia.org/wiki/Unha,28.0 m\n" +
            "3,Unha-3,https://en.wikipedia.org/wiki/Unha,32.0 m\n" +
            "4,Vanguard,https://en.wikipedia.org/wiki/Vanguard_(rocket),23.0 m\n" +
            "5,Vector-H,https://en.wikipedia.org/wiki/Vector-H,18.3 m\n" +
            "6,Vector-R,https://en.wikipedia.org/wiki/Vector-R,13.0 m\n");

        SecretKey secretKey = null;

        MJTSpaceScanner res = new MJTSpaceScanner(missionReader, rocketReader, secretKey);

        Collection<Mission> missions = res.getAllMissions();
        Collection<Rocket> rockets = res.getAllRockets();

        assertEquals(5, missions.size());
        assertEquals(7, rockets.size());
    }

    @Test
    void testMJTSpaceScannerReadersInitializeCorrectlyMissionsAndReadersWithBigFiles() {
        Path missionPath = Path.of("F:\\Java\\Projects\\Homework_2_MJT\\all-missions-from-1957.csv");
        Path rocketPath = Path.of("F:\\Java\\Projects\\Homework_2_MJT\\all-rockets-from-1957.csv");

        Reader missionReader = null;
        Reader rocketReader = null;
        try {
            missionReader = Files.newBufferedReader(missionPath);
            rocketReader = Files.newBufferedReader(rocketPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SecretKey secretKey = null;

        MJTSpaceScanner res = new MJTSpaceScanner(missionReader, rocketReader, secretKey);

        Collection<Mission> missions = res.getAllMissions();
        Collection<Rocket> rockets = res.getAllRockets();

        assertEquals(4324, missions.size());
        assertEquals(416, rockets.size());
    }
}
