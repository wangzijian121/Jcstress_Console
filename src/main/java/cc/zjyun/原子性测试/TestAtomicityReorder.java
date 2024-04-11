package cc.zjyun.原子性测试;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.Z_Result;

/**
 * 不使用锁的情况
 * Observed state   Occurrences              Expectation  Interpretation
 * false            25   ACCEPTABLE_INTERESTING  出现了赋值后原子性问题：false
 * true     1,934,115               ACCEPTABLE  赋值正常！
 *
 * @author zijian Wang
 * @Description 使用jcstress测试程序的原子性问题
 */

@JCStressTest
@Outcome(id = {"false"}, expect = Expect.ACCEPTABLE_INTERESTING, desc = "出现了赋值后原子性问题：false")
@Outcome(id = {"true"}, expect = Expect.ACCEPTABLE, desc = "赋值正常！")
@State
public class TestAtomicityReorder {

    private boolean flag = true;

    public TestAtomicityReorder() {
    }

    @Actor
    public void setTrue(Z_Result r) {
        flag = true;
        r.r1 = flag;
    }

    @Actor
    public void setFlase() {
        flag = false;

    }
}
