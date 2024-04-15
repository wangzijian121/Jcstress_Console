package cc.zjyun;

/**
 * Jcstress 并发测试入口类
 * ⚠ 指定的类内的变量必须是私有的。
 *
 * @author zijian Wang
 */
public class Main {
    public static void main(String[] args) throws Exception {
        /**
         * 不指定testName 时会全局搜索 JCStressTest 类，不建议置为空
         */
        String param = "-t";
        String testName = "TestInstructionReorder";

        if (("").equals(testName) || !("-t").equals(param)) {
            System.out.println("不建议使用全局搜索类名！");
            System.exit(0);
        }
        String[] jcstressArgs = new String[]{param, testName};
        org.openjdk.jcstress.Main.main(jcstressArgs);
    }
}
