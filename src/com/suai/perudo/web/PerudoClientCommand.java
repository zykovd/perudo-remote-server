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

//    private boolean isMaputo = false;
//    private boolean isBid = false;
//    private boolean isLeave = false;
//    private boolean isDoubt = false;
//    private boolean isJoin = false;
//    private boolean isGetParties = false;
//    private boolean isNewParty = false;
//    private boolean isDisconnect = false;

    private int currentBidQuantity = 0;
    private int currentBidValue = 0;
    private Party party = null;
    private String login;
    private String password;




    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum){
        this.commandEnum = perudoClientCommandEnum;
    }

    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum, Party party){
        this.commandEnum = perudoClientCommandEnum;
        this.party = party;
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

    public Party getParty() {
        return party;
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
