package bg.sofia.uni.fmi.mjt.space.mission;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DetailTest {
    @Test
    void testOfInitializesDetailCorrectly() {
        String line = "Falcon 9 Block 5 | Starlink V1 L9 & BlackSky";

        Detail detail = Detail.of(line);

        assertEquals("Falcon 9 Block 5", detail.rocketName());
        assertEquals("Starlink V1 L9 & BlackSky", detail.payload());
    }
}
