package utils;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StopWatchTest {

    @Test
    void stopWatchTest() throws InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Thread.sleep(1000);
        stopWatch.split();
        var splitTime1 = stopWatch.getSplitTime();
        System.out.println(splitTime1);
        Assertions.assertTrue(splitTime1 > 1_000);
        Thread.sleep(2000);
        stopWatch.split();
        var splitTime2 = stopWatch.getSplitTime();
        Assertions.assertTrue(splitTime2 > 3_000);
        System.out.println(splitTime2);

    }


}
