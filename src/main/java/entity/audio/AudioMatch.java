package entity.audio;

public class AudioMatch {


    public final double recordTime;
    public final double dbTime;
    public final String name;
    public long hash;

    public AudioMatch(String name, double recordTime, double dbTime, long hash) {
        this.name = name;
        this.recordTime = recordTime;
        this.dbTime = dbTime;
        this.hash = hash;
    }

    public double getRecordTime() {
        return recordTime;
    }

    public double getDbTime() {
        return dbTime;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return " At " + String.format(" (%.3f)", recordTime) + ": " +
                name + String.format(" (%.3f)", dbTime) +
                " hash: " + hash;
    }

}
