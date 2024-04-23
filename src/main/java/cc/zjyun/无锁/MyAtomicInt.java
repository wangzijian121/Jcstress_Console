package cc.zjyun.无锁;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.stream.IntStream;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

/**
 * 使用UNSAFE实现自己的原子类-并发测试
 */
public class MyAtomicInt {
    private volatile int value;
    static final long offset;
    private static Unsafe unsafe;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            offset = unsafe.objectFieldOffset(MyAtomicInt.class.getDeclaredField("value"));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public MyAtomicInt(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }

    private boolean add(int addValue) {
        int prev;
        do {
            prev = value;
        } while (!unsafe.compareAndSwapInt(this, offset, prev, prev + addValue));
        return true;
    }

    @JCStressTest
    @Outcome(id = "1000", expect = ACCEPTABLE_INTERESTING, desc = "增加1000次！")
    @Outcome(expect = FORBIDDEN, desc = "其他的值。")
    @State
    public static class Test {

        private int i;

        public int testFunc() {
            MyAtomicInt myAtomicInt = new MyAtomicInt(0);
            IntStream.range(0, 1000).forEach(x -> {
                Thread thread = new Thread(() -> {
                    myAtomicInt.add(1);
                }, "线程" + x);
                thread.start();
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return myAtomicInt.get();
        }

        //time时间内运行n次
        @Actor
        public int actor1() {
            return testFunc();
        }

        @Arbiter
        public void result(I_Result iResult) {
            iResult.r1 = actor1();
        }
    }
}
