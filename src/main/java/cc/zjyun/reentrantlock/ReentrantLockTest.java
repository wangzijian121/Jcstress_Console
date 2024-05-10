package cc.zjyun.reentrantlock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class ReentrantLockTest {

    private ReentrantLock reentrantLock = new ReentrantLock();
    private CountDownLatch countDownLatch = new CountDownLatch(10);
    private int sum = 0;


    public  int add() throws InterruptedException {
        IntStream.range(0, 10).forEach(x -> new Thread(() -> {
            reentrantLock.lock();
            try {
                for (int i = 0; i < 1000L; i++) {
                    sum++;
                }
                countDownLatch.countDown();
                System.out.println("执行....");
            } finally {
                reentrantLock.unlock();
            }
        }, "线程" + x).start());

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        System.out.println(sum);
        return  sum;
    }
}

