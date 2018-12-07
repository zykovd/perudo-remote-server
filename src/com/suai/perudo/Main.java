package com.suai.perudo;

import com.suai.perudo.web.PerudoServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        try {
            new PerudoServer(port).start();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
