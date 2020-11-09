package com.simtlix.fixclientexample;

import quickfix.*;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.field.*;
import quickfix.fix50.*;
import quickfix.fixt11.Logon;

/**
 * By https://blog.10pines.com/es/2011/11/21/protocolo-fix-aspectos-basicos-y-utilizacion-en-java-mediante-la-librera-quickfixj/
 */
public class ExampleClientApplication extends ApplicationCrackerAdapter {

    private final String usuario;
    private final String password;

    public ExampleClientApplication(String usuario, String password) {
        this.usuario = usuario;
        this.password = password;
    }

    /**
     * Esta función es llamada cuando se establece una sesión
     * @param sessionId
     */
    @Override
    public void onCreate(SessionID sessionId) {
        System.out.println("onCreate");
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
        System.out.println("onLogon");
        try {
            enviarSusbscripciones(sessionId);
        } catch (SessionNotFound e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Crea subscripciones
     * @param sessionId
     */
    private void enviarSusbscripciones(SessionID sessionId) throws SessionNotFound {
        MarketDataRequest marketDataRequest = new MarketDataRequest();

        Integer counter = 1;
        marketDataRequest.set(new MDReqID(counter.toString()));
        marketDataRequest.set(new SubscriptionRequestType('1'));
        marketDataRequest.set(new MarketDepth(1));
        marketDataRequest.set(new MDUpdateType(1));
        Session.sendToTarget(marketDataRequest, sessionId);
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
    public void onMessage(MarketDataSnapshotFullRefresh message, SessionID sessionID) {
        System.out.println("MarketDataSnapshotFullRefresh");
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
            message.setField(new Username(usuario));
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
}