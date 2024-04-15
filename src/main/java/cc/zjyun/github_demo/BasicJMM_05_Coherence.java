/*
 * Copyright (c) 2016, 2021, Red Hat, Inc. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package cc.zjyun.github_demo;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;



import static org.openjdk.jcstress.annotations.Expect.*;

/**连贯性
 * @author zijian Wang
 */
public class BasicJMM_05_Coherence {

    /*相同读取
      ----------------------------------------------------------------------------------------------------------

        另一个微妙而直观的属性来自对程序如何工作的天真理解。
        在 Java 内存模型下，在没有同步的情况下，独立读取的顺序是未定义的。
        这包括对 *same* 变量的读取！

          RESULT      SAMPLES     FREQ       EXPECT  DESCRIPTION
            0, 0   14,577,607    6.96%   Acceptable  Doing both reads early.
            1, 1  194,792,419   93.02%   Acceptable  Doing both reads late.
            0, 1       24,942    0.01%   Acceptable  Doing first read early, not surprising.
            1, 0        6,376   <0.01%  Interesting  First read seen racy value early, and the second one did ...
    */

    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "尽早进行两次读取.")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "做这两件事都读得很晚。")//常识
    @Outcome(id = "0, 1", expect = ACCEPTABLE, desc = "做先读早，不足为奇。")
    @Outcome(id = "1, 0", expect = ACCEPTABLE_INTERESTING, desc = "第一次阅读早早就看到了饶有趣味的价值，第二次阅读也是如此")
    @State
    public static class SameRead {

        private final Holder h1 = new Holder();
        private final Holder h2 = h1;

        private static class Holder {
            int a;
            int trap;
        }

        @Actor
        public void actor1() {
            h1.a = 1;
        }

        @Actor
        public void actor2(II_Result r) {
            Holder h1 = this.h1;
            Holder h2 = this.h2;

            // Spam null-pointer check folding: try to step on NPEs early.
            // 尽早执行此操作可以将编译器从移动 h1.a 和 h2.a 负载中解放出来周围，因为它不必再维护异常顺序。
            h1.trap = 0;
            h2.trap = 0;

            // Spam alias analysis: the code effectively reads the same field twice,
            // but compiler does not know (h1 == h2) (i.e. does not check it, as
            // this is not a profitable opt for real code), so it issues two independent
            // loads.
            r.r1 = h1.a;
            r.r2 = h2.a;
            //常识h1.a h2.a应该都是 1
        }
    }

    /*
      ----------------------------------------------------------------------------------------------------------


        更强的属性 - 一致性 - 要求对同一变量的写入在总顺序（这意味着 _observers_ 也是有序的）。Java “volatile” 具有此属性。

          RESULT      SAMPLES     FREQ      EXPECT  DESCRIPTION
            0, 0  114,696,597   30.95%  Acceptable  Doing both reads early.
            0, 1    2,126,717    0.57%  Acceptable  Doing first read early, not surprising.
            1, 0            0    0.00%   Forbidden  Violates coherence.
            1, 1  253,704,430   68.47%  Acceptable  Doing both reads late.
     */

    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Doing both reads early.")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Doing both reads late.")
    @Outcome(id = "0, 1", expect = ACCEPTABLE, desc = "Doing first read early, not surprising.")
    @Outcome(id = "1, 0", expect = FORBIDDEN, desc = "Violates coherence.")
    @State
    public static class SameVolatileRead {

        private final Holder h1 = new Holder();
        private final Holder h2 = h1;

        private static class Holder {
            volatile int a;
            int trap;
        }

        @Actor
        public void actor1() {
            h1.a = 1;
        }

        @Actor
        public void actor2(II_Result r) {
            Holder h1 = this.h1;
            Holder h2 = this.h2;

            h1.trap = 0;
            h2.trap = 0;

            r.r1 = h1.a;
            r.r2 = h2.a;
        }
    }



}