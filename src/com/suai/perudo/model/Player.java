package com.suai.perudo.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by dmitry on 09.09.18.
 */

public class Player implements Serializable{

    @Expose private String name;

    @Expose private int numberOfDices;
    @Expose private int[] dices = new int[6];
    @Expose private int[] previousDices = new int[6];

    public Player(String name) {
        this.name = name;
    }

    public int[] getDices() {
        return dices;
    }

    public void setDices(int[] dices) {
        this.dices = dices;
    }

    public int[] getPreviousDices() {
        return previousDices;
    }

    public void setPreviousDices(int[] previousDices) {
        this.previousDices = previousDices;
    }

    public int getNumberOfDices() {
        return numberOfDices;
    }

    public void setNumberOfDices(int numberOfDices) {
        this.numberOfDices = numberOfDices;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (this.getClass() != obj.getClass())
            return false;
        return this.getName().equals(((Player) obj).getName());
    }
}
