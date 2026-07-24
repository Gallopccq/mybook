package com.mybook.mybook.user.relation.biz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Configuration 相当于自动生成application.xml（bean的配置文件）
 * 配合@Bean，进行Bean的配置声名，待AnnotationConfigApplicationContext 或 AnnotationConfigWebApplicationContext类进行扫描
 */
@Configuration
public class ThreadPoolConfig {

    @Bean(name="taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(9); // max小而队列大，减少线程切换的资源损耗，多加的1为有效磁盘数
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("AuthExecutor-");
        executor.setKeepAliveSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}