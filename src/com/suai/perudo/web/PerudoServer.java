package com.suai.perudo.web;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suai.perudo.model.PerudoModel;
import com.suai.perudo.model.Player;
import javafx.util.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PerudoServer extends Thread{

    private long id;

    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.create();

    private ArrayList<Party> parties = new ArrayList<>();
    private HashMap<WebUser, Player> clients = new HashMap<>();
    /*private PerudoModel model;
    private String message;
    private boolean newTurn = false;
    private HashMap<WebUser, Player> players = new HashMap<>();*/

    private ServerSocket serverSocket;
    private int port;



    public PerudoServer(int port) throws IOException, ClassNotFoundException {
        this.port = port;
    }

    @Override
    public void run() {
        super.run();
        try {
            this.serverSocket = new ServerSocket(port);
            String address = serverSocket.getLocalSocketAddress().toString();
            System.out.println(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();

                WebUser webUser = new WebUser(clientSocket);
                DataInputStream dataInputStream = webUser.getDataInputStream();
                Player player = gson.fromJson(dataInputStream.readUTF(), Player.class);

                clients.put(webUser, player);

                DataOutputStream dataOutputStream = webUser.getDataOutputStream();
                dataOutputStream.writeUTF(new PerudoServerResponse(PerudoServerResponseEnum.CONNECTED).toJson());

                new PerudoServerThread(webUser).start();
                System.out.println("Connected player = " + player);
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void startGame(Party party) {
        ArrayList<Player> playersList = new ArrayList<>();
        for (Map.Entry<WebUser, Player> entry: party.getPlayers().entrySet()) {
            playersList.add(entry.getValue());
        }
        party.setModel(new PerudoModel(playersList, 6));
        party.getModel().setGameStarted(true);
        party.getModel().refreshDices();
        new Thread(() -> {
            try {
                resendChangesToClients(party);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void joinParty(WebUser webUser, Party party) {
        party.addPlayer(webUser, clients.get(webUser));
    }

    synchronized private boolean tryProceedGameCommand(PerudoClientCommand perudoClientCommand, WebUser webUser) {
        Player player = clients.get(webUser);
        Party party = webUser.getCurrentParty();
        if (perudoClientCommand == null) {
            return false;
        }
        else {
            if (perudoClientCommand.isBid()) {
                Pair bid = perudoClientCommand.getBid();
                if (party.getModel().tryMakeBid(player, (int)bid.getKey(), (int)bid.getValue()))
                    return true;
                else
                    return false;
            }
            else if (perudoClientCommand.isDoubt()) {
                String loser;
                if (party.getModel().doubt(player)) {
                    loser = party.getModel().getCurrentBidPlayer().getName();
                }
                else {
                    loser = player.getName();
                }
                party.setMessage(loser + " loosing one dice!");
                if (party.getModel().getPlayers().size() == 1) {
                    party.setMessage(party.getMessage() + "\n" + party.getModel().getPlayers().get(0).getName()+" is the winner!");
                }
                party.setNewTurn(true);
                return true;
            }
            else if (perudoClientCommand.isLeave()) {
                //TODO leave
                return true;
            }
            else if (perudoClientCommand.isMaputo()) {
                party.getModel().setMaputo(true);
                return true;
            }
        }
        return false;
    }

    synchronized private void proceedCommand(WebUser webUser, PerudoClientCommand perudoClientCommand) throws IOException {
        if (perudoClientCommand.isGameCommand()) {
            Party party = webUser.getCurrentParty();
            if (!party.getModel().isPlayersTurn(clients.get(webUser))) {
                PerudoServerResponse response = new PerudoServerResponse(party.getModel(), PerudoServerResponseEnum.WRONG_TURN, clients.get(webUser).getDices());
                webUser.getDataOutputStream().writeUTF(response.toJson());
                return;
            }
            boolean stateChanged = tryProceedGameCommand(perudoClientCommand, webUser); //todo wrong_turn etc.
            if (stateChanged) {
                resendChangesToClients(party);
            }
            else {
                PerudoServerResponse response = new PerudoServerResponse(party.getModel(), PerudoServerResponseEnum.INVALID_BID, clients.get(webUser).getDices());
                webUser.getDataOutputStream().writeUTF(response.toJson());
            }
        }
        else {
            if (perudoClientCommand.isGetParties()) {
                PerudoServerResponse response = new PerudoServerResponse(PerudoServerResponseEnum.PARTIES_LIST, parties);
                webUser.getDataOutputStream().writeUTF(response.toJson());
            }
            if (perudoClientCommand.isJoin()) {
                Party partyForJoin = perudoClientCommand.getParty();
                joinParty(webUser, partyForJoin);
                webUser.setCurrentParty(partyForJoin);
                PerudoServerResponse response = new PerudoServerResponse(PerudoServerResponseEnum.JOINED_PARTY);
                webUser.getDataOutputStream().writeUTF(response.toJson());
            }
            if (perudoClientCommand.isNewParty()) {
                Party party = new Party(id++);
                party.addPlayer(webUser, clients.get(webUser));
                PerudoServerResponse response = new PerudoServerResponse(PerudoServerResponseEnum.JOINED_PARTY);
                webUser.getDataOutputStream().writeUTF(response.toJson());
            }
        }
    }


    private void resendChangesToClients(Party party) throws IOException {
        for (WebUser webUser: party.getPlayers().keySet()) {
            DataOutputStream dataOutputStream = webUser.getDataOutputStream();
            PerudoServerResponse response;
            if (party.isNewTurn()) {
                response = new PerudoServerResponse(party.getModel(), PerudoServerResponseEnum.ROUND_RESULT, party.getPlayers().get(webUser).getDices());
                response.setMessage(party.getMessage());
            }
            else {
                response = new PerudoServerResponse(party.getModel(), PerudoServerResponseEnum.TURN_ACCEPTED, party.getPlayers().get(webUser).getDices());
            }
            dataOutputStream.writeUTF(response.toJson());
        }
    }

    private class PerudoServerThread extends Thread {

        private WebUser webUser;

        public PerudoServerThread(WebUser webUser) {
            this.webUser = webUser;
        }

        @Override
        public void run() {
            DataInputStream dataInputStream = webUser.getDataInputStream();
            PerudoClientCommand perudoClientCommand;
            while (true) {
                try {
                    perudoClientCommand = gson.fromJson(dataInputStream.readUTF(), PerudoClientCommand.class);
                    if (perudoClientCommand != null) {
                        proceedCommand(webUser, perudoClientCommand);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
