package com.suai.perudo.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suai.perudo.model.PerudoModel;
import com.suai.perudo.model.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by dmitry on 04.11.18.
 */

public class PerudoServerResponse implements Serializable{
    private PerudoServerResponseEnum responseEnum;

    private int totalDicesCount;
    private int currentBidQuantity;
    private int currentBidValue;
    private String currentTurnPlayerName;
    private String currentBidPlayerName;
    private boolean isMaputo;
    private boolean isGameStarted;

    private int[] dices;

    private ArrayList<Player> players;
    private ArrayList<PartyHeader> parties;
    private LinkedList<ChatMessage> chatMessages;

    private String message;

    public PerudoServerResponse(PerudoServerResponseEnum responseEnum, ArrayList<PartyHeader> parties) {
        this.responseEnum = responseEnum;
        this.dices = null;
        switch (responseEnum) {
            case PARTIES_LIST:
                message = "Parties list";
                this.parties = parties;
                break;
            default:
                message = "Something went wrong!";
                break;
        }

    }

    public PerudoServerResponse(PerudoServerResponseEnum responseEnum) {
        this.responseEnum = responseEnum;
        this.dices = null;
        switch (responseEnum) {
            case CONNECTED:
                message = "Connected to server";
                break;
            case JOINED_PARTY:
                message = "Connected to party";
                break;
            case JOIN_ERROR:
                message = "Could not connect to party!";
                break;
            case GAME_END:
                message = "Game ended!";
                break;
            default:
                message = "Something went wrong!";
                break;
        }
    }

    public PerudoServerResponse(PerudoServerResponseEnum responseEnum, ChatMessage message) {
        this.responseEnum = responseEnum;
        this.dices = null;
        switch (responseEnum) {
            case NEW_CHAT_MESSAGE:
                chatMessages = new LinkedList<>();
                chatMessages.addLast(message);
                break;
        }
    }


    public PerudoServerResponse(PerudoModel model, PerudoServerResponseEnum responseEnum, int[] dices) {
        this.responseEnum = responseEnum;
        this.dices = dices;
        switch (responseEnum) {
            case CONNECTED:
                break;
            case JOINED_PARTY:
                this.isMaputo = model.isMaputo();
                this.isGameStarted = model.isGameStarted();
                this.currentTurnPlayerName = model.getCurrentTurnPlayer().getName();
                this.totalDicesCount = model.getTotalDicesCount();
                this.currentBidQuantity = model.getCurrentBidQuantity();
                this.currentBidValue = model.getCurrentBidValue();
                this.currentBidPlayerName = model.getCurrentBidPlayer().getName();
                //this.chatMessages = chatMessages;
                break;
            case GAME_START:
                this.isMaputo = model.isMaputo();
                this.isGameStarted = model.isGameStarted();
                this.currentTurnPlayerName = model.getCurrentTurnPlayer().getName();
                this.totalDicesCount = model.getTotalDicesCount();
                this.currentBidQuantity = model.getCurrentBidQuantity();
                this.currentBidValue = model.getCurrentBidValue();
                this.currentBidPlayerName = model.getCurrentBidPlayer().getName();
                break;
            case WRONG_TURN:
                this.isMaputo = model.isMaputo();
                this.isGameStarted = model.isGameStarted();
                this.currentTurnPlayerName = model.getCurrentTurnPlayer().getName();
                this.totalDicesCount = model.getTotalDicesCount();
                this.currentBidQuantity = model.getCurrentBidQuantity();
                this.currentBidValue = model.getCurrentBidValue();
                this.currentBidPlayerName = model.getCurrentBidPlayer().getName();
                break;
            case INVALID_BID:
                this.isMaputo = model.isMaputo();
                this.isGameStarted = model.isGameStarted();
                this.currentTurnPlayerName = model.getCurrentTurnPlayer().getName();
                this.totalDicesCount = model.getTotalDicesCount();
                this.currentBidQuantity = model.getCurrentBidQuantity();
                this.currentBidValue = model.getCurrentBidValue();
                this.currentBidPlayerName = model.getCurrentBidPlayer().getName();
                break;
            case GAME_END:
                this.isMaputo = model.isMaputo();
                this.isGameStarted = model.isGameStarted();
                this.currentTurnPlayerName = model.getCurrentTurnPlayer().getName();
                this.totalDicesCount = model.getTotalDicesCount();
                this.currentBidQuantity = model.getCurrentBidQuantity();
                this.currentBidValue = model.getCurrentBidValue();
                this.currentBidPlayerName = model.getCurrentBidPlayer().getName();
                this.players = new ArrayList<>(model.getPlayers());
                if (model.getDeletedPlayer() != null) {
                    players.add(model.getDeletedPlayer());
                }
                break;
            case IS_MAPUTO:
                this.isMaputo = model.isMaputo();
                this.isGameStarted = model.isGameStarted();
                this.currentTurnPlayerName = model.getCurrentTurnPlayer().getName();
                this.totalDicesCount = model.getTotalDicesCount();
                this.currentBidQuantity = model.getCurrentBidQuantity();
                this.currentBidValue = model.getCurrentBidValue();
                this.currentBidPlayerName = model.getCurrentBidPlayer().getName();
                this.players = new ArrayList<>(model.getPlayers());
                if (model.getDeletedPlayer() != null) {
                    players.add(model.getDeletedPlayer());
                }
                break;
            case ROUND_RESULT:
                this.isMaputo = model.isMaputo();
                this.isGameStarted = model.isGameStarted();
                this.currentTurnPlayerName = model.getCurrentTurnPlayer().getName();
                this.totalDicesCount = model.getTotalDicesCount();
                this.currentBidQuantity = model.getCurrentBidQuantity();
                this.currentBidValue = model.getCurrentBidValue();
                this.currentBidPlayerName = model.getCurrentBidPlayer().getName();
                this.players = new ArrayList<>(model.getPlayers());
                if (model.getDeletedPlayer() != null) {
                    players.add(model.getDeletedPlayer());
                }
                break;
            case TURN_ACCEPTED:
                this.isMaputo = model.isMaputo();
                this.isGameStarted = model.isGameStarted();
                this.currentTurnPlayerName = model.getCurrentTurnPlayer().getName();
                this.totalDicesCount = model.getTotalDicesCount();
                this.currentBidQuantity = model.getCurrentBidQuantity();
                this.currentBidValue = model.getCurrentBidValue();
                this.currentBidPlayerName = model.getCurrentBidPlayer().getName();
                break;
        }
    }

    public PerudoServerResponse(PerudoModel model, LinkedList<ChatMessage> chatMessages, PerudoServerResponseEnum responseEnum, int[] dices) {
        this.responseEnum = responseEnum;
        this.dices = dices;
        switch (responseEnum) {
            case JOINED_PARTY:
                this.isMaputo = model.isMaputo();
                this.isGameStarted = model.isGameStarted();
                this.currentTurnPlayerName = model.getCurrentTurnPlayer().getName();
                this.totalDicesCount = model.getTotalDicesCount();
                this.currentBidQuantity = model.getCurrentBidQuantity();
                this.currentBidValue = model.getCurrentBidValue();
                this.currentBidPlayerName = model.getCurrentBidPlayer().getName();
                this.chatMessages = chatMessages;
                break;
        }
    }

    public LinkedList<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(LinkedList<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public int getTotalDicesCount() {
        return totalDicesCount;
    }

    public int getCurrentBidQuantity() {
        return currentBidQuantity;
    }

    public int getCurrentBidValue() {
        return currentBidValue;
    }

    public String getCurrentBidPlayerName() {
        return currentBidPlayerName;
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public int[] getDices() {
        return dices;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public PerudoServerResponseEnum getResponseEnum() {
        return responseEnum;
    }

    public String getCurrentTurnPlayerName() {
        return currentTurnPlayerName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<PartyHeader> getParties() {
        return parties;
    }

    public void setParties(ArrayList<PartyHeader> parties) {
        this.parties = parties;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PerudoServerResponse{");
        sb.append("responseEnum=").append(responseEnum);
        sb.append(", totalDicesCount=").append(totalDicesCount);
        sb.append(", currentBidQuantity=").append(currentBidQuantity);
        sb.append(", currentBidValue=").append(currentBidValue);
        sb.append(", currentTurnPlayerName='").append(currentTurnPlayerName).append('\'');
        sb.append(", currentBidPlayerName='").append(currentBidPlayerName).append('\'');
        sb.append(", isMaputo=").append(isMaputo);
        sb.append(", isGameStarted=").append(isGameStarted);
        sb.append(", dices=").append(Arrays.toString(dices));
        sb.append(", players=").append(players);
        sb.append(", parties=").append(parties);
        sb.append(", message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
