package com.simtlix.fixclientexample;

import quickfix.ApplicationAdapter;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.field.ExecType;
import quickfix.field.Password;
import quickfix.field.Username;
import quickfix.fix50.ExecutionReport;
import quickfix.fix50.NewOrderSingle;
import quickfix.fixt11.Logon;

/**
 * By https://blog.10pines.com/es/2011/11/21/protocolo-fix-aspectos-basicos-y-utilizacion-en-java-mediante-la-librera-quickfixj/
 */
public class ExampleClientApplication extends ApplicationAdapter {
    private boolean seEjecutoOrdenCorrectamente = false;
    private  boolean estaLogueado = false;

    private final NewOrderSingle newOrder;
    private final String usuario;
    private final String password;

    public ExampleClientApplication(NewOrderSingle newOrder, String usuario, String password) {
        this.newOrder = newOrder;
        this.usuario = usuario;
        this.password = password;
    }

    @Override
    public  void onLogon(SessionID sessionId) {
        this.estaLogueado = true;
        try {
            Session.sendToTarget(this.newOrder, sessionId);
        } catch (SessionNotFound e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public  void fromApp(Message message, SessionID sessionId) throws FieldNotFound {
        if (message instanceof ExecutionReport) {
            ExecutionReport executionReport = (ExecutionReport) message;
            if (esExecutionReportNew(executionReport)) {
                // Como el mensaje corresponde a una creacion de orden,
                // verifico que se refiera a la misma orden que acabo de enviar
                // comparando su ClOrdID.
                if (esClOrdIDCorrecto(executionReport)) {
                    this.seEjecutoOrdenCorrectamente = true;
                }
            }
        }else {

        }
    }

    private  boolean esClOrdIDCorrecto(ExecutionReport executionReport) throws FieldNotFound {
        return executionReport.getClOrdID().getValue().equals(this.newOrder.getClOrdID().getValue());
    }

    private  boolean esExecutionReportNew(ExecutionReport executionReport) throws FieldNotFound {
        return executionReport.getExecType().getValue() == ExecType.NEW;
    }

    @Override
    public  void toAdmin(Message message, SessionID sessionId) {
        if (message instanceof Logon) {
            message.setField(new Username(usuario));
            message.setField(new Password(password));
        }
    }

    public  boolean estaLogueado() {
        return estaLogueado;
    }

    public  boolean seEjecutoOrdenCorrectamente() {
        return seEjecutoOrdenCorrectamente;
    }
}
