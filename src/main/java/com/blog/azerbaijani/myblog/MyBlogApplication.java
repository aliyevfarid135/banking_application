package com.blog.azerbaijani.myblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.blog.azerbaijani")
@EntityScan(basePackages = "com.blog.azerbaijani.entity")
@EnableJpaRepositories(basePackages = "com.blog.azerbaijani.repository")
public class MyBlogApplication {
	public static void main(String[] args) {
		SpringApplication.run(MyBlogApplication.class, args);
	}
}
