package com.suai.perudo.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by dmitry on 13.09.18.
 */

public class WebUser {

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Party currentParty;

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
}
