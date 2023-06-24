package service.audio;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioRecordTest {

    @Test
    public void bigendtolittleend() {
        byte low = 2;
        byte high = 1;
        ByteBuffer bb = ByteBuffer.allocate(2).put(high).put(low)
                .order(ByteOrder.LITTLE_ENDIAN);
        int newlow = reverseBitsByte(bb.get(0));
        int newhigh = reverseBitsByte(bb.get(1));
        int oldInt = (high << 8) + (low & 0x00ff);
        int newInt = (newhigh << 8) + (newlow & 0x00ff);
    }

    public byte reverseBitsByte(byte x) {
        int intSize = 8;
        byte y = 0;
        for (int position = intSize - 1; position > 0; position--) {
            y += ((x & 1) << position);
            x >>= 1;
        }
        return y;
    }
}
