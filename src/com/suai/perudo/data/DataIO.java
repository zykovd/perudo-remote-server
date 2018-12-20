package com.suai.perudo.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suai.perudo.web.Party;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

public class DataIO {

    private String pathUsers = "/home/dmitry/IdeaProjects/perudo-remote-server/data/users.json";
    private String pathPartiesDir = "/home/dmitry/IdeaProjects/perudo-remote-server/data/parties/";
    private String pathParties = "/home/dmitry/IdeaProjects/perudo-remote-server/data/parties/party";
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.setPrettyPrinting()
                                .create();

    private UserDB userDB;

    public DataIO() throws IOException {
        String usersJson = new String(Files.readAllBytes(Paths.get(pathUsers)));
        userDB = gson.fromJson(usersJson, UserDB.class);
        if (userDB == null) {
            userDB = new UserDB();
        }
    }

    public ArrayList<Party> loadPartys() throws IOException {
        ArrayList<Party> res = new ArrayList<>();
        Iterator<Path> dir = Files.list(Paths.get(pathPartiesDir)).sorted().iterator();
        long partiesNum = Files.list(Paths.get(pathPartiesDir)).count();
        System.out.println("partiesNum = " + partiesNum);
        while (dir.hasNext()) {
            Path path = dir.next();
            Party party = gson.fromJson(new String(Files.readAllBytes(path)), Party.class);
            res.add(party);
        }
        return res;
    }

    public boolean addUser(String login, String password) throws IOException {
        if (userDB.contains(login))
            return false;
        userDB.addUser(login, password);
        Files.write(Paths.get(pathUsers), gson.toJson(userDB).getBytes());
        return true;
    }

    public void addParty(Party party) throws IOException {
        String path = pathParties + party.getId() +".json";
        if (!Files.exists(Paths.get(path))) {
            Files.createFile(Paths.get(path));
            Files.write(Paths.get(path), gson.toJson(party).getBytes());
        }
    }

    public void refreshParty(Party party) {
        String path = pathParties + party.getId() +".json";
        if (Files.exists(Paths.get(path))) {
            try {
                Files.write(Paths.get(path), gson.toJson(party).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeParty(Party party) throws IOException {
        Files.deleteIfExists(Paths.get(pathParties + party.getId() +".json"));
    }


    public boolean checkPassword(String login, String password) {
        return userDB.checkPassword(login, password);
    }
}
