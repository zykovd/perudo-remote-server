package com.suai.perudo.web;

import com.suai.perudo.model.PerudoModel;
import com.suai.perudo.model.Player;

import java.util.HashMap;

public class Party {
    private long id;
    private String title;
    private PerudoModel model;
    private String message;
    private boolean newTurn = false;
    private HashMap<WebUser, Player> players = new HashMap<>();

    public Party(long id) {
        this.id = id;
        this.title = "Party" + id;
    }

    public Party(long id, String title) {
        this.id = id;
        this.title = title;
    }

    public PerudoModel getModel() {
        return model;
    }

    public void setModel(PerudoModel model) {
        this.model = model;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isNewTurn() {
        return newTurn;
    }

    public void setNewTurn(boolean newTurn) {
        this.newTurn = newTurn;
    }

    public HashMap<WebUser, Player> getPlayers() {
        return players;
    }

    public void setPlayers(HashMap<WebUser, Player> players) {
        this.players = players;
    }

    public void addPlayer(WebUser webUser) {
        players.put(webUser, new Player(webUser.getLogin()));
    }

    public void removePlayer(WebUser webUser, Player player) {
        model.removePlayer(player);
        players.remove(webUser, player);
    }

    public long getId() {
        return id;
    }

    public boolean contains(WebUser webUser) {
        return players.containsKey(webUser);
    }

    public PartyHeader getPartyHeader() {
        return new PartyHeader(this);
    }
}
