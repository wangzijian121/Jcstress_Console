package cc.zjyun;

/**
 * Jcstress 并发测试入口类
 * <p>
 * -c<N>:测试的并发级别。该值可以大于可用的CPU数量。<br/>
 * -deoptRatio<N>:每第N次迭代（大致）取消优化。价值较大提高了测试性能，但降低了命中率不幸的编译。<br/>
 * -f[count]:应该fork每个测试N次。“0”以嵌入模式运行偶尔分叉。<br/>
 * -h:打印此帮助。<br/>
 * -iters<N>:每次测试的迭代次数。<br/>
 * -jvmArgs<字符串>:使用给定的JVM参数。这会禁用JVM标志自动检测，并且仅运行单JVM模式。要么是单个空格分隔的选项行，或多个选项被接受。此选项仅影响分叉运行。<br/>
 * -jvmArgsPrepend:将给定的JVM参数添加到自动检测的前面配置。单个空格分隔的选项行，或接受多个选项。仅此选项影响分叉运行。<br/>
 * -l[bool]:列出与请求的设置匹配的可用测试。<br/>
 * -m<mode>:预设测试模式：sanity, quick, default, tough, stress.<br/>
 * -maxStride<N>:最大内部步幅大小。较大的值会降低同步开销，但也降低了准确性。<br/>
 * -mf<MB>:每个测试的最大占用空间（以兆字节为单位）。这影响步幅大小：最大足迹永远不会超出，无论最小/最大步幅大小如何。<br/>
 * -minStride:最小内部步幅大小。较大的值会降低同步开销，但也降低了准确性。<br/>
 * -p<结果文件>:在结果文件上重新运行解析器。这不会运行任何测试。<br/>
 * -r< dir>:将报告放入的目标位置。<br/>
 * -sc<N>:系统中的CPU数量。设置该值会覆盖自动检测。<br/>
 * -t<regexp>:用于测试的正则表达式选择器。<br/>
 * -time<ms>:单次测试迭代所花费的时间。较大值提高测试可靠性，因为调度程序做得更好从长远来看。<br/>
 * -v:更加详细。<br/>
 * -yield[bool]:在繁忙循环中调用Thread.yield()。<br/>
 *
 * @author zijian Wang
 */
public class Main {
    public static void main(String[] args) throws Exception {

        /*
         * -c<N>:测试的并发级别。该值可以大于可用的CPU数量。
         * -deoptRatio<N>:每第N次迭代（大致）取消优化。价值较大提高了测试性能，但降低了命中率不幸的编译。
         * -f[count]:应该fork每个测试N次。“0”以嵌入模式运行偶尔分叉。
         * -h:打印此帮助。
         * -iters<N>:每次测试的迭代次数。
         * -jvmArgs<字符串>:使用给定的JVM参数。这会禁用JVM标志自动检测，并且仅运行单JVM模式。要么是单个空格分隔的选项行，或多个选项被接受。此选项仅影响分叉运行。<br/>
         * -jvmArgsPrepend:将给定的JVM参数添加到自动检测的前面配置。单个空格分隔的选项行，或接受多个选项。仅此选项影响分叉运行.
         * -l[bool]:列出与请求的设置匹配的可用测试。
         * -m<mode>:预设测试模式：sanity, quick, default, tough, stress.
         * -maxStride<N>:最大内部步幅大小。较大的值会降低同步开销，但也降低了准确性。
         * -mf<MB>:每个测试的最大占用空间（以兆字节为单位）。这影响步幅大小：最大足迹永远不会超出，无论最小/最大步幅大小如何。
         * -minStride:最小内部步幅大小。较大的值会降低同步开销，但也降低了准确性。
         * -p<结果文件>:在结果文件上重新运行解析器。这不会运行任何测试。
         * -r< dir>:将报告放入的目标位置。
         * -sc<N>:系统中的CPU数量。设置该值会覆盖自动检测。
         * -t<regexp>:用于测试的正则表达式选择器。
         * -time<ms>:单次测试迭代所花费的时间。较大值提高测试可靠性，因为调度程序做得更好从长远来看。
         * -v:更加详细。
         * -yield[bool]:在繁忙循环中调用Thread.yield()。
         */
        String testName = "DoubleCheckLockTest.DoubleCheckLockSingleton";

        if (("").equals(testName)) {
            System.out.println("不建议使用全局搜索类名！");
            System.exit(0);
        }
        String[] jcstressArgs = new String[]{
                "-t", testName,
                "-m", "default",
//                "-v",
                "-mf", "300",
//                "-r", "./results/bin"
        };
        org.openjdk.jcstress.Main.main(jcstressArgs);
    }
}
