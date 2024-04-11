package cc.zjyun.原子性测试;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.Z_Result;

/**
 * 使用Synchronized 锁
 * <p>
 * true	84229010	83930770	78127910	72798770	68640410	11646070	ACCEPTABLE
 *
 * @author zijian Wang
 * @Description 使用 jcstress 测试程序的原子性问题
 */

@JCStressTest
@Outcome(id = {"false"}, expect = Expect.ACCEPTABLE_INTERESTING, desc = "出现了赋值后原子性问题：false")
@Outcome(id = {"true"}, expect = Expect.ACCEPTABLE, desc = "赋值正常！")
@State
public class TestAtomicityReorderUsedLock {

    private final Object lock = new Object();
    private boolean flag = true;

    public TestAtomicityReorderUsedLock() {
    }

    @Actor
    public void setTrue(Z_Result r) {
        synchronized (lock) {
            flag = true;
            r.r1 = flag;
        }
    }

    @Actor
    public void setFlase() {
        synchronized (lock) {
            flag = false;
        }
    }
}
