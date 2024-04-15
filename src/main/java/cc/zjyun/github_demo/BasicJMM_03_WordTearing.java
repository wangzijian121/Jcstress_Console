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
import org.openjdk.jcstress.infra.results.ZZ_Result;

import java.util.BitSet;

import static org.openjdk.jcstress.annotations.Expect.*;

/**
 * 词语撕裂
 * @author zijian Wang
 */
public class BasicJMM_03_WordTearing {


    /*为数组分配元素
      ----------------------------------------------------------------------------------------------------------

        Java 内存模型禁止拆分单词。也就是说，它要求每个字段和数组元素作为不同的，并且一个元素的操作不应干扰其他元素。

        请注意，这与访问原子性略有不同。访问原子性表示，对_wide_ 逻辑字段应该是原子的，即使它需要多个 _narrower_ 物理访问。
        禁止拆字意味着对 _narrow_ 逻辑字段的访问不应干扰相邻的逻辑字段字段，即使使用 _wider_ 物理访问完成。
事实上，对纯布尔数组的测试表明，这条规则成立：
              RESULT      SAMPLES     FREQ      EXPECT  DESCRIPTION
          true, true  489,444,864  100.00%  Acceptable  看到两个更新都完好无损。
      */

    @JCStressTest
    @Outcome(id = "true, true", expect = ACCEPTABLE, desc = "看到两个更新都完好无损")
    @Outcome(expect = FORBIDDEN, desc = "Other cases are forbidden.")
    @State
    public static class JavaArrays {
        boolean[] bs = new boolean[2];

        @Actor
        public void writer1() {
            bs[0] = true;
        }

        @Actor
        public void writer2() {
            bs[1] = true;
        }

        @Arbiter
        public void arbiter(ZZ_Result r) {
            r.r1 = bs[0];
            r.r2 = bs[1];
        }
    }

    /*
      ----------------------------------------------------------------------------------------------------------

        但是，虽然该要求对字段和数组元素强制执行，但 Java 类
        实现可能仍然违反此要求，例如，如果它们密集地打包元素，并且
        定期读取/写入相邻元素。通常的例子是 java.util.BitSet。

        事实上，这很容易重现:

               RESULT      SAMPLES     FREQ       EXPECT  DESCRIPTION
          false, true   75,809,316   16.27%  Interesting  销毁了一个更新.
          true, false   84,291,298   18.09%  Interesting  销毁了一个更新.
           true, true  305,945,850   65.65%   Acceptable  看到两个更新都完好无损.
     */

    @JCStressTest
    @Outcome(id = "true, true", expect = ACCEPTABLE, desc = "看到两个更新都完好无损.")
    @Outcome(id = "false, true", expect = ACCEPTABLE_INTERESTING, desc = "Destroyed one update.")
    @Outcome(id = "true, false", expect = ACCEPTABLE_INTERESTING, desc = "Destroyed one update.")
    @State
    public static class BitSets {

        BitSet bs = new BitSet();

        @Actor
        public void writer1() {
            bs.set(0);
        }

        @Actor
        public void writer2() {
            bs.set(1);
        }

        @Arbiter
        public void arbiter(ZZ_Result r) {
            r.r1 = bs.get(0);
            r.r2 = bs.get(1);
        }
    }


}