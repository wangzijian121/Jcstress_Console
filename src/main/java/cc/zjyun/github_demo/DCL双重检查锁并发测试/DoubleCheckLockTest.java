package cc.zjyun.github_demo.DCL双重检查锁并发测试;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import static org.openjdk.jcstress.annotations.Expect.*;

/**
 * @author zijian Wang
 */
public class DoubleCheckLockTest {

    @JCStressTest
    @Outcome(id = "1", expect = ACCEPTABLE, desc = "只初始化了1次对象.")
    @Outcome(expect = FORBIDDEN, desc = "其他情况是禁止的")
    @State
    public static class DoubleCheckLockSingleton {

        private int i;
        private static int createInstanceCount;
        private final static Object lock = new Object();
        private static DoubleCheckLockSingleton doubleCheckLockSingleton = null;

        @Actor
        public DoubleCheckLockSingleton getInstance(I_Result result) {
            if (doubleCheckLockSingleton == null) {
                synchronized (lock) {
                    if (doubleCheckLockSingleton == null) {
                        createInstanceCount++;
                        doubleCheckLockSingleton = new DoubleCheckLockSingleton();
                    }
                }
            }
            result.r1 = createInstanceCount;
            return doubleCheckLockSingleton;
        }

    }
}
