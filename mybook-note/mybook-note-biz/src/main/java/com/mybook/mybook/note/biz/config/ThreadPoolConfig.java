package com.mybook.mybook.note.biz.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {
    /**
     * ThreadPoolTaskExecutor是spring的组件，底层仍是jdk的ThreadPoolExecutor。
     * ThreadPoolExecutor需要自行管理生命周期
     * ThreadPoolTaskExecutor有更好的阻塞队列配置和友好的监控支持。支持自动管理线程池的生命周期。
     * CPU密集，core=N+1;IO密集：core=N*2
     * maxpool：队列较大，maxpool可与corepoll相近，队列较小，maxpool要设置较大（防止频繁拒绝）
     */
    @Bean
    public Executor taskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("NoteExecutor-");
        executor.setKeepAliveSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());;
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}
