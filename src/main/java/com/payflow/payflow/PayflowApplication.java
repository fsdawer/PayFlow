package com.payflow.payflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling  // 스케줄러 활성화!
@SpringBootApplication(scanBasePackages = "com.payflow")
@EntityScan(basePackages = "com.payflow.domain")
@EnableJpaRepositories(basePackages = "com.payflow.domain")
public class PayflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayflowApplication.class, args);
	}

}
