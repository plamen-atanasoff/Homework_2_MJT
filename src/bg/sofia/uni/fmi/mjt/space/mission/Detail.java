package bg.sofia.uni.fmi.mjt.space.mission;

public record Detail(String rocketName, String payload) {
    public static Detail of(String line) {
        String[] tokens = line.split("\\s*\\|\\s*");
        return new Detail(tokens[0], tokens[1]);
    }
}
