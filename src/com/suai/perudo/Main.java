package com.suai.perudo;

import com.suai.perudo.web.PerudoRemoteServer;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        new PerudoRemoteServer(port).start();
    }
}
