package com.simtlix.fixclientexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix50.NewOrderSingle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Date;

@SpringBootApplication
public class FixClientExampleApplication {

	public static void main(String[] args) throws InterruptedException {
		String password = "password";
		String usuario = "usuario";

		NewOrderSingle newOrder = new NewOrderSingle(new ClOrdID("12345"), new Side(Side.SUBSCRIBE),
				new TransactTime(LocalDateTime.now()), new OrdType(OrdType.MARKET));
		newOrder.set(new OrderQty(1000));
		ExampleClientApplication application = new ExampleClientApplication(newOrder, usuario, password);


		SessionSettings settings = null;
		SocketInitiator socketInitiator = null;
		try {
			settings = new SessionSettings(new FileInputStream("src/main/resources/application.properties"));
			socketInitiator = new SocketInitiator(application, new MemoryStoreFactory(), settings,
					new ScreenLogFactory(), new DefaultMessageFactory());
			socketInitiator.start();
		} catch (ConfigError configError) {
			configError.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Thread.sleep(5000l);
		if(application.estaLogueado()) {
			System.out.println("se logueó");
		}
		if(application.seEjecutoOrdenCorrectamente()){
			System.out.println("Orden ejecutada");
		}
	}

}
