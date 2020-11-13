package com.simtlix.fixclient;

import quickfix.*;
import quickfix.field.*;
import quickfix.fix50.MarketDataRequest;
import quickfix.fix50.MarketDataSnapshotFullRefresh;
import quickfix.fix50.component.Instrument;
import quickfix.fixt11.Logon;

/**
 * By https://blog.10pines.com/es/2011/11/21/protocolo-fix-aspectos-basicos-y-utilizacion-en-java-mediante-la-librera-quickfixj/
 */
public class ServerMock extends ApplicationAdapter {

    @Override
    public  void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, RejectLogon {
        if (message instanceof Logon) {
            if (!usuarioYPasswordCorrectos((Logon) message)) {
                throw new RejectLogon();
            }
        }
    }

    private  boolean usuarioYPasswordCorrectos(Logon logon) throws FieldNotFound {
        return logon.getUsername().getValue().equals("user")
                && logon.getPassword().getValue().equals("password");
    }

    @Override
    public  void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectTagValue,
            UnsupportedMessageType {
        if (message instanceof MarketDataRequest) {
            MarketDataRequest marketDataRequest = ((MarketDataRequest) message);
            MarketDataSnapshotFullRefresh marketDataSnapshotFullRefresh = new MarketDataSnapshotFullRefresh();
            String mDReqID = marketDataRequest.getMDReqID().getValue();
            marketDataSnapshotFullRefresh.set(new MDReqID(mDReqID));
            marketDataSnapshotFullRefresh.set(new MDBookType(1));
            marketDataSnapshotFullRefresh.set(new Symbol("EUR/USD"));

            MarketDataSnapshotFullRefresh.NoMDEntries group =
                    new MarketDataSnapshotFullRefresh.NoMDEntries();
            group.set(new MDEntryType('0'));
            group.set(new MDEntryPx(12.32));
            group.set(new MDEntrySize(100));
            marketDataSnapshotFullRefresh.addGroup(group);

            try {
                Session.sendToTarget(marketDataSnapshotFullRefresh, sessionId);
            } catch (SessionNotFound e) {
                throw new RuntimeException(e);
            }
        }
    }

}
