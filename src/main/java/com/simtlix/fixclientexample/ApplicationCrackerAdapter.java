package com.simtlix.fixclientexample;

import quickfix.*;

public abstract class ApplicationCrackerAdapter extends MessageCracker implements Application {
    protected ApplicationCrackerAdapter() {
    }

    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
    }

    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
    }

    public void onCreate(SessionID sessionId) {
    }

    public void onLogon(SessionID sessionId) {
    }

    public void onLogout(SessionID sessionId) {
    }

    public void toAdmin(Message message, SessionID sessionId) {
    }

    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
    }
}