package com.simtlix.fixclient;

import quickfix.*;
import quickfix.Message;
import quickfix.field.*;
import quickfix.fix50.*;
import quickfix.fixt11.Logon;
import quickfix.fixt11.Logout;

import java.util.HashMap;
import java.util.Map;

/**
 * Financial Information eXchange Client
 * @author Alvaro Gili
 */
public class FixClient extends ApplicationCrackerAdapter {

    private final String user;
    private final String password;
    private Boolean connected = false;
    private Map<String, Double> priceBySymbol = new HashMap();

    //attributes for text purpose
    private String lastTextMessage = "";
    MarketDataSnapshotFullRefresh lastMarketDataSnapshotFullRefresh = null;

    public FixClient(String user, String password) {
        this.user = user;
        this.password = password;
    }

    /**
     * It function was called when the session was established
     * @param sessionId Session's ID
     */
    @Override
    public void onCreate(SessionID sessionId) {
    }

    /**
     * Esta función es llamada cuando una contraparte envia un
     * mensaje de tipo Logon (A). Por defecto, acepta toda conexión
     * entrante. Es posible utilizar esta función para crear
     * autenticación simple, o basada en sistemas de autententicación centralizadas.
     * @param sessionId
     */
    @Override
    public  void onLogon(SessionID sessionId) {
        System.out.println("Login with server successfully");
        this.connected = true;
        try {
            enviarSusbscripciones(sessionId);
        } catch (SessionNotFound e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Notifica cuando una sesión FIX ya no esta en linea,
     * o cuando se recibe un mensaje de Logout (5). Posibles
     * causes de ser llamada esta función son: un logout válido,
     * problemas en la conexión, o terminación forzada de la conexión
     * @param sessionId
     */
    @Override
    public void onLogout(SessionID sessionId) {
        System.out.println("onLogout");
    }

    /**
     * Es prácticamente el núcleo de una aplicación quickfix.
     * Esta función permitirá recibir las confirmaciones
     * de una orden emitida (o rechazo). La excepción ”FieldNotFound”
     * permite decir a la contraparte que faltaron campos en el
     * mensaje, esta excepción es tirada automáticamente por la clase
     * Message. La excepción ”UnsupportedMessageType” permite
     * indicar a la contra parte que no se posible procesar ese tipo
     * de mensaje. La excepción ”IncorrectTagValue” permite indicar que
     * un campo tiene un valor o rango no soportado.
     * @param message
     * @param sessionId
     * @throws FieldNotFound
     */
    @Override
    public  void fromApp(Message message, SessionID sessionId) {
        try {
            System.out.println("Message received: " + message.toString());
            crack(message, sessionId);
        } catch (UnsupportedMessageType unsupportedMessageType) {

            unsupportedMessageType.printStackTrace();
        } catch (IncorrectTagValue incorrectTagValue) {
            incorrectTagValue.printStackTrace();
        } catch (FieldNotFound fieldNotFound) {
            fieldNotFound.printStackTrace();
        }
    }

    @quickfix.MessageCracker.Handler
    public void onMessage(ExecutionReport message, SessionID sessionID) {
        try {
            if (esExecutionReportNew(message)) {
                new Exception("No es una orden nueva");
            }
        } catch (FieldNotFound fieldNotFound) {
            fieldNotFound.printStackTrace();
        }
    }

    @quickfix.MessageCracker.Handler
    public void onMessage(MarketDataRequestReject message, SessionID sessionID) {
        System.out.println("MarketDataRequestReject");
    }

    @quickfix.MessageCracker.Handler
    public void onMessage(MarketDataSnapshotFullRefresh message, SessionID sessionID) throws FieldNotFound {
        lastMarketDataSnapshotFullRefresh = message;
        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = new MarketDataSnapshotFullRefresh.NoMDEntries();
        for(int i = 0; i < message.getNoMDEntries().getValue(); i++) {
            MDEntryType mdEntryType = new MDEntryType();
            MDEntryPx mdEntryPx = new MDEntryPx();
            MDEntrySize mdEntrySize = new MDEntrySize();
            message.getGroup(i+1, noMDEntries);
            noMDEntries.get(mdEntryType);
            noMDEntries.get(mdEntryPx);
            noMDEntries.get(mdEntrySize);
            //TODO almacenar
            priceBySymbol.put("EUR/USD", mdEntryPx.getValue());
        }

    }

    @quickfix.MessageCracker.Handler
    public void onMessage(MarketDataIncrementalRefresh message, SessionID sessionID) throws FieldNotFound {
        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = new MarketDataSnapshotFullRefresh.NoMDEntries();
        for(int i = 0; i < message.getNoMDEntries().getValue(); i++) {
            MDEntryType mdEntryType = new MDEntryType();
            MDEntryPx mdEntryPx = new MDEntryPx();
            MDEntrySize mdEntrySize = new MDEntrySize();
            message.getGroup(i+1, noMDEntries);
            noMDEntries.get(mdEntryType);
            noMDEntries.get(mdEntryPx);
            noMDEntries.get(mdEntrySize);
            //TODO almacenar
            priceBySymbol.put("EUR/USD", mdEntryPx.getValue());
        }
    }

    @quickfix.MessageCracker.Handler
    public void onMessage(Logon message, SessionID sessionID) {
    }

    @quickfix.MessageCracker.Handler
    public void onMessage(RejectLogon message, SessionID sessionID) {
        lastTextMessage = message.getMessage();
    }

    @quickfix.MessageCracker.Handler
    public void onMessage(Logout message, SessionID sessionID) throws FieldNotFound {
        lastTextMessage = message.getText().toString();
    }

    private  boolean esExecutionReportNew(ExecutionReport executionReport) throws FieldNotFound {
        return executionReport.getExecType().getValue() == ExecType.NEW;
    }

    /**
     * Notifica cuando el Engine QuickFIX enviará un mensaje administrativo a la
     * contra parte. Algo interesante, esa que la referencia que es pasada del
     * mensaje no es constante. Es decir, se puede modificar el mensaje.
     * @param message
     * @param sessionId
     */
    @Override
    public  void toAdmin(Message message, SessionID sessionId) {
        if (message instanceof Logon) {
            message.setField(new Username(user));
            message.setField(new Password(password));
        }
    }

    /**
     * Notifica cuando llega al engine QuickFIX un mensaje administrativo de su contra parte
     * @param message
     * @param sessionId
     * @throws FieldNotFound
     * @throws IncorrectDataFormat
     * @throws IncorrectTagValue
     * @throws RejectLogon
     */
    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        System.out.println("fromAdmin");
        fromApp(message, sessionId);
    }

    /**
     * Función de callback que se llama cuando se envía un mensaje de
     * aplicación. La referencia al mensaje no es constante. Arrojar
     * una excepción ”DoNotSend”, hace que el engine QuickFIX no envie este mensaje.
     * @param message
     * @param sessionId
     * @throws DoNotSend
     */
    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        System.out.println("toApp");
    }

