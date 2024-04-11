package cc.zjyun.github_demo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.openjdk.jcstress.infra.runners.TestConfig;
import org.openjdk.jcstress.infra.collectors.TestResultCollector;
import org.openjdk.jcstress.infra.runners.Runner;
import org.openjdk.jcstress.infra.runners.StateHolder;
import org.openjdk.jcstress.util.Counter;
import org.openjdk.jcstress.vm.WhiteBoxSupport;
import java.util.concurrent.ExecutionException;
import cc.zjyun.github_demo.BasicJMM_01_DataRaces;
import org.openjdk.jcstress.infra.results.L_Result_jcstress;

public class BasicJMM_01_DataRaces_jcstress extends Runner<L_Result_jcstress> {

    volatile StateHolder<BasicJMM_01_DataRaces, L_Result_jcstress> version;

    public BasicJMM_01_DataRaces_jcstress(TestConfig config, TestResultCollector collector, ExecutorService pool) {
        super(config, collector, pool, "cc.zjyun.github_demo.BasicJMM_01_DataRaces");
    }

    @Override
    public void sanityCheck() throws Throwable {
        sanityCheck_API();
        sanityCheck_Footprints();
    }

    private void sanityCheck_API() throws Throwable {
        final BasicJMM_01_DataRaces t = new BasicJMM_01_DataRaces();
        final BasicJMM_01_DataRaces s = new BasicJMM_01_DataRaces();
        final L_Result_jcstress r = new L_Result_jcstress();
        Collection<Future<?>> res = new ArrayList<>();
        res.add(pool.submit(() -> t.writer()));
        res.add(pool.submit(() -> t.reader(r)));
        for (Future<?> f : res) {
            try {
                f.get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        }
    }

    private void sanityCheck_Footprints() throws Throwable {
        config.adjustStrides(size -> {
            version = new StateHolder<>(new BasicJMM_01_DataRaces[size], new L_Result_jcstress[size], 2, config.spinLoopStyle);
            final BasicJMM_01_DataRaces t = new BasicJMM_01_DataRaces();
            for (int c = 0; c < size; c++) {
                L_Result_jcstress r = new L_Result_jcstress();
                BasicJMM_01_DataRaces s = new BasicJMM_01_DataRaces();
                version.rs[c] = r;
                version.ss[c] = s;
                s.writer();
                s.reader(r);
            }
        });
    }

    @Override
    public Counter<L_Result_jcstress> internalRun() {
        version = new StateHolder<>(new BasicJMM_01_DataRaces[0], new L_Result_jcstress[0], 2, config.spinLoopStyle);

        control.isStopped = false;
        Collection<Future<Counter<L_Result_jcstress>>> tasks = new ArrayList<>();
        tasks.add(pool.submit(this::writer));
        tasks.add(pool.submit(this::reader));

        try {
            TimeUnit.MILLISECONDS.sleep(config.time);
        } catch (InterruptedException e) {
        }

        control.isStopped = true;

        waitFor(tasks);

        Counter<L_Result_jcstress> counter = new Counter<>();
        for (Future<Counter<L_Result_jcstress>> f : tasks) {
            try {
                counter.merge(f.get());
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
        return counter;
    }

    public final void jcstress_consume(StateHolder<BasicJMM_01_DataRaces, L_Result_jcstress> holder, Counter<L_Result_jcstress> cnt, int a, int actors) {
        BasicJMM_01_DataRaces[] ss = holder.ss;
        L_Result_jcstress[] rs = holder.rs;
        int len = ss.length;
        int left = a * len / actors;
        int right = (a + 1) * len / actors;
        for (int c = left; c < right; c++) {
            L_Result_jcstress r = rs[c];
            BasicJMM_01_DataRaces s = ss[c];
            ss[c] = new BasicJMM_01_DataRaces();
            cnt.record(r);
            r.r1 = null;
        }
    }

    public final void jcstress_updateHolder(StateHolder<BasicJMM_01_DataRaces, L_Result_jcstress> holder) {
        if (!holder.tryStartUpdate()) return;
        BasicJMM_01_DataRaces[] ss = holder.ss;
        L_Result_jcstress[] rs = holder.rs;
        int len = ss.length;

        int newLen = holder.updateStride ? Math.max(config.minStride, Math.min(len * 2, config.maxStride)) : len;

        BasicJMM_01_DataRaces[] newS = ss;
        L_Result_jcstress[] newR = rs;
        if (newLen > len) {
            newS = Arrays.copyOf(ss, newLen);
            newR = Arrays.copyOf(rs, newLen);
            for (int c = len; c < newLen; c++) {
                newR[c] = new L_Result_jcstress();
                newS[c] = new BasicJMM_01_DataRaces();
            }
         }

        version = new StateHolder<>(control.isStopped, newS, newR, 2, config.spinLoopStyle);
        holder.finishUpdate();
   }

    public final Counter<L_Result_jcstress> writer() {

        Counter<L_Result_jcstress> counter = new Counter<>();
        while (true) {
            StateHolder<BasicJMM_01_DataRaces,L_Result_jcstress> holder = version;
            if (holder.stopped) {
                return counter;
            }

            BasicJMM_01_DataRaces[] ss = holder.ss;
            L_Result_jcstress[] rs = holder.rs;
            int size = ss.length;

            holder.preRun();

            for (int c = 0; c < size; c++) {
                BasicJMM_01_DataRaces s = ss[c];
                s.writer();
            }

            holder.postRun();

            jcstress_consume(holder, counter, 0, 2);
            jcstress_updateHolder(holder);

            holder.postUpdate();
        }
    }

    public final Counter<L_Result_jcstress> reader() {

        Counter<L_Result_jcstress> counter = new Counter<>();
        while (true) {
            StateHolder<BasicJMM_01_DataRaces,L_Result_jcstress> holder = version;
            if (holder.stopped) {
                return counter;
            }

            BasicJMM_01_DataRaces[] ss = holder.ss;
            L_Result_jcstress[] rs = holder.rs;
            int size = ss.length;

            holder.preRun();

            for (int c = 0; c < size; c++) {
                BasicJMM_01_DataRaces s = ss[c];
                L_Result_jcstress r = rs[c];
                r.trap = 0;
                s.reader(r);
            }

            holder.postRun();

            jcstress_consume(holder, counter, 1, 2);
            jcstress_updateHolder(holder);

            holder.postUpdate();
        }
    }

}
