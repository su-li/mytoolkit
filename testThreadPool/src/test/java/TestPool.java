/**
 * @author HD
 * @date 2017/11/15
 */

public class TestPool {

    public static void main(String[] args) {
        ThreadPoolUtil pool = ThreadPoolUtil.init();
        for (int i = 0; i <=1000000; i++) {

            int finalI = i;
            pool.execute(new Thread() {
                @Override
                public void run() {
                    System.out.println(finalI);
                }
            });

        }

        pool.shutdown();
        try {
            //
            pool.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
