package bg.sofia.uni.fmi.mjt.space.rocket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RocketTest {
    @Test
    void testOfInitializesRocketCorrectly() {
        String line = "0,Tsyklon-3,https://en.wikipedia.org/wiki/Tsyklon-3,39.0 m";

        Rocket rocket = Rocket.of(line);

        assertEquals("0", rocket.id());
        assertEquals("Tsyklon-3", rocket.name());
        assertEquals("https://en.wikipedia.org/wiki/Tsyklon-3", rocket.wiki().get());
        assertEquals(39.0, rocket.height().get(), 0.0001);
    }

    @Test
    void testOfInitializesRocketCorrectlyWithoutWikiAndHeight() {
        String line = "0,Tsyklon-3,,";

        Rocket rocket = Rocket.of(line);

        assertEquals("0", rocket.id());
        assertEquals("Tsyklon-3", rocket.name());
        assertTrue(rocket.wiki().isEmpty());
        assertTrue(rocket.height().isEmpty());
    }

    @Test
    void testOfInitializesRocketCorrectlyWithoutWiki() {
        String line = "0,Tsyklon-3,,39.0 m";

        Rocket rocket = Rocket.of(line);

        assertEquals("0", rocket.id());
        assertEquals("Tsyklon-3", rocket.name());
        assertTrue(rocket.wiki().isEmpty());
        assertEquals(39.0, rocket.height().get(), 0.0001);
    }

    @Test
    void testOfInitializesRocketCorrectlyWithoutHeight() {
        String line = "0,Tsyklon-3,https://en.wikipedia.org/wiki/Tsyklon-3,";

        Rocket rocket = Rocket.of(line);

        assertEquals("0", rocket.id());
        assertEquals("Tsyklon-3", rocket.name());
        assertEquals("https://en.wikipedia.org/wiki/Tsyklon-3", rocket.wiki().get());
        assertTrue(rocket.height().isEmpty());
    }
}
