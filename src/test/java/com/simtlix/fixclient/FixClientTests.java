package com.simtlix.fixclient;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import quickfix.*;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryType;
import quickfix.fix50.MarketDataSnapshotFullRefresh;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class FixClientTests {

	private final String password = "password";
	private final String user = "user";
	private static SocketAcceptor acceptor = null;
	private static SessionSettings settings = null;
	private SocketInitiator socketInitiatorFixClient;
	private FixClient fixClient;
	int amountMarketDataIncrementalRefresh = 0;
	ServerMock serverMock;


	@BeforeClass
	public static void setUpClass() throws ConfigError, FileNotFoundException, InterruptedException {
		settings = new SessionSettings(new FileInputStream("src/main/resources/application.properties"));
	}

	@Before
	public void setUp() throws ConfigError {
		fixClient = new FixClient(user, password);
		socketInitiatorFixClient = new SocketInitiator(fixClient, new MemoryStoreFactory(), settings,
				new ScreenLogFactory(), new DefaultMessageFactory());
	}

	@After
	public void tearDown() {
		socketInitiatorFixClient.stop(true);
		acceptor.stop();
	}

	@Test
	public void invalidPassword() throws InterruptedException, ConfigError, FileNotFoundException {
		iniciarServidor();
		fixClient = new FixClient(user, "invalidPassword");
		socketInitiatorFixClient = new SocketInitiator(fixClient, new MemoryStoreFactory(), settings,
				new ScreenLogFactory(), new DefaultMessageFactory());
		socketInitiatorFixClient.start();
		Thread.sleep(5000l);
		assertFalse(fixClient.getConnected());
	}

	@Test
	public void flujoNominalUnSimbolo() throws InterruptedException, ConfigError, FieldNotFound, FileNotFoundException {
		iniciarServidor();
		socketInitiatorFixClient.start();
		Thread.sleep(3000l);
		assertTrue(fixClient.getConnected());

		assertEquals(12.32, fixClient.getPriceBySymbol().get("EUR/USD"),0.0);
	}


	@Test
	public void flujoNominalMarketDataIncrementalRefresh() throws InterruptedException, ConfigError, FileNotFoundException, FieldNotFound {
		amountMarketDataIncrementalRefresh = 2;
		iniciarServidor();
		socketInitiatorFixClient.start();
		Thread.sleep(3000l);
		serverMock.initMarketDataIncrementalRefresh();
		Thread.sleep(4000l);
		assertTrue(fixClient.getConnected());

		assertEquals(20.0, fixClient.getPriceBySymbol().get("EUR/USD"),0.0);
	}

	private void iniciarServidor() throws ConfigError, InterruptedException, FileNotFoundException {
		serverMock = new ServerMock();
		serverMock.setAmountMarketDataIncrementalRefresh(this.amountMarketDataIncrementalRefresh);
		SessionSettings serverSettings = new SessionSettings(new FileInputStream("src/test/resources/server.properties"));
		acceptor = new SocketAcceptor(serverMock, new MemoryStoreFactory(), serverSettings,
				new ScreenLogFactory(), new DefaultMessageFactory());
		acceptor.start();
	}
}
