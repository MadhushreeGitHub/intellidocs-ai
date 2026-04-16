package com.intellidocs.intellidocs_ai.config;

import io.netty.util.concurrent.ThreadPerTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "documentPRocessingExecutor")
    public Executor documentProcessingExecutor() {
        // Customize the executor as needed (e.g., thread pool size)
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5); //always-alive threads
        executor.setMaxPoolSize(20); // burst ceiling
        executor.setQueueCapacity(100);//tasks queued when all 20 threads are busy
        executor.setThreadNamePrefix("doc-processor-"); //Visible in logs for easier debugging
        executor.setWaitForTasksToCompleteOnShutdown(true); //graceful shutdown
        executor.setAwaitTerminationSeconds(60); //wait up to 60s before forcing shutdown
        executor.initialize();
        return executor;
    }

    @Bean(name = "embeddingExecutor")
    public Executor embeddingExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); //always-alive threads
        executor.setMaxPoolSize(10); // burst ceiling
        executor.setQueueCapacity(50);//tasks queued when all 10 threads are busy
        executor.setThreadNamePrefix("embedding-"); //Visible in logs for easier debugging
        executor.setWaitForTasksToCompleteOnShutdown(true); //graceful shutdown
        executor.setAwaitTerminationSeconds(60); //wait up to 60s before forcing shutdown
        executor.initialize();
        return executor;
    }

}
