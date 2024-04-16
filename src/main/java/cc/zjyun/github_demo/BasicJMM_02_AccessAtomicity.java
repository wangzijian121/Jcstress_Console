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
import org.openjdk.jcstress.infra.results.I_Result;
import org.openjdk.jcstress.infra.results.J_Result;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

import static org.openjdk.jcstress.annotations.Expect.*;
import static org.openjdk.jcstress.util.UnsafeHolder.U;

public class BasicJMM_02_AccessAtomicity {

    /*
        How to run this test:
            $ java -jar jcstress-samples/target/jcstress.jar -t BasicJMM_02_AccessAtomicity[.SubTestName]
     */

    /*
      ----------------------------------------------------------------------------------------------------------

        这是我们的第二种情况：访问原子性。大多数基本类型都带有
        直观属性：这些基本类型的读取和写入发生
        完全，即使在比赛下:

          RESULT         SAMPLES     FREQ      EXPECT  DESCRIPTION
              -1  11,916,557,823   81.45%  Acceptable  Seeing the full value.
               0   2,714,388,481   18.55%  Acceptable  Seeing the default value: writer had not acted yet.
     */

    @JCStressTest
    @Outcome(id = "0", expect = ACCEPTABLE, desc = "看到默认值：writer 尚未执行操作。")
    @Outcome(id = "-1", expect = ACCEPTABLE, desc = "看到全部价值。")
    @Outcome(expect = FORBIDDEN, desc = "其他情况是禁止的。")
    @State
    @Description("原子性测试")
    public static class Integers {
        int v;

        @Actor
        public void writer() {
            v = 0xFFFFFFFF;
        }

        @Actor
        public void reader(I_Result r) {
            r.r1 = v;
        }
    }

    /*
      ----------------------------------------------------------------------------------------------------------

        Java 语言规范中有一些有趣的例外，
        根据 17.7 “双倍和长期的非原子处理”。它说长和双打可以用非原子方式处理。

        此测试将在某些 32 位 VM 上产生有趣的结果，例如x86_32：

               RESULT        SAMPLES     FREQ       EXPECT  DESCRIPTION
                   -1  8,818,463,884   70.12%   Acceptable  Seeing the full value.
          -4294967296      9,586,556    0.08%  Interesting  Other cases are violating access atomicity, but allowed u...
                    0  3,747,652,022   29.80%   Acceptable  Seeing the default value: writer had not acted yet.
           4294967295         86,082   <0.01%  Interesting  Other cases are violating access atomicity, but allowed u...

        其他 32 位 VM 可能仍会选择使用高级指令来恢复原子性，
        例如，在 ARMv7（32 位）上:

              RESULT     SAMPLES     FREQ       EXPECT  DESCRIPTION
                  -1  96,332,256   79.50%   Acceptable  Seeing the full value.
                   0  24,839,456   20.50%   Acceptable  Seeing the default value: writer had not acted yet.

     */

    @JCStressTest
    @Outcome(id = "0", expect = ACCEPTABLE, desc = "Seeing the default value: writer had not acted yet.")
    @Outcome(id = "-1", expect = ACCEPTABLE, desc = "Seeing the full value.")
    @Outcome(expect = ACCEPTABLE_INTERESTING, desc = "Other cases are violating access atomicity, but allowed under JLS.")
    @Ref("https://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.7")
    @State
    public static class Longs {
        long v;

        @Actor
        public void writer() {
            v = 0xFFFFFFFF_FFFFFFFFL;
        }

        @Actor
        public void reader(J_Result r) {
            r.r1 = v;
        }
    }

    /*
      ----------------------------------------------------------------------------------------------------------

        通过使字段“易失性”来恢复访问原子性是可能的：

        x86_32:
          RESULT        SAMPLES     FREQ      EXPECT  DESCRIPTION
              -1  1,306,213,861   17.27%  Acceptable  Seeing the full value.
               0  6,257,145,883   82.73%  Acceptable  Seeing the default value: writer had not acted yet.
     */

    @JCStressTest
    @Outcome(id = "0", expect = ACCEPTABLE, desc = "看到默认值：writer 尚未执行操作。")
    @Outcome(id = "-1", expect = ACCEPTABLE, desc = "看到全部价值。")
    @Outcome(expect = FORBIDDEN, desc = "其他情况是禁止的。")
    @State
    public static class VolatileLongs {
        volatile long v;

        @Actor
        public void writer() {
            v = 0xFFFFFFFF_FFFFFFFFL;
        }

        @Actor
        public void reader(J_Result r) {
            r.r1 = v;
        }
    }


    /*
      ----------------------------------------------------------------------------------------------------------

    虽然字段和数组元素访问的规范要求很严格，但具体类可能具有宽松的语义。以 ByteBuffer 为例，我们可以读取 4 字节整数从任意偏移量。

    较旧的 ByteBuffer 实现一次访问一个字节，这需要合并/拆分
            任何大于字节的内容都包含在单个操作中。当然，没有访问原子性
            那里通过建设。在较新的 ByteBuffer 实现中，_aligned_ 访问是使用
            更大的指令，回馈原子性。未对齐的访问仍必须执行多项操作
            在不支持错位的机器上更窄的访问。

        x86_64:
             RESULT      SAMPLES     FREQ       EXPECT  DESCRIPTION
                 -1  142,718,041   61.57%   Acceptable  看到全部价值.
          -16711936            4   <0.01%  Interesting  其他情况是允许的，因为读取/写入不是...
          -16777216      111,579    0.05%  Interesting  其他情况是允许的，因为读取/写入不是...
               -256      110,267    0.05%  Interesting  其他情况是允许的，因为读取/写入不是...
             -65281            3   <0.01%  Interesting  其他情况是允许的，因为读取/写入不是...
             -65536      111,618    0.05%  Interesting  其他情况是允许的，因为读取/写入不是...
                  0   88,765,143   38.29%   Acceptable  看到默认值：writer 尚未执行操作。
           16711680           36   <0.01%  Interesting  其他情况是允许的，因为读取/写入不是...
           16777215            5   <0.01%  Interesting  其他情况是允许的，因为读取/写入不是...
                255            1   <0.01%  Interesting  其他情况是允许的，因为读取/写入不是...
              65535            7   <0.01%  Interesting  其他情况是允许的，因为读取/写入不是...
    */

    @JCStressTest
    @Outcome(id = "0", expect = ACCEPTABLE, desc = "Seeing the default value: writer had not acted yet.")
    @Outcome(id = "-1", expect = ACCEPTABLE, desc = "Seeing the full value.")
    @Outcome(expect = ACCEPTABLE_INTERESTING, desc = "Other cases are allowed, because reads/writes are not atomic.")
    @State
    public static class ByteBuffers {
        public static final int SIZE = 256;

        ByteBuffer bb = ByteBuffer.allocate(SIZE);
        int idx = ThreadLocalRandom.current().nextInt(SIZE - 4);

        @Actor
        public void writer() {
            bb.putInt(idx, 0xFFFFFFFF);
        }

        @Actor
        public void reader(I_Result r) {
            r.r1 = bb.getInt(idx);
        }
    }

    /*
        不安全的跨缓存行
      ----------------------------------------------------------------------------------------------------------

        但是，即使硬件支持未对齐的访问，也永远无法保证它是原子的。例如，读取跨越两个缓存行的值不会是原子的，即使我们设法发出
        访问的单一指令。

        x86_64:
             RESULT      SAMPLES     FREQ       EXPECT  DESCRIPTION
                 -1  127,819,822   48.55%   Acceptable  Seeing the full value.
          -16777216           17   <0.01%  Interesting  Other cases are allowed, because reads/writes are not ato...
               -256           17   <0.01%  Interesting  Other cases are allowed, because reads/writes are not ato...
             -65536           11   <0.01%  Interesting  Other cases are allowed, because reads/writes are not ato...
                  0  134,990,763   51.27%   Acceptable  Seeing the default value: writer had not acted yet.
           16777215      154,265    0.06%  Interesting  Other cases are allowed, because reads/writes are not ato...
                255      154,643    0.06%  Interesting  Other cases are allowed, because reads/writes are not ato...
              65535      154,446    0.06%  Interesting  Other cases are allowed, because reads/writes are not ato...
     */

    @JCStressTest
    @Outcome(id = "0", expect = ACCEPTABLE, desc = "看到默认值：writer 尚未执行操作。")
    @Outcome(id = "-1", expect = ACCEPTABLE, desc = "看到全部价值。")
    @Outcome(expect = ACCEPTABLE_INTERESTING, desc = "其他情况是允许的，因为读取/写入不是原子的。")
    @State
    public static class UnsafeCrossCacheLine {

        public static final int SIZE = 256;
        public static final long ARRAY_BASE_OFFSET = U.arrayBaseOffset(byte[].class);
        public static final long ARRAY_BASE_SCALE = U.arrayIndexScale(byte[].class);

        byte[] ss = new byte[SIZE];
        long off = ARRAY_BASE_OFFSET + ARRAY_BASE_SCALE * ThreadLocalRandom.current().nextInt(SIZE - 4);

