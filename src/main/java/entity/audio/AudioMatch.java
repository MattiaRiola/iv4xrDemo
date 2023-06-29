package entity.audio;

import java.util.Set;

public class AudioMatch {

    private final ChunkDetail inputChunk;

    private final Set<ChunkDetail> dbChunks;


    public AudioMatch(ChunkDetail inputChunk, Set<ChunkDetail> dbChunks) {
        this.inputChunk = inputChunk;
        this.dbChunks = dbChunks;
    }

    public ChunkDetail getInputChunk() {
        return inputChunk;
    }

    public Set<ChunkDetail> getDbChunks() {
        return dbChunks;
    }

    @Override
    public String toString() {

        return " At " + inputChunk + " matches: " +
                dbChunks;
    }

}
