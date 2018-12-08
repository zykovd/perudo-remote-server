package com.suai.perudo.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suai.perudo.data.DataIO;
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

public class PerudoRemoteServer extends Thread{

    private long id;

    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.create();

    private DataIO dataIO;

    private ArrayList<Party> parties = new ArrayList<>();
    private HashMap<WebUser, Player> clients = new HashMap<>();

    private ServerSocket serverSocket;
    private int port;



    public PerudoRemoteServer(int port) {
        this.port = port;
        System.out.println("PerudoRemoteServer.PerudoRemoteServer");
        System.out.println("port = " + port);
        try {
            this.dataIO = new DataIO();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            this.serverSocket = new ServerSocket(port);
            String address = serverSocket.getInetAddress().getCanonicalHostName();
            System.out.println("address = " + address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
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
        finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startGame(Party party) {
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
        System.out.println("PerudoRemoteServer.startGame " + party.getId());
    }

    private void joinParty(WebUser webUser, Party party) {
        party.addPlayer(webUser, clients.get(webUser));
        System.out.println("PerudoRemoteServer.joinParty " + party.getId());
    }

    synchronized private boolean tryProceedGameCommand(PerudoClientCommand perudoClientCommand, WebUser webUser) {
        Player player = clients.get(webUser);
        Party party = webUser.getCurrentParty();
        if (perudoClientCommand == null) {
            return false;
        }
        else {
            PerudoClientCommandEnum command = perudoClientCommand.getCommand();
            switch (command){
                case BID:
                    Pair bid = perudoClientCommand.getBid();
                    if (party.getModel().tryMakeBid(player, (int)bid.getKey(), (int)bid.getValue()))
                        return true;
                    else
                        return false;
                case DOUBT:
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
                case LEAVE:
                    try {
                        webUser.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //todo disconnect
                    return true;
                case MAPUTO:
                    party.getModel().setMaputo(true);
                    return true;
                case START_GAME:
                    startGame(party);
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
            boolean stateChanged = tryProceedGameCommand(perudoClientCommand, webUser);
            if (stateChanged) {
                resendChangesToClients(party);
            }
            else {
                PerudoServerResponse response = new PerudoServerResponse(party.getModel(), PerudoServerResponseEnum.INVALID_BID, clients.get(webUser).getDices());
                webUser.getDataOutputStream().writeUTF(response.toJson());
            }
        }
        else {
            PerudoClientCommandEnum command = perudoClientCommand.getCommand();
            switch (command) {
                case GET_PARTIES:
                    PerudoServerResponse response = new PerudoServerResponse(PerudoServerResponseEnum.PARTIES_LIST, parties);
                    webUser.getDataOutputStream().writeUTF(response.toJson());
                    break;
                case JOIN:
                    Party partyForJoin = perudoClientCommand.getParty();
                    joinParty(webUser, partyForJoin);
                    webUser.setCurrentParty(partyForJoin);
                    PerudoServerResponse joinResponse = new PerudoServerResponse(PerudoServerResponseEnum.JOINED_PARTY);
                    webUser.getDataOutputStream().writeUTF(joinResponse.toJson());
                    break;
                case NEW_PARTY:
                    Party party = new Party(id++);
                    party.addPlayer(webUser, clients.get(webUser));
                    PerudoServerResponse newPartyResponse = new PerudoServerResponse(PerudoServerResponseEnum.JOINED_PARTY);
                    webUser.getDataOutputStream().writeUTF(newPartyResponse.toJson());
                    break;
                case DISCONNECT:
                    //todo disconnect
                    break;
                case LOGIN:
                    boolean rightData = dataIO.checkPassword(perudoClientCommand.getLogin(), perudoClientCommand.getPassword());
                    if (rightData) {
                        PerudoServerResponse loggedResponse = new PerudoServerResponse(PerudoServerResponseEnum.AUTH_SUCCESS);
                        webUser.getDataOutputStream().writeUTF(loggedResponse.toJson());
                    }
                    else {
                        PerudoServerResponse loggedResponse = new PerudoServerResponse(PerudoServerResponseEnum.AUTH_ERROR);
                        webUser.getDataOutputStream().writeUTF(loggedResponse.toJson());
                    }
                    break;
                case REGISTER:
                    boolean rightReg = dataIO.addUser(perudoClientCommand.getLogin(), perudoClientCommand.getPassword());
                    PerudoServerResponse loggedResponse;
                    if (rightReg) {
                        loggedResponse = new PerudoServerResponse(PerudoServerResponseEnum.REG_SUCCESS);
                    }
                    else {
                        loggedResponse = new PerudoServerResponse(PerudoServerResponseEnum.REG_ERROR);
                    }
                    webUser.getDataOutputStream().writeUTF(loggedResponse.toJson());
                    break;
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
