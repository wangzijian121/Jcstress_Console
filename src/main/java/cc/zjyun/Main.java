package cc.zjyun;

/**
 * Jcstress 并发测试入口类
 *
 * @author zijian Wang
 */
public class Main {
    public static void main(String[] args) throws Exception {
        /**
         * 不指定testName 时会全局搜索 JCStressTest 类，不建议置为空
         */
        String param = "-t";
//        String testName = "BasicJMM_02_AccessAtomicity[.Integers]";
        String testName = "TestInstructionReorder";

        if (("").equals(testName) || !("-t").equals(param)) {
            System.out.println("不建议使用全局搜索类名！");
            System.exit(0);
        }


//        String[] jcstressArgs = new String[]{param, testName,"-m","tough"};
        String[] jcstressArgs = new String[]{param, testName,"-m","default"};
        org.openjdk.jcstress.Main.main(jcstressArgs);
    }
}
