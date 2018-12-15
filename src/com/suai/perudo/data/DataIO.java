package com.suai.perudo.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suai.perudo.web.Party;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataIO {

    private String pathUsers = "/home/dmitry/IdeaProjects/perudo-remote-server/data/users.json";
    private String pathParties = "/home/dmitry/IdeaProjects/perudo-remote-server/data/parties/party";
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.setPrettyPrinting()
                                .create();

    private UserDB userDB;
    //private PartiesDB partiesDB;

    public DataIO() throws IOException {
        String usersJson = new String(Files.readAllBytes(Paths.get(pathUsers)));
        userDB = gson.fromJson(usersJson, UserDB.class);
        if (userDB == null) {
            userDB = new UserDB();
        }

        /*String partiesJson = new String(Files.readAllBytes(Paths.get(pathParties)));
        partiesDB = gson.fromJson(partiesJson, PartiesDB.class);
        if (partiesDB == null) {
            partiesDB = new PartiesDB();
        }*/

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
