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
	private SocketInitiator socketInitiator;


	@BeforeClass
	public static void setUpClass() throws ConfigError, FileNotFoundException, InterruptedException {
		settings = new SessionSettings(new FileInputStream("src/main/resources/application.properties"));
		iniciarServidor();
	}

	@After
	public void tearDown() {
		socketInitiator.stop(true);
	}

	@AfterClass
	public static void tearDownClass() {
		acceptor.stop();
	}

	@Test
	public void invalidPassword() throws InterruptedException, ConfigError  {
		FixClient application = new FixClient(user, "invalidPassword");
		socketInitiator = new SocketInitiator(application, new MemoryStoreFactory(), settings,
				new ScreenLogFactory(), new DefaultMessageFactory());
		socketInitiator.start();
		Thread.sleep(5000l);
		assertFalse(application.getConnected());
	}

	@Test
	public void flujoNominal() throws InterruptedException, ConfigError, FieldNotFound {
		FixClient application = new FixClient(user, password);
		socketInitiator = new SocketInitiator(application, new MemoryStoreFactory(), settings,
					new ScreenLogFactory(), new DefaultMessageFactory());
		socketInitiator.start();
		Thread.sleep(5000l);
		assertTrue(application.getConnected());

		MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = new MarketDataSnapshotFullRefresh.NoMDEntries();
		MDEntryType mdEntryType = new MDEntryType();
		MDEntryPx mdEntryPx = new MDEntryPx();
		MDEntrySize mdEntrySize = new MDEntrySize();
		application.getLastMarketDataSnapshotFullRefresh().getGroup(1, noMDEntries);
		noMDEntries.get(mdEntryType);
		noMDEntries.get(mdEntryPx);
		noMDEntries.get(mdEntrySize);

		assertEquals(12.32, mdEntryPx.getValue(),0.0);
	}

	private static void iniciarServidor() throws ConfigError, InterruptedException, FileNotFoundException {
		ServerMock application = new ServerMock();
		SessionSettings serverSettings = new SessionSettings(new FileInputStream("src/test/resources/server.properties"));
		acceptor = new SocketAcceptor(application, new MemoryStoreFactory(), serverSettings,
				new ScreenLogFactory(), new DefaultMessageFactory());
		acceptor.start();
		Thread.sleep(4000l);
	}
}
