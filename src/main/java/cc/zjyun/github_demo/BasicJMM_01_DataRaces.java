package cc.zjyun.github_demo;
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

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;


/**
 * 比较对两个对象的争用状态
 *
 * @author zijian Wang
 */
@JCStressTest
@Outcome(id = "null", expect = Expect.ACCEPTABLE, desc = "尚未看到对象")
@Outcome(id = "class java.lang.Object", expect = Expect.ACCEPTABLE, desc = "看到对象，有效的类")
//@Outcome(expect = Expect.FORBIDDEN, desc = "其他情况是非法的")
@State
@Description("两个对象的争用状态")
public class BasicJMM_01_DataRaces {

/*
    这是我们的第一个案例：数据争用。存在冲突：作者和读者访问
            同一位置，没有任何同步。顾名思义，这是一个争用

    这个测试可能看起来微不足道，但它实际上突出了一个相当强大的 Java 属性：
            即使在存在数据争用的情况下，这种行为也是合理的。值得注意的是，数据竞赛
            不要破坏 JVM。在这里，我们通过比赛发布对象，但即使是
            然后对象设置了所有元数据集，因此我们可以询问类，调用方法，
            访问字段。

        在所有平台上，此测试产生：

                      结果            样本           FREQ EXPECT          描述
      class java.lang.Object  3,619,439,149   51.74%  Acceptable  看到对象，有效的类
                        null  3,376,358,355   48.26%  Acceptable  尚未看到对象
    */

    private Object o;

    @Actor
    public synchronized void writer() {
        o = new Object();
    }

    @Actor
    public synchronized void reader(L_Result r) {
        Object lo = o;
        if (lo != null) {
            try {
                r.r1 = lo.getClass();
            } catch (NullPointerException npe) {
                r.r1 = npe;
            }
        } else {
            r.r1 = null;
        }
    }
}