package cc.zjyun.可见性;

import org.openjdk.jcstress.annotations.*;

/**
 * 可见性并发测试
 *
 * @author zijian Wang
 */
@JCStressTest(Mode.Termination)
@Outcome(id = "TERMINATED", expect = Expect.ACCEPTABLE, desc = "优雅正常的推出")
@Outcome(id = "STALE", expect = Expect.ACCEPTABLE_INTERESTING, desc = "测试卡住")
@State
public class NoStop {

    private boolean run = true;

    @Actor
    public void test1() throws InterruptedException {
        while (run) {
            // ....
        }
    }

    @Signal
    public void signal() {
        run = false;
    }
}
