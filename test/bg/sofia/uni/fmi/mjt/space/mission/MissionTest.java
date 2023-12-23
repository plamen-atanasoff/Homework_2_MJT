package bg.sofia.uni.fmi.mjt.space.mission;

import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MissionTest {
    @Test
    void testOfInitializesMissionCorrectly() {
        String line = "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\"," +
                      "Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Success\n";
        LocalDate date = LocalDate.of(2020, 8, 7);
        Detail detail = Detail.of("Falcon 9 Block 5 | Starlink V1 L9 & BlackSky");

        Mission mission = Mission.of(line);

        assertEquals("0", mission.id());
        assertEquals("SpaceX", mission.company());
        assertEquals("LC-39A, Kennedy Space Center, Florida, USA", mission.location());
        assertEquals(date, mission.date());
        assertEquals(detail, mission.detail());
        assertEquals(RocketStatus.STATUS_ACTIVE, mission.rocketStatus());
        assertEquals(50.0, mission.cost().get(), 0.0001);
        assertEquals(MissionStatus.SUCCESS, mission.missionStatus());
    }

    @Test
    void testOfInitializesMissionCorrectlyWithoutCost() {
        String line = "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\"," +
            "Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,,Success\n";
        LocalDate date = LocalDate.of(2020, 8, 7);
        Detail detail = Detail.of("Falcon 9 Block 5 | Starlink V1 L9 & BlackSky");

        Mission mission = Mission.of(line);

        assertEquals("0", mission.id());
        assertEquals("SpaceX", mission.company());
        assertEquals("LC-39A, Kennedy Space Center, Florida, USA", mission.location());
        assertEquals(date, mission.date());
        assertEquals(detail, mission.detail());
        assertEquals(RocketStatus.STATUS_ACTIVE, mission.rocketStatus());
        assertTrue(mission.cost().isEmpty());
        assertEquals(MissionStatus.SUCCESS, mission.missionStatus());
    }
}
