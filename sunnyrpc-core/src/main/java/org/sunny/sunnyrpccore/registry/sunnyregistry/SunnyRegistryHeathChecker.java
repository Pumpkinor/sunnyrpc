package org.sunny.sunnyrpccore.registry.sunnyregistry;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SunnyRegistryHeathChecker {
    ScheduledExecutorService consumerExecutor = null;
    ScheduledExecutorService providerExecutor = null;
    final int interval = 5_000;

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
    public void start() {
        log.info(" ====>>>> [SunnyRegistry] : start with health checker.");
        consumerExecutor = Executors.newScheduledThreadPool(1);
        providerExecutor = Executors.newScheduledThreadPool(1);
    }
    
    public void stop() {
        log.info(" ====>>>> [SunnyRegistry] : stop with health checker.");
        gracefulShutdown(consumerExecutor);
        gracefulShutdown(providerExecutor);
    }
    public void check(Callback callback) {
        executor.scheduleWithFixedDelay(() -> {
            log.debug(" schedule to check kk registry ... [{}]", DTF.format(LocalDateTime.now()));
            try {
                callback.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, interval, interval, TimeUnit.MILLISECONDS);
    }
    public void consumerCheck(Callback callback) {
        consumerExecutor.scheduleAtFixedRate(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }, 1, 5, TimeUnit.SECONDS);
    }
    
    public void providerCheck(Callback callback) {
        providerExecutor.scheduleAtFixedRate(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
    
    private void gracefulShutdown(ScheduledExecutorService executorService) {
        executorService.shutdown();
        try {
            if(executorService.awaitTermination(1000, TimeUnit.MILLISECONDS) && !executorService.isTerminated()) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            // ignore
        }
    }
    public interface Callback {
        void call() throws Exception;
    }
}
