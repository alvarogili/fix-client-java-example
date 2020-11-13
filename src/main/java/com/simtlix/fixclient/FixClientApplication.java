package com.simtlix.fixclient;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import quickfix.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootApplication
public class FixClientApplication {

	public static void main(String[] args) throws InterruptedException {
		try {
			SessionSettings settings = new SessionSettings(new FileInputStream("src/main/resources/application.properties"));
			String user = settings.getDefaultProperties().getProperty("user");
			String password = settings.getDefaultProperties().getProperty("password");
			FixClient application = new FixClient(user, password);

			SocketInitiator socketInitiator = new SocketInitiator(application, new MemoryStoreFactory(), settings,
					new ScreenLogFactory(), new DefaultMessageFactory());
			socketInitiator.start();
		} catch (ConfigError configError) {
			configError.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Thread.sleep(5000l);

	}

}