        @Actor
        public void writer() {
            U.putInt(ss, off, 0xFFFFFFFF);
        }

        @Actor
        public void reader(I_Result r) {
            r.r1 = U.getInt(ss, off);
        }
    }

    public static void main(String[] args) {
        System.out.println(0xFFFFFFFF);
    }

    // ======================================= EARLY VALHALLA EXAMPLES BELOW =======================================
    // These require Valhalla JDK builds.

    /*
      ----------------------------------------------------------------------------------------------------------

        While most modern hardware implementation provide access atomicity for all Java primitive types,
        the issue with access atomicity raises it ugly head again with Project Valhalla, which strives
        to introduce multi-field classes that behave like primitives. There, reading the entirety of
        the "inlined" ("flattened") primitive type is sometimes not possible, because the effective
        data type width is too large. Therefore, we would normally see access atomicity violations.

        Indeed, on x86_64 this happens:
          RESULT        SAMPLES     FREQ       EXPECT  DESCRIPTION
            0, 0    790,816,955   22.90%   Acceptable  Seeing the default value: writer had not acted yet.
            0, 1      2,154,875    0.06%  Interesting  Other cases are allowed, because reads/writes are not ato...
            1, 0      2,385,714    0.07%  Interesting  Other cases are allowed, because reads/writes are not ato...
            1, 1  2,658,516,120   76.97%   Acceptable  Seeing the full value.
     */

    /*
    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Seeing the default value: writer had not acted yet.")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Seeing the full value.")
    @Outcome(expect = ACCEPTABLE_INTERESTING, desc = "Other cases are allowed, because reads/writes are not atomic.")
    @State
    public static class Values {
        static primitive class Value {
            long x;
            long y;
            public Value(long x, long y) {
                this.x = x;
                this.y = y;
            }
        }

        Value v = Value.default;

        @Actor
        public void writer() {
            v = new Value(1, 1);
        }

        @Actor
        public void reader(JJ_Result r) {
            Value tv = v;
            r.r1 = tv.x;
            r.r2 = tv.y;
        }
    }
     */

    /*
      ----------------------------------------------------------------------------------------------------------

        As usual, marking the primitive field "volatile" regains the access atomicity. In current implementations,
        this happens by forbidding the "flattening" of the inline type.

        x86_64:
          RESULT        SAMPLES     FREQ      EXPECT  DESCRIPTION
            0, 0  2,780,487,683   84.19%  Acceptable  Seeing the default value: writer had not acted yet.
            1, 1    522,202,621   15.81%  Acceptable  Seeing the full value.
     */

    /*

    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Seeing the default value: writer had not acted yet.")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Seeing the full value.")
    @Outcome(expect = FORBIDDEN, desc = "Other cases are forbidden.")
    @State
    public static class VolatileValues {
        static primitive class Value {
            long x;
            long y;
            public Value(long x, long y) {
                this.x = x;
                this.y = y;
            }
        }

        volatile Value v = Value.default;

        @Actor
        public void writer() {
            v = new Value(1, 1);
        }

        @Actor
        public void reader(JJ_Result r) {
            Value tv = v;
            r.r1 = tv.x;
            r.r2 = tv.y;
        }
    }
     */

    /*
      ----------------------------------------------------------------------------------------------------------

        The awkward case is when the primitive field is not marked specifically, so field layouter flattens
        the type, but then the the primitive field is used as "opaque". In this case, the implementation
        has to enforce atomicity by e.g. locking.

        x86_64:
            RESULT        SAMPLES     FREQ      EXPECT  DESCRIPTION
              0, 0    542,416,624   25.36%  Acceptable  Seeing the default value: writer had not acted yet.
              1, 1  1,596,364,560   74.64%  Acceptable  Seeing the full value.
     */

    /*
    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Seeing the default value: writer had not acted yet.")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Seeing the full value.")
    @Outcome(expect = FORBIDDEN, desc = "Other cases are forbidden.")
    @State
    public static class OpaqueValues {
        static primitive class Value {
            long x;
            long y;
            public Value(long x, long y) {
                this.x = x;
                this.y = y;
            }
        }

        Value v = Value.default;

        static final VarHandle VH;

        static {
            try {
                VH = MethodHandles.lookup().findVarHandle(OpaqueValues.class, "v", Value.class);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        @Actor
        public void writer() {
            VH.setOpaque(this, new Value(1, 1));
        }

        @Actor
        public void reader(JJ_Result r) {
            Value tv = (Value) VH.getOpaque(this);
            r.r1 = tv.x;
            r.r2 = tv.y;
        }
    }
    */

}