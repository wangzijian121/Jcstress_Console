package cc.zjyun.reentrantlock;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.util.stream.IntStream;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

public class ReentrantLockTestTest {
    @JCStressTest
    @Outcome(id = "10000", expect = ACCEPTABLE, desc = "正确的结果")
    @Outcome(expect = FORBIDDEN, desc = "其他的值。")
    @State
    public static class Test {
        private int threadCount = 0;

        @Actor
        public void createThread(I_Result result) {
            ReentrantLockTest reentrantLockTest = new ReentrantLockTest();
            try {
                int add = reentrantLockTest.add();
                result.r1 = add;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
