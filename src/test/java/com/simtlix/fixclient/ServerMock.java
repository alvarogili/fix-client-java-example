package com.simtlix.fixclient;

import quickfix.*;
import quickfix.field.*;
import quickfix.fix50.MarketDataIncrementalRefresh;
import quickfix.fix50.MarketDataRequest;
import quickfix.fix50.MarketDataSnapshotFullRefresh;
import quickfix.fix50.component.Instrument;
import quickfix.fixt11.Logon;

/**
 * By https://blog.10pines.com/es/2011/11/21/protocolo-fix-aspectos-basicos-y-utilizacion-en-java-mediante-la-librera-quickfixj/
 */
public class ServerMock extends ApplicationAdapter {

    String mDReqID;

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
            //Campo requerido si el mensaje surge de un pedido mediante un mensaje
            //MarkeDataRequest. Es el único identificador de la solicitud (copia el valor desde
            //el mensaje MarketDataRequest).
            mDReqID = marketDataRequest.getMDReqID().getValue();
            marketDataSnapshotFullRefresh.set(new MDReqID(mDReqID));
            //Describe el tipo de libro para la cual se realiza la suscripción. Puede ser
            //utilizada cuando se solicitan varias suscripciones sobre la misma conexión. Los
            //libros de futuros siempre son order depth (3).
            marketDataSnapshotFullRefresh.set(new MDBookType(1));
            //Representación “humana” del título.
            marketDataSnapshotFullRefresh.set(new Symbol("EUR/USD"));

            MarketDataSnapshotFullRefresh.NoMDEntries group =
                    new MarketDataSnapshotFullRefresh.NoMDEntries();
            //Tipo de información de mercado solicitada.
            group.set(new MDEntryType(MDEntryType.BID));
            //Precio asociado a la entrada de datos informada. Requerido condicionalmente
            //si el campo MDEntryType no es Variación o Volumen operado.
            group.set(new MDEntryPx(12.32));
            //Cantidad o volumen representado por la entrada de datos informada. Requerido
            //condicionalmente si el campo MDEntryType = Compra(BID), Venta(OFFER), Operado(TRADE),
            //Volumen de negociación (TRADE_VOLUME), o Interés abierto (OPEN_INTEREST)
            group.set(new MDEntrySize(100));
            marketDataSnapshotFullRefresh.addGroup(group);

            try {
                Session.sendToTarget(marketDataSnapshotFullRefresh, sessionId);
            } catch (SessionNotFound e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void enviarMarketDataIncrementalRefresh(int amountToSend) {
        MarketDataIncrementalRefresh marketDataIncrementalRefresh = new MarketDataIncrementalRefresh();
        //TODO: completar
    }

}
