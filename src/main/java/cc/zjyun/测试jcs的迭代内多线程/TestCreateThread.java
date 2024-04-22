package cc.zjyun.测试jcs的迭代内多线程;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.util.stream.IntStream;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

public class TestCreateThread {

    @JCStressTest
    @Outcome(id = "10", expect = ACCEPTABLE, desc = "线程的数量")
    @Outcome(expect = FORBIDDEN, desc = "其他的值。")
    @State
    public  static class Test {
        private int threadCount = 0;
        @Actor
        public void createThread() {
            IntStream.range(0, 10).forEach(x -> {
                Thread thread = new Thread(() -> {
                    System.out.println("do something....");
                });
                thread.start();
                threadCount++;
            });
        }

        @Arbiter
        public void result(I_Result result) {
            result.r1 = threadCount;
        }
    }
}
