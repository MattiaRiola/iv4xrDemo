package utils;

public enum SoundFileNames {
    FIRESIZZLE("firesizzle.wav"),
    DING1("ding1.wav"),
    DING2("ding2.wav"),
    MONSTERATTACK("monsterattack.wav"),
    TENSION("tension.wav"),
    DEATH("death.wav");

    private String fileName;

    SoundFileNames(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return this.fileName;
    }


}
