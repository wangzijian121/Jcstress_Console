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

import org.openjdk.jcstress.annotations.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static org.openjdk.jcstress.annotations.Expect.*;

/**
 * @author zijian Wang
 */
public class BasicJMM_04_Progress {



    /*出现可见性问题
      ----------------------------------------------------------------------------------------------------------

人们天真地期望对变量的写入最终是可见的。但是，在 Java 内存模型下，
        这不适用于普通读取和写入。通常的例子是平原中的繁忙循环。
        允许优化编译器检查该字段一次，如果它为“false”，则减少其余部分
        循环成“while（true）”，无限版本。

        Indeed, running this on just about any platform yields:

              RESULT  SAMPLES     FREQ       EXPECT  DESCRIPTION
               STALE        4   50.00%  Interesting  测试卡住
          TERMINATED        4   50.00%   Acceptable  优雅地完成
      */

    @JCStressTest(Mode.Termination)
    @Outcome(id = "TERMINATED", expect = ACCEPTABLE, desc = "优雅地完成")
    @Outcome(id = "STALE", expect = ACCEPTABLE_INTERESTING, desc = "测试卡住")
    @State
    public static class PlainSpin {
        boolean ready;

        @Actor
        public void actor1() {
            while (!ready) {
                ; // spin
            }
        }

        @Signal
        public void signal() {
            ready = true;
        }
    }

    /*
    使用volatile 保证了可见性
      ----------------------------------------------------------------------------------------------------------

        让该领域“不稳定”是实现进度保证的万无一失的方法。
        所有易失性写入最终都是可见的，因此循环最终会终止。

        事实上，这保证在所有平台上都会发生：

              RESULT  SAMPLES    FREQ       EXPECT  DESCRIPTION
               STALE        0    0.0%  Interesting  Test is stuck
          TERMINATED   17,882  100.0%   Acceptable  Gracefully finished
     */

    @JCStressTest(Mode.Termination)
    @Outcome(id = "TERMINATED", expect = ACCEPTABLE, desc = "Gracefully finished")
    @Outcome(id = "STALE", expect = ACCEPTABLE_INTERESTING, desc = "Test is stuck")
    @State
    public static class VolatileSpin {
        volatile boolean ready;

        @Actor
        public void actor1() {
            while (!ready) {
                ; // spin
            }
        }

        @Signal
        public void signal() {
            ready = true;
        }
    }

    /*

      ----------------------------------------------------------------------------------------------------------

        事实上，绝大多数硬件最终都使写入变得可见，所以我们最少的
        想要的是使访问对优化编译器不透明。幸运的是，这很容易做到
        VarHandles。{设置|获取}不透明。

        Indeed, this is guaranteed to happen on all platforms:

              RESULT  SAMPLES     FREQ      EXPECT  DESCRIPTION
               STALE        0    0.00%   Forbidden  Test is stuck
          TERMINATED   17,902  100.00%  Acceptable  Gracefully finished
     */

    @JCStressTest(Mode.Termination)
    @Outcome(id = "TERMINATED", expect = ACCEPTABLE, desc = "Gracefully finished")
    @Outcome(id = "STALE", expect = FORBIDDEN, desc = "Test is stuck")
    @State
    public static class OpaqueSpin {
        static final VarHandle VH;

        static {
            try {
                VH = MethodHandles.lookup().findVarHandle(OpaqueSpin.class, "ready", boolean.class);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        boolean ready;

        @Actor
        public void actor1() {
            while (!(boolean) VH.getOpaque(this)) ; // spin
        }

        @Signal
        public void signal() {
            VH.setOpaque(this, true);
        }
    }

    /*
      ----------------------------------------------------------------------------------------------------------

        为了完整起见，在显式同步下写入和读取变量还提供进度保证。
        在一个线程下释放锁的写入对于随后获取锁的线程可见。因此，循环最终终止。

        事实上，这保证在所有平台上都会发生：

              RESULT  SAMPLES     FREQ       EXPECT  DESCRIPTION
               STALE        0    0.00%  Interesting  Test is stuck
          TERMINATED   35,750  100.00%   Acceptable  Gracefully finished
     */

    @JCStressTest(Mode.Termination)
    @Outcome(id = "TERMINATED", expect = ACCEPTABLE, desc = "Gracefully finished")
    @Outcome(id = "STALE", expect = ACCEPTABLE_INTERESTING, desc = "Test is stuck")
    @State
    public static class SyncSpin {
        boolean ready;

        @Actor
        public void actor1() {
            while (true) { // spin
                synchronized (this) {
                    if (ready) {
                        break;
                    }
                }
            }
        }

        @Signal
        public void signal() {
            synchronized (this) {
                ready = true;
            }
        }
    }

}