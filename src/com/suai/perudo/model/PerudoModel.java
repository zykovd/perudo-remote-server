package com.suai.perudo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by dmitry on 09.09.18.
 */

public class PerudoModel implements Serializable {

    private ArrayList<Player> players = new ArrayList<>();
//    private PerudoQueue queue = new PerudoQueue();

    private int totalDicesCount; //Бесполезная статистика
    private int initDicesPerPlayer = 5;
    private Random random = new Random(System.currentTimeMillis());

    private int currentBidQuantity;
    private int currentBidValue;
    private Player currentBidPlayer;

    private boolean isMaputo = false;
    private int currentTurn = 0;

    private boolean isGameStarted = false;
    private boolean isGameEnded = false;

    private String doubtMessage;

    public PerudoModel() {
    }

    public PerudoModel(ArrayList<Player> players, int initDicesPerPlayer) {
        this.players = players;
        this.initDicesPerPlayer = initDicesPerPlayer;
        this.currentTurn = 0;
        this.currentBidPlayer = new Player("First turn");
        this.currentBidQuantity = 0;
        this.currentBidValue = 0;
        for (Player player : players) {
            player.setNumberOfDices(initDicesPerPlayer);
        }
    }

    public void refreshDices() {
        totalDicesCount = 0;
        for (Player player : players) {
            int[] dices = player.getDices();
            for (int i = 0; i < 6; ++i)
                dices[i] = 0;
            for (int i = 0; i < player.getNumberOfDices(); ++i) {
                int randDiceValue = random.nextInt(5);
                dices[randDiceValue]++;
            }
            player.setDices(dices);
            totalDicesCount += player.getNumberOfDices();
        }
        currentBidValue = 0;
        currentBidQuantity = 0;
    }

    public boolean isPlayersTurn(Player player) {
        if (players.indexOf(player) == currentTurn)
            return true;
        else
            return false;
    }

    private void forwardTurnTransition() {
        //System.out.println("players = " + players);
        currentTurn++;
        if (currentTurn == players.size())
            currentTurn = 0;
    }

    private void backwardTurnTransition() {
        currentTurn--;
        if (currentTurn < 0)
            currentTurn = players.size() - 1;
    }

    public Player getCurrentTurnPlayer() {
        if (players.size() != 0)
            return players.get(currentTurn);
        else
            return null;
    }

    public boolean tryMakeBid(Player player, int quantity, int value) {
        if (!isMaputo) {
            if (value == 1) {
                if (currentBidValue == 1) {
                    if (quantity > currentBidQuantity) {
                        currentBidQuantity = quantity;
                        currentBidPlayer = player;
                        forwardTurnTransition();
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    if (quantity > (currentBidQuantity / 2)) {
                        currentBidQuantity = quantity;
                        currentBidValue = value;
                        currentBidPlayer = player;
                        forwardTurnTransition();
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                if (currentBidValue == 1) {
                    if (quantity >= currentBidQuantity * 2) {
                        currentBidQuantity = quantity;
                        currentBidValue = value;
                        currentBidPlayer = player;
                        forwardTurnTransition();
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    if (quantity > currentBidQuantity) {
                        currentBidQuantity = quantity;
                        currentBidValue = value;
                        currentBidPlayer = player;
                        forwardTurnTransition();
                        return true;
                    }
                    if (quantity == currentBidQuantity && value > currentBidValue) {
                        currentBidQuantity = quantity;
                        currentBidValue = value;
                        currentBidPlayer = player;
                        forwardTurnTransition();
                        return true;
                    }
                    return false;
                }
            }
        } else {
            if (value != currentBidValue) {
                return false;
            } else {
                if (quantity > currentBidQuantity) {
                    currentBidQuantity = quantity;
                    currentBidValue = value;
                    currentBidPlayer = player;
                    forwardTurnTransition();
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public boolean doubt(Player player) {
        boolean isPlayerRight;
        int[] totalDices = new int[6];
        for (Player p : players) {
            for (int i = 0; i < 6; ++i) {
                totalDices[i] += p.getDices()[i];
            }
            p.setPreviousDices(Arrays.copyOf(p.getDices(), p.getDices().length));
        }
        if (isPreviousBidWon(totalDices)) {
            isPlayerRight = false;
            player.setNumberOfDices(player.getNumberOfDices() - 1);
            if (player.getNumberOfDices() == 0) {
                players.remove(player);
            }
        } else {
            isPlayerRight = true;
            currentBidPlayer.setNumberOfDices(currentBidPlayer.getNumberOfDices() - 1);
            if (currentBidPlayer.getNumberOfDices() == 0) {
                players.remove(currentBidPlayer);
            }
            backwardTurnTransition();
        }
        refreshDices();
        return isPlayerRight;
    }

    private boolean isPreviousBidWon(int[] dices) {
        if (currentBidValue == 1) {
            doubtMessage = "There were " + String.valueOf(dices[0]) + " " + getDiceValueString();
            if (currentBidQuantity > dices[0]) {
                return false;
            } else {
                return true;
            }
        } else {
            doubtMessage = "There were " + String.valueOf(dices[0]+ dices[currentBidValue - 1]) + " " + getDiceValueString();
            if (currentBidQuantity > dices[0] + dices[currentBidValue - 1]) {
                return false;
            } else {
                return true;
            }
        }
    }

    private String getDiceValueString() {
        switch (currentBidValue) {
            case 1:
                return "one(s)";
            case 2:
                return "two(s)";
            case 3:
                return "three(s)";
            case 4:
                return "four(s)";
            case 5:
                return "five(s)";
            case 6:
                return "six(s)";
            default:
                return "";
        }

    }

    public void removePlayer(Player player) {
        if (players.contains(player))
            players.remove(player);
    }

    public String getDoubtMessage() {
        return doubtMessage;
    }

    public int getTotalDicesCount() {
        return totalDicesCount;
    }

    public void setTotalDicesCount(int totalDicesCount) {
        this.totalDicesCount = totalDicesCount;
    }

    public boolean isMaputo() {
        return isMaputo;
    }

    public void setMaputo(boolean maputo) {
        isMaputo = maputo;
    }

    public int getCurrentBidQuantity() {
        return currentBidQuantity;
    }

    public void setCurrentBidQuantity(int currentBidQuantity) {
        this.currentBidQuantity = currentBidQuantity;
    }

    public int getCurrentBidValue() {
        return currentBidValue;
    }

    public void setCurrentBidValue(int currentBidValue) {
        this.currentBidValue = currentBidValue;
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        isGameStarted = gameStarted;
    }

    public Player getCurrentBidPlayer() {
        return currentBidPlayer;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public int getInitDicesPerPlayer() {
        return initDicesPerPlayer;
    }

    public void setInitDicesPerPlayer(int initDicesPerPlayer) {
        this.initDicesPerPlayer = initDicesPerPlayer;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public void setCurrentBidPlayer(Player currentBidPlayer) {
        this.currentBidPlayer = currentBidPlayer;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    public boolean isGameEnded() {
        return isGameEnded;
    }

    public void setGameEnded(boolean gameEnded) {
        isGameEnded = gameEnded;
    }
}
