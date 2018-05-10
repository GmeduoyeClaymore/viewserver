package io.viewserver.controller;

import io.viewserver.authentication.AuthenticationToken;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.IExecutionContext;
import io.viewserver.messages.IMessage;
import io.viewserver.network.*;
import io.viewserver.operators.deserialiser.DeserialiserOperator;
import rx.Observable;
import rx.Observer;
import rx.observables.AsyncOnSubscribe;

/**
 * Created by Gbemiga on 15/12/17.
 */

public class MockSession implements IPeerSession {

    @Override
    public void sendCommand(Command command) {

    }

    @Override
    public void addOpenCommand(Command command) {

    }

    @Override
    public void removeOpenCommand(Command command) {

    }

    @Override
    public Command getOpenCommand(int commandId) {
        return null;
    }

    @Override
    public void closeCommand(Command command) {

    }

    @Override
    public int getConnectionId() {
        return 0;
    }

    @Override
    public SessionType getSessionType() {
        return null;
    }

    @Override
    public void setAuthenticationToken(AuthenticationToken authenticationToken) {

    }

    @Override
    public void sendMessage(IMessage message) {

    }

    @Override
    public void sendCommandResult(CommandResult commandResult) {

    }

    @Override
    public void removeDisconnectionHandler(PeerSession.IDisconnectionHandler disconnectionHandler) {

    }

    @Override
    public void fireDisconnection() {

    }

    @Override
    public IExecutionContext getExecutionContext() {
        return null;
    }

    @Override
    public ICatalog getSessionCatalog() {
        return null;
    }

    @Override
    public IMessageManager getMessageManager() {
        return null;
    }

    @Override
    public void addDisconnectionHandler(PeerSession.IDisconnectionHandler disconnectionHandler) {

    }

    @Override
    public Observable onDisconnect() {
        return Observable.create(new AsyncOnSubscribe<Object, Object>() {
            @Override
            protected Object generateState() {
                return null;
            }

            @Override
            protected Object next(Object o, long l, Observer<Observable<?>> observer) {
                return null;
            }
        });
    }

    @Override
    public ICatalog getSystemCatalog() {
        return null;
    }

    @Override
    public IChannel getChannel() {
        return null;
    }

    @Override
    public Network getNetwork() {
        return null;
    }

    @Override
    public int getNextCommandId() {
        return 0;
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public AuthenticationToken getAuthenticationToken() {
        return null;
    }

    @Override
    public void addAuthenticationEventHandler(IPeerSessionAuthenticationHandler handler) {

    }

    @Override
    public void removeAuthenticationEventHandler(IPeerSessionAuthenticationHandler handler) {

    }

    @Override
    public String getCatalogName() {
        return null;
    }

    @Override
    public boolean shouldLog() {
        return false;
    }

    @Override
    public void registerDeserialiser(DeserialiserOperator deserialiser) {

    }

    @Override
    public void unregisterDeserialiser(DeserialiserOperator deserialiser) {

    }

    @Override
    public boolean isTornDown() {
        return false;
    }

    @Override
    public void tearDown() {

    }
}
