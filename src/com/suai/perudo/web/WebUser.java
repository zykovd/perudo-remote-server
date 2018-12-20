package com.suai.perudo.web;

import com.suai.perudo.model.Player;

import java.io.*;
import java.net.Socket;

/**
 * Created by dmitry on 13.09.18.
 */

public class WebUser implements Serializable {

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Party currentParty;

    private Player currentPlayer;
    private String login;

    public WebUser(Socket socket) throws IOException {
        this.socket = socket;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public Party getCurrentParty() {
        return currentParty;
    }

    public void setCurrentParty(Party currentParty) {
        this.currentParty = currentParty;
    }

    public void disconnect() throws IOException {
        currentParty = null;
        dataInputStream.close();
        dataOutputStream.close();
        socket.close();
    }

    public boolean isConnected() {
        if (socket == null)
            return false;
        else
            return socket.isConnected();
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (this.getClass() != obj.getClass())
            return false;
        return this.getLogin().equals(((WebUser) obj).getLogin());
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setDataInputStream(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WebUser{");
        sb.append("login='").append(login).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
