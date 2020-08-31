package com.demo.contractmessagingdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

@SpringBootApplication
public class ContractMessagingDemoApplication {

	@Bean
	public Supplier<String> timeSupplier() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return () -> sdf.format(new Date());
	}

	public static void main(String[] args) {
		SpringApplication.run(ContractMessagingDemoApplication.class, args);
	}
}
