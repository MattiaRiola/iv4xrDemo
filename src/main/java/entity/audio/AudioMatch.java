package entity.audio;

public class AudioMatch {


    public final double recordTime;
    public final double dbTime;
    public final String name;

    public AudioMatch(String name, double recordTime, double dbTime) {
        this.name = name;
        this.recordTime = recordTime;
        this.dbTime = dbTime;
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
}
