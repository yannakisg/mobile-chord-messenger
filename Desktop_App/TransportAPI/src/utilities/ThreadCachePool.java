package utilities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ThreadCachePool {
    private static final ExecutorService servicePool = Executors.newCachedThreadPool();
    
    public static void execute(Runnable runnable) {
        servicePool.execute(runnable);
    }
}
