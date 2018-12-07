package com.suai.perudo.web;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.util.Pair;

import java.io.Serializable;

/**
 * Created by dmitry on 04.11.18.
 */

public class PerudoClientCommand implements Serializable{
    private boolean isMaputo = false;
    private boolean isBid = false;
    private boolean isLeave = false;
    private boolean isDoubt = false;
    private boolean isJoin = false;
    private boolean isGetParties = false;
    private boolean isNewParty = false;

    private int currentBidQuantity = 0;
    private int currentBidValue = 0;
    private Party party = null;


    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum){
        switch (perudoClientCommandEnum){
            case BID:
                isBid = true;
                break;
            case DOUBT:
                isDoubt = true;
                break;
            case LEAVE:
                isLeave = true;
                break;
            case MAPUTO:
                isMaputo = true;
                break;
            case JOIN:
                isJoin = true;
                break;
            case GET_PARTIES:
                isGetParties = true;
                break;
            case NEW_PARTY:
                isNewParty = true;
                break;
        }
    }

    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum, Party party){
        switch (perudoClientCommandEnum){
            case BID:
                isBid = true;
                break;
            case DOUBT:
                isDoubt = true;
                break;
            case LEAVE:
                isLeave = true;
                break;
            case MAPUTO:
                isMaputo = true;
                break;
            case JOIN:
                isJoin = true;
                this.party = party;
                break;
            case GET_PARTIES:
                isGetParties = true;
                break;
        }
    }

    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum, int currentBidQuantity, int currentBidValue){
        switch (perudoClientCommandEnum){
            case BID:
                isBid = true;
                this.currentBidQuantity = currentBidQuantity;
                this.currentBidValue = currentBidValue;
                break;
            case DOUBT:
                isDoubt = true;
                break;
            case LEAVE:
                isLeave = true;
                break;
            case MAPUTO:
                isMaputo = true;
                break;
            case JOIN:
                isJoin = true;
                break;
            case GET_PARTIES:
                isGetParties = true;
                break;
        }
    }

    public boolean isMaputo() {
        return isMaputo;
    }

    public boolean isBid() {
        return isBid;
    }

    public boolean isLeave() {
        return isLeave;
    }

    public boolean isDoubt() {
        return isDoubt;
    }

    public boolean isJoin() {
        return isJoin;
    }

    public Party getParty() {
        return party;
    }

    public boolean isGetParties() {
        return isGetParties;
    }

    public boolean isNewParty() {
        return isNewParty;
    }

    public boolean isGameCommand() {
        return isBid | isDoubt | isLeave | isMaputo;
    }

    public Pair getBid() {
        return new Pair(currentBidQuantity, currentBidValue);
    }

    public String toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

}
