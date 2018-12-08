package com.suai.perudo.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

public class DataIO {

    private String path = "/home/dmitry/IdeaProjects/perudo-remote-server/data/users.json";
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.setPrettyPrinting()
                                .create();

    private UserDB userDB;

    public DataIO() throws IOException {
        String string = new String(Files.readAllBytes(Paths.get(path)));
        userDB = gson.fromJson(string, UserDB.class);
        if (userDB == null) {
            userDB = new UserDB();
        }
    }

    public boolean addUser(String login, String password) throws IOException {
        if (userDB.contains(login))
            return false;
        userDB.addUser(login, password);
        Files.write(Paths.get(path), gson.toJson(userDB).getBytes());
        return true;
    }

    public boolean checkPassword(String login, String password) {
        return userDB.checkPassword(login, password);
    }
}
