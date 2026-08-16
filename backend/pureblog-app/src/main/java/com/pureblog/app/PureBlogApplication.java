package com.pureblog.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.pureblog", exclude = {
        org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration.class
})
@MapperScan("com.pureblog.**.mapper")
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class PureBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(PureBlogApplication.class, args);
    }
}
