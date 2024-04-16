package cc.zjyun.有序性测试;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;

/**
 * @author zijian Wang
 * @Description 使用jcstress测试指令重排序
 */

public class TestInstructionReorder {
   /*指令重排序 导致   r.r1 = h1.a; r.r2 = h2.a; 执行顺序更改
        另一个微妙而直观的属性来自对程序如何工作的天真理解。
        在 Java 内存模型下，在没有同步的情况下，独立读取的顺序是未定义的。
        这包括对 *same* 变量的读取！

          RESULT      SAMPLES     FREQ       EXPECT  DESCRIPTION
            0, 0   14,577,607    6.96%   Acceptable  Doing both reads early. 先2 后1
            1, 1  194,792,419   93.02%   Acceptable  Doing both reads late. 先1 后2
            0, 1       24,942    0.01%   Acceptable  Doing first read early, not surprising. 先2 执行一半 后1
            1, 0        6,376   <0.01%  Interesting  First read seen racy value early, and the second one did ...
    */

    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "尽早进行两次读取.")//actor2先执行 都是0
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "做这两件事都读得很晚。")//actor1 先执行都是1
    @Outcome(id = "0, 1", expect = ACCEPTABLE_INTERESTING, desc = "做先读早，不足为奇。")//执行r.r2 = h2.a; 之前执行actor1 为1
    @Outcome(id = "1, 0", expect = ACCEPTABLE_INTERESTING, desc = "第一次阅读早早就看到了饶有趣味的价值，第二次阅读也是如此")
    // 先执行actor1 然后执行actor2 的r.r1 = h1.a; 在执行h1.trap = 0; h2.trap = 0; 在执行r.r2 = h2.a;
    @State
    public static class SameRead {
        private Object lock = new Object();

        private final Holder h1 = new Holder();
        private final Holder h2 = h1;

        private static class Holder {
            //可以使用 volatile 解决 1, 0 有序性情况
            int a;
            int trap;
        }

        @Actor
        public void actor1() {
//            h1.a = 1;
            synchronized (lock) {
                h1.a = 1;
            }
        }

        @Actor
        public void actor2(II_Result r) {
            Holder h1 = this.h1;
            Holder h2 = this.h2;

            // Spam null-pointer check folding: try to step on NPEs early.
            // 尽早执行此操作可以将编译器从移动 h1.a 和 h2.a 负载中解放出来周围，因为它不必再维护异常顺序。
            h1.trap = 0;
            h2.trap = 0;

       /*     r.r1 = h1.a;
            r.r2 = h2.a;*/
//            可以使用synchronized 解决(1,0和0，1) 的问题。
            synchronized (lock) {
                r.r1 = h1.a;
                r.r2 = h2.a;
            }
        }
    }
}
