package com.suai.perudo;

import com.suai.perudo.web.PerudoServer;
import javafx.util.Pair;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        new PerudoServer(port).start();
    }
}
