package cc.zjyun.无锁;


import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.util.stream.IntStream;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

/**
 * 使用锁实现
 *
 * @author zijian Wang
 */
public class AccountNothing implements Account {
    private int balance = 100;

    /**
     * 获取余额
     *
     * @return
     */
    @Override
    public int getBalance() {
        return this.balance;
    }

    /**
     * 取款
     *
     * @param amount 取款金额
     */
    @Override
    public void withdrawals(int amount) {
        balance -= amount;
    }

    @JCStressTest
    @Outcome(id = "0", expect = ACCEPTABLE, desc = "余额为0.")
    @Outcome(expect = FORBIDDEN, desc = "其他的值。")
    @State
    public static class AccountNothingTest {
        private int i;
        private Account account = new AccountNothing();

        @Actor
        public void actor1() {
            IntStream.range(0, 50).forEach(x -> {
                account.withdrawals(1);
            });
        }

        @Actor
        public void actor2() {
            IntStream.range(0, 50).forEach(x -> {
                account.withdrawals(1);
            });
        }


        @Arbiter
        public void result(I_Result iResult) {
            iResult.r1 = account.getBalance();
            account = new AccountNothing();
        }
    }
}
