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
package cc.zjyun.samples.jmm.advanced;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

import static org.openjdk.jcstress.annotations.Expect.*;

@JCStressTest
@State
@Outcome(id = {"0, 0", "1, 1"}, expect = Expect.ACCEPTABLE, desc = "Boring")
@Outcome(id = {"0, 1", "1, 0"}, expect = Expect.ACCEPTABLE, desc = "Plausible")
public class AdvancedJMM_11_WrongAcquireOrder {

    /*
        How to run this test:
            $ java -jar jcstress-samples/target/jcstress.jar -t AdvancedJMM_11_WrongAcquireOrder
     */

    /*
      ----------------------------------------------------------------------------------------------------------

        For completeness, the example that has a wrong acquire order. All these results can be explained by
        sequential execution of the code.

        x86_64:
          RESULT        SAMPLES     FREQ       EXPECT  DESCRIPTION
            0, 0  2,560,656,086   55.33%   Acceptable  Boring
            0, 1      2,961,349    0.06%   Acceptable  Plausible
            1, 0      7,885,064    0.17%   Acceptable  Plausible
            1, 1  2,056,684,125   44.44%   Acceptable  Boring
     */

    int x;
    volatile int g;

    @Actor
    public void actor1() {
        x = 1;
        g = 1;
    }

    @Actor
    public void actor2(II_Result r) {
        r.r1 = x;
        r.r2 = g; // acquiring too late
    }
}