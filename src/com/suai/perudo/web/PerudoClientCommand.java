package com.suai.perudo.web;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.util.Pair;

import java.io.Serializable;

/**
 * Created by dmitry on 04.11.18.
 */

public class PerudoClientCommand implements Serializable{
    private PerudoClientCommandEnum commandEnum;

    private int currentBidQuantity = 0;
    private int currentBidValue = 0;
    private PartyHeader partyHeader = null;
    private String login;
    private String password;

    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum){
        this.commandEnum = perudoClientCommandEnum;
    }

    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum, PartyHeader partyHeader){
        this.commandEnum = perudoClientCommandEnum;
        this.partyHeader = partyHeader;
    }

    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum, String login, String password){
        this.commandEnum = perudoClientCommandEnum;
        this.login = login;
        this.password = password;
    }

    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum, int currentBidQuantity, int currentBidValue){
        this.commandEnum = perudoClientCommandEnum;
        this.currentBidQuantity = currentBidQuantity;
        this.currentBidValue = currentBidValue;
    }

    public PerudoClientCommandEnum getCommand() {
        return commandEnum;
    }

    public boolean isGameCommand(){
        return (commandEnum == PerudoClientCommandEnum.BID) || (commandEnum == PerudoClientCommandEnum.DOUBT) ||
                (commandEnum == PerudoClientCommandEnum.LEAVE) || (commandEnum == PerudoClientCommandEnum.MAPUTO) ||
                (commandEnum == PerudoClientCommandEnum.START_GAME);
    }

    public PartyHeader getPartyHeader() {
        return partyHeader;
    }

    public Pair getBid() {
        return new Pair(currentBidQuantity, currentBidValue);
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

}
