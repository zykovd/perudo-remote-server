package com.suai.perudo.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.suai.perudo.model.PerudoModel;
import com.suai.perudo.model.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Party implements Serializable {
    @Expose private long id;
    @Expose private String title;
    @Expose private PerudoModel model;
    @Expose private String message;
    @Expose private String loser;
    @Expose private boolean newTurn = false;

    @Expose private int numberOfBots;

    //@SerializedName("PlayersMap")
    @Expose private HashMap<String, Player> players = new HashMap<>();
    private ArrayList<WebUser> webUsers = new ArrayList<>();

    @Expose private LinkedList<ChatMessage> chatMessages = new LinkedList<>();
    @Expose private int maxChatMessages = 30;
    @Expose private boolean newChatMessage = false;

    public Party(long id) {
        this.id = id;
        this.title = "Party" + id;
    }

    public Party(long id, String title) {
        this.id = id;
        this.title = title;
    }

    public void addChatMessage(ChatMessage message) {
        if (chatMessages.size() == maxChatMessages) {
            chatMessages.removeFirst();
        }
        chatMessages.addLast(message);
    }

    public boolean isNewChatMessage() {
        return newChatMessage;
    }

    public void setNewChatMessage(boolean newChatMessage) {
        this.newChatMessage = newChatMessage;
    }

    public String getLoser() {
        return loser;
    }

    public void setLoser(String loser) {
        this.loser = loser;
    }

    public int getNumberOfBots() {
        return numberOfBots;
    }

    public void setNumberOfBots(int numberOfBots) {
        this.numberOfBots = numberOfBots;
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

    public HashMap<String, Player> getPlayers() {
        return players;
    }

    public void setPlayers(HashMap<String, Player> players) {
        this.players = players;
    }

    public void addPlayer(WebUser webUser) {
        players.put(webUser.getLogin(), new Player(webUser.getLogin()));
        webUsers.add(webUser);
    }

    public void removePlayer(WebUser webUser, Player player) {
        model.removePlayer(player);
        players.remove(webUser.getLogin(), player);
        webUsers.remove(webUser);
    }

    public LinkedList<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public long getId() {
        return id;
    }

    public boolean contains(WebUser webUser) {
        return players.containsKey(webUser.getLogin());
    }

    public PartyHeader getPartyHeader() {
        return new PartyHeader(this);
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setChatMessages(LinkedList<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public int getMaxChatMessages() {
        return maxChatMessages;
    }

    public void setMaxChatMessages(int maxChatMessages) {
        this.maxChatMessages = maxChatMessages;
    }

    public ArrayList<WebUser> getWebUsers() {
        return webUsers;
    }

    public void setWebUsers(ArrayList<WebUser> webUsers) {
        this.webUsers = webUsers;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
