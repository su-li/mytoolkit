import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 线程池工具类
 *
 * @author hd
 * @Date 2017/11/15
 */
public class ThreadPoolUtil {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolUtil.class);

    private static ThreadPoolUtil threadPool;
    private ThreadPoolExecutor executor = null;
    //线程池的基础参数 实际使用可写入到配置文件中

    // 核心池的大小 运行线程的最大值 当线程池中的线程数目达到corePoolSize后，就会把多余的任务放到缓存队列当中；
    private int corePoolSize = 10;

    //当使用LinkedBlockingQueue 无限队列时 此参数失效 so 可以把maximumPoolSize,corePoolSize写成一样便于测试
    private int maximumPoolSize = Integer.MAX_VALUE;  // 创建线程最大值

    // 线程没有执行任务时 被保留的最长时间 超过这个时间就会被销毁 直到线程数等于 corePoolSize
    private long keepAliveTime = 1;

    // 等待线程池任务执行结束的超时时间
    private long timeout = 10;

    /**
     * 时间单位，有7种取值，在TimeUnit类中有7种静态属性：
     * TimeUnit.DAYS;               天
     * TimeUnit.HOURS;             小时
     * TimeUnit.MINUTES;           分钟
     * TimeUnit.SECONDS;           秒
     * TimeUnit.MILLISECONDS;      毫秒
     * TimeUnit.MICROSECONDS;      微妙
     * TimeUnit.NANOSECONDS;       纳秒
     ***/
    private TimeUnit keepAliveTime_unit = TimeUnit.SECONDS;
    private TimeUnit timeout_unit = TimeUnit.DAYS;

    /**
     * 用来储存等待中的任务的容器
     * <p>
     * 几种选择：
     * ArrayBlockingQueue;
     * LinkedBlockingQueue;
     * SynchronousQueue;
     * 区别太罗嗦请百度  http://blog.csdn.net/mn11201117/article/details/8671497
     */
    private LinkedBlockingQueue workQueue = new LinkedBlockingQueue<Runnable>();

    /**
     * 单例
     *
     * @param corePoolSize 核心线程数
     * @return
     */
    public static ThreadPoolUtil init(int corePoolSize) {

        if (threadPool == null)
            threadPool = new ThreadPoolUtil(corePoolSize);
        return threadPool;
    }

    /**
     * 私有构造方法
     */
    private ThreadPoolUtil(int corePoolSize) {
        //实现线程池
        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, keepAliveTime_unit,
                workQueue);
        System.out.println("线程池初始化成功");
    }

    /**
     * 线程池获取方法
     *
     * @return
     */
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    /**
     * 准备执行 抛入线程池
     *
     * @param t
     */
    public void execute(Thread t) {
        executor.execute(t);
    }

    public void execute(Runnable t) {
        executor.execute(t);
    }

    public int getQueueSize() {
        return executor.getQueue().size();
    }

    /**
     * 异步提交返回 Future
     * Future.get()可获得返回结果
     *
     * @return
     */
    public Future<?> submit(Runnable t) {
        return executor.submit(t);
    }

    /**
     * 异步提交返回 Future
     * Future.get()可获得返回结果
     *
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Future<?> submit(Callable t) {
        return getExecutor().submit(t);
    }

    /**
     * 销毁线程池
     */
    public void shutdown() {
        getExecutor().shutdown();
    }

    /**
     * 阻塞，直到线程池里所有任务结束
     *
     * @return true 规定时间内,线程池终止了不关心是否正常终止;false 超时了,仅此而已,不关心线程池终止与否
     */
    public boolean awaitTermination() throws InterruptedException {
        logger.info("Thread pool ,awaitTermination started, please wait till all the jobs complete.");
        /**
         * 参数：
         timeout：超时时间
         timeout_unit：     timeout超时时间的单位
         返回：
         true：线程池正常终止或被打断
         false：超过timeout指定时间
         在发出一个shutdown请求后，在以下3种情况发生之前，awaitTermination()都会被阻塞
         1、所有任务完成执行
         2、到达超时时间
         3、当前线程被中断
         */
        return executor.awaitTermination(timeout, timeout_unit);
    }

    /**
     * 统计任务的总耗时
     *
     * @param startTime 开始时间
     * @return
     */
    public long elapsedTime(long startTime) throws Exception {
        threadPool.shutdown();
        if (threadPool.awaitTermination()) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }
}
