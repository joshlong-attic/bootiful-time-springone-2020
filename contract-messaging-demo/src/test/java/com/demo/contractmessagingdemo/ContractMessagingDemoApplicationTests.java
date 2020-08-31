package com.demo.contractmessagingdemo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.SimpleDateFormat;

@ExtendWith(SpringExtension.class)
@AutoConfigureMessageVerifier
@SpringBootTest
class ContractMessagingDemoApplicationTests {

	@Autowired
	private MessageVerifier verifier;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	boolean received() {
		return true;
	}

	@Test
	void contextLoads() {
	}
}
