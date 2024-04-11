package cc.zjyun.有序性测试;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

/**
 * @author zijian Wang
 * @Description 使用jcstress测试指令重排序
 */

@JCStressTest // 标记此类为一个并发测试类
@Outcome(id = {"0"}, expect = Expect.ACCEPTABLE_INTERESTING, desc = "错误结果") // 描述测试结果
@Outcome(id = {"-1", "5"}, expect = Expect.ACCEPTABLE, desc = "正常结果") // 描述测试结果
@State
public class TestInstructionReorder {

    private boolean flag;
    private int x;

    public TestInstructionReorder() {
    }

    @Actor
    public void actor1(I_Result r) {
        if (flag) {
            r.r1 = x;
        } else {
            r.r1 = -1;
        }
    }

    @Actor
    public void actor2(I_Result r) {
        this.x = 5;
        flag = true;
    }
}
