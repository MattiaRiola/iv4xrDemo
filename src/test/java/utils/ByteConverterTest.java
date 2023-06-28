package utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ByteConverterTest {

    @Test
    public void getShortFromTwoBytes() {
        byte[] byteArray = new byte[2];
        byteArray[0] = 0;  //0000 0000
        byteArray[1] = 3;  //0000 0011
        byte high = byteArray[0];
        byte low = byteArray[1];
        short shortValue = FileExplorer.getShortFromTwoBytes(high, low);
        Assertions.assertEquals(3, shortValue);
    }

    @Test
    public void fromBytesToShortsBigEndian() {
        byte[] byteArray = new byte[4];
        byteArray[0] = 0;  //0000 0000
        byteArray[1] = 3;  //0000 0011
        byteArray[2] = 1;  //0000 0001
        byteArray[3] = 2;  //0000 0010
        //BIG ENDIAN
        //0000 0001 0000 0011 = 2^1 + 2^0 = 3
        //0000 0001 0000 0010 = 2^8 + 2 = 258


        short[] shortArray = FileExplorer.getShortsFromByteArray(byteArray, true);

        Assertions.assertEquals(2, shortArray.length);
        Assertions.assertEquals(3, shortArray[0]);
        Assertions.assertEquals(258, shortArray[1]);

    }

    @Test
    public void fromBytesToShortsLittleEndian() {
        byte[] byteArray = new byte[6];
        byteArray[0] = 0;  //0000 0000
        byteArray[1] = 3;  //0000 0011
        byteArray[2] = 1;  //0000 0001
        byteArray[3] = 2;  //0000 0010
        byteArray[4] = 2;  //0000 0010
        byteArray[5] = -126;  //1000 0010 = - 2^7 + 2 = -126

        //LITTLE ENDIAN
        //0000 0011 0000 0000 = 2^9 + 2^8 = 512 + 256 = 768
        //0000 0010 0000 0001 = 2^9 + 2^0 = 512 + 1 = 513
        //1000 0010 0000 0010 = - 2^15 + 2^9 + 2^1 = -32768 + 512 + 2 = -32254


        short[] shortArray = FileExplorer.getShortsFromByteArray(byteArray, false);

        Assertions.assertEquals(3, shortArray.length);
        Assertions.assertEquals(768, shortArray[0]);
        Assertions.assertEquals(513, shortArray[1]);
        Assertions.assertEquals(-32254, shortArray[2]);

    }
}