    /**
     * Crea subscripciones
     * @param sessionId
     */
    private void enviarSusbscripciones(SessionID sessionId) throws SessionNotFound {

        //basado en pag 35 de SBAFIX_MD_1_18_9_MAR2020_preliminar.pdf
        //ver notas pag 41
        MarketDataRequest marketDataRequest = new MarketDataRequest();

        //session ID
        marketDataRequest.set(new MDReqID(sessionId.toString()));
        //Indica que tipo de respuesta se está esperando. Valores válidos:
        //0:Captura, 1:Captura+actualizaciones (subscripción) no
        //soportada, 2:Anular subscripción (no soportada para DMA).
        marketDataRequest.set(new SubscriptionRequestType('0'));
        //Profundidad del Mercado tanto para capturas de libro, como
        //actualizaciones incrementales.
        //Para DMA no esta soportado, siempre se informan 5 niveles
        marketDataRequest.set(new MarketDepth(1));
        //esta etiqueta se usa para describir el tipo de actualización de
        //Market Data y ByMA requiere el valor 1 en este campo.
        marketDataRequest.set(new MDUpdateType(1));
        //Especifica si las entradas tienen o no que ser agregadas.
        marketDataRequest.set(new AggregatedBook(false));
        // MDEntryType debe ser el primer campo en este grupo de
        //repetición. Se trata de un listado detallando la información
        //(MarketDataEntries) que la firma solicitante está interesada en
        //recibir
        MarketDataRequest.NoMDEntryTypes entries = new MarketDataRequest.NoMDEntryTypes();
        entries.set(new MDEntryType(MDEntryType.BID));
        marketDataRequest.addGroup(entries);

        //Especifica la cantidad de símbolos repetidos en el grupo.
        marketDataRequest.set(new NoRelatedSym(1));
        MarketDataRequest.NoRelatedSym symbols = new MarketDataRequest.NoRelatedSym();
        symbols.set(new Symbol("EUR/USD"));
        marketDataRequest.addGroup(symbols);
        //Representación “humana” del título. En caso de no existir un
        //símbolo para el intrumento, puede asignarse el valor del
        //SecurityID. Solo usar “[N/A]” cuando se está solicitando
        //información por producto (Product = 7).
        //Ver “Instrumentos usados para informar datos estadisticos” para
        //suscribir mensajes de estadísticas
        Session.sendToTarget(marketDataRequest, sessionId);
        /*
        Luego de una subscripción correcta se recibe un MarketDataSnapshotFullRefresh (MsgType = W)
        y posteriormente se van recibiendo MarketDataIncrementalRefresh (MsgType = X)
         */
    }

    public Boolean getConnected() {
        return connected;
    }

    public Map<String, Double> getPriceBySymbol() {
        return priceBySymbol;
    }
}
