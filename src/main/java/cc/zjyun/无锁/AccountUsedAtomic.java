package cc.zjyun.无锁;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

/**
 * 使用原子类的的无锁并发
 *
 * @author zijian Wang
 */
public class AccountUsedAtomic implements Account {

    private static AtomicInteger atomicInteger = new AtomicInteger(10000);

    /**
     * 获取余额
     *
     * @return
     */
    @Override
    public int getBalance() {
        return 0;
    }

    /**
     * 取款
     *
     * @param amount 取款金额
     */
    @Override
    public void withdrawals(int amount) {
        //CAS A ->B
        while (true) {
            int snapShot = atomicInteger.get();
            if (atomicInteger.compareAndSet(snapShot, amount - 10)) {
                break;
            }
        }
    }

    @JCStressTest
    @Outcome(id = "0", expect = ACCEPTABLE_INTERESTING, desc = "余额为0,正确！")
    @Outcome(expect = FORBIDDEN, desc = "其他的值。")
    @State
    public static class Test {

        private int i;

        public int testFunc() {
            Account account = new AccountUsedAtomic();
            IntStream.range(0, 1000).forEach(x -> {
                Thread thread = new Thread(() -> {
                    account.withdrawals(10);
                }, "线程" + x);
                thread.start();
            });
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return account.getBalance();
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
