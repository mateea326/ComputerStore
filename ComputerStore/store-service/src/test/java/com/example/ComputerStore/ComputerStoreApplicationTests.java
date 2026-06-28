package com.example.ComputerStore;

import com.example.ComputerStore.client.NotificationServiceClient;
import com.example.ComputerStore.client.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ComputerStoreApplicationTests {

	@MockBean
	private UserServiceClient userServiceClient;

	@MockBean
	private NotificationServiceClient notificationServiceClient;

	@Test
	void contextLoads() {
	}

}
