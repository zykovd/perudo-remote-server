package com.suai.perudo.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suai.perudo.data.DataIO;
import com.suai.perudo.model.PerudoModel;
import com.suai.perudo.model.Player;
import javafx.util.Pair;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

public class PerudoRemoteServer extends Thread {

    private long id;

    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.create();

    private DataIO dataIO;

    private ArrayList<Party> parties = new ArrayList<>();
    //private HashMap<WebUser, Player> clients = new HashMap<>();

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


        //TODO Remove this tests
        /*try {
            parties = dataIO.loadPartys();
            id = parties.size();
            System.out.println("parties = " + parties);
            for (Party party: parties) {
                System.out.println(party.getId() + " " + party.getTitle());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        /*try {
            Party party = new Party(id++);
            startGame(party);
            dataIO.addParty(party);
            parties.add(party);
            //dataIO.removeParty(party);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Party party2 = new Party(id++);
            startGame(party2);
            dataIO.addParty(party2);
            parties.add(party2);
        } catch (IOException e) {
            e.printStackTrace();
        }*/


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
                    //DataInputStream dataInputStream = webUser.getDataInputStream();
                    //Player player = gson.fromJson(dataInputStream.readUTF(), Player.class);
                    //DataOutputStream dataOutputStream = webUser.getDataOutputStream();
                    //dataOutputStream.writeUTF(new PerudoServerResponse(PerudoServerResponseEnum.CONNECTED).toJson());

                    new PerudoServerThread(webUser).start();
                    //System.out.println("Connected player = " + player);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startGame(Party party) {
        ArrayList<Player> playersList = new ArrayList<>();
        for (Map.Entry<String, Player> entry : party.getPlayers().entrySet()) {
            playersList.add(entry.getValue());
        }
        for (int i = 0; i < party.getNumberOfBots(); ++i) {
            playersList.add(new Player("Bot" + String.valueOf(i), true));
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
        if (!party.contains(webUser)) {
            party.addPlayer(webUser);
        } else {
//            Player player = party.getPlayers().get(webUser);
//            party.getPlayers().remove(webUser, player);
//            party.getPlayers().put(webUser, player);
            party.getWebUsers().remove(webUser);
            party.getWebUsers().add(webUser);
        }
        System.out.println("PerudoRemoteServer.joinParty " + party.getId());
    }

    synchronized private boolean tryProceedGameCommand(PerudoClientCommand perudoClientCommand, WebUser webUser) {
        Player player = null;
        Party party = webUser.getCurrentParty();
        if (party != null) {
            player = webUser.getCurrentParty().getPlayers().get(webUser.getLogin());
        }
        if (party == null || perudoClientCommand == null) {
            return false;
        } else {
            PerudoClientCommandEnum command = perudoClientCommand.getCommand();
            switch (command) {
                case BID:
                    Pair bid = perudoClientCommand.getBid();
                    if (party.getModel().tryMakeBid(player, (int) bid.getKey(), (int) bid.getValue()))
                        return true;
                    else
                        return false;
                case CHAT_MESSAGE:
                    party.addChatMessage(new ChatMessage(webUser.getLogin(), perudoClientCommand.getMessage()));
                    party.setNewChatMessage(true);
                    return true;
                case DOUBT:
                    String loser;
                    if (party.getModel().doubt(player)) {
                        loser = party.getModel().getCurrentBidPlayer().getName();
                    } else {
                        loser = player.getName();
                    }
                    party.setMessage(loser + " loosing one dice!\n" + party.getModel().getDoubtMessage());
                    party.setLoser(loser);
                    if (party.getModel().getPlayers().size() == 1) {
                        party.setMessage(party.getMessage() + "\n" + party.getModel().getPlayers().get(0).getName() + " is the winner!");
                        party.getModel().setGameEnded(true);
                        parties.remove(party);
                    }
                    party.setNewTurn(true);
                    return true;
                case LEAVE:
                    party.setNewTurn(true);
                    party.setMessage(player.getName() + " left the game!");
                    party.removePlayer(webUser, player);
                    party.getModel().setCurrentTurn(0);//TODO Check
                    if (party.getModel().getPlayers().size() == 1) {
                        party.getModel().setGameEnded(true);
                        party.setMessage(party.getMessage() + "\n" + party.getModel().getPlayers().get(0).getName() + " is the winner!");
                    }
                    PerudoServerResponse response = new PerudoServerResponse(PerudoServerResponseEnum.LEFT_GAME);
                    sendResponse(webUser, response);
                    webUser.setCurrentParty(null);
                    //todo disconnect
                    return true;
                case MAPUTO:
                    party.getModel().setMaputo(true);
                    party.setMessage("Maputo round!");
                    return true;
                case NOT_MAPUTO:
                    party.getModel().setMaputo(false);
                    party.setMessage("Ordinary round!");
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
            if (party != null && party.getModel().isGameEnded()) {
                PerudoServerResponse response = new PerudoServerResponse(party.getModel(), PerudoServerResponseEnum.GAME_END, party.getPlayers().get(webUser.getLogin()).getDices());
                response.setMessage(party.getMessage());
                sendResponse(webUser, response);
                return;
            }
            //Player player222 = party.getPlayers().get(webUser);
            if (!party.getModel().isPlayersTurn(party.getPlayers().get(webUser.getLogin())) && perudoClientCommand.isTurnCommand()) {
                PerudoServerResponse response = new PerudoServerResponse(party.getModel(), PerudoServerResponseEnum.WRONG_TURN, party.getPlayers().get(webUser.getLogin()).getDices());
                sendResponse(webUser, response);
                return;
            }
            boolean stateChanged = tryProceedGameCommand(perudoClientCommand, webUser);
            if (stateChanged) {
                dataIO.refreshParty(party);
                resendChangesToClients(party);
                party.setNewChatMessage(false);
                party.setNewTurn(false);
                party.getModel().setDeletedPlayer(null);
                if (!party.getModel().isGameEnded()) {
                    try {
                        while (cpuTurn(party) && !party.getModel().isGameEnded()) {
                            Thread.sleep(2000);
                            resendChangesToClients(party);
                            dataIO.refreshParty(party);
                            party.setNewChatMessage(false);
                            party.setNewTurn(false);
                            party.getModel().setDeletedPlayer(null);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                PerudoServerResponse response = new PerudoServerResponse(party.getModel(), PerudoServerResponseEnum.INVALID_BID, party.getPlayers().get(webUser.getLogin()).getDices());
                sendResponse(webUser, response);
            }
        } else {
            PerudoClientCommandEnum command = perudoClientCommand.getCommand();
            switch (command) {
                case GET_PARTIES:
                    ArrayList<PartyHeader> partyHeaders = new ArrayList<>();
                    for (Party p : parties) {
                        partyHeaders.add(p.getPartyHeader());
                    }
                    PerudoServerResponse response = new PerudoServerResponse(PerudoServerResponseEnum.PARTIES_LIST, partyHeaders);
                    System.out.println(response.toJson());
                    sendResponse(webUser, response);
                    break;
                case JOIN:
                    PartyHeader partyForJoin = perudoClientCommand.getPartyHeader();
                    Party party = findPartyByHeader(partyForJoin);
                    if (party != null) {
                        joinParty(webUser, party);
                        webUser.setCurrentParty(party);
                        PerudoServerResponse joinHeader = new PerudoServerResponse(PerudoServerResponseEnum.JOINED_PARTY);
                        PerudoServerResponse joinResponse = new PerudoServerResponse(party.getModel(), party.getChatMessages(), PerudoServerResponseEnum.JOINED_PARTY, party.getPlayers().get(webUser.getLogin()).getDices());
                        sendResponse(webUser, joinHeader);
                        sendResponse(webUser, joinResponse);
                    } else {
                        PerudoServerResponse joinResponse = new PerudoServerResponse(PerudoServerResponseEnum.JOIN_ERROR);
                        sendResponse(webUser, joinResponse);
                    }
                    break;
                case NEW_PARTY:
                    Party newParty = new Party(id++, perudoClientCommand.getMessage());
                    newParty.setNumberOfBots(perudoClientCommand.getNumber());
                    newParty.addPlayer(webUser);
                    webUser.setCurrentParty(newParty);
                    dataIO.addParty(newParty);
                    parties.add(newParty);
                    PerudoServerResponse newPartyResponse = new PerudoServerResponse(PerudoServerResponseEnum.JOINED_PARTY);
                    sendResponse(webUser, newPartyResponse);
                    new DelayedGameStartThread(newParty, 10000).start();
                    break;
                case RETURN:
                    webUser.setCurrentParty(null);
                    break;
                case DISCONNECT:
                    webUser.disconnect();
                    break;
                case LOGIN:
                    boolean rightData = dataIO.checkPassword(perudoClientCommand.getLogin(), perudoClientCommand.getPassword());
                    if (rightData) {
                        PerudoServerResponse loggedResponse = new PerudoServerResponse(PerudoServerResponseEnum.AUTH_SUCCESS);
                        sendResponse(webUser, loggedResponse);
                        webUser.setLogin(perudoClientCommand.getLogin());
                        Player player = new Player(perudoClientCommand.getLogin());
                        System.out.println("player.getName() = " + player.getName());
                        //clients.put(webUser, player);
                    } else {
                        PerudoServerResponse loggedResponse = new PerudoServerResponse(PerudoServerResponseEnum.AUTH_ERROR);
                        sendResponse(webUser, loggedResponse);
                    }
                    break;
                case REGISTER:
                    boolean rightReg = dataIO.addUser(perudoClientCommand.getLogin(), perudoClientCommand.getPassword());
                    PerudoServerResponse loggedResponse;
                    if (rightReg) {
                        loggedResponse = new PerudoServerResponse(PerudoServerResponseEnum.REG_SUCCESS);
                        Player player = new Player(perudoClientCommand.getLogin());
                        System.out.println("player.getName() = " + player.getName());
                        //clients.put(webUser, player);
                    } else {
                        loggedResponse = new PerudoServerResponse(PerudoServerResponseEnum.REG_ERROR);
                    }
                    sendResponse(webUser, loggedResponse);
                    break;
            }
        }
    }

    private Party findPartyByHeader(PartyHeader party) {
        for (Party p : parties) {
            if (p.getId() == party.getId()) {
                return p;
            }
        }
        return null;
    }

    public void sendResponse(WebUser webUser, PerudoServerResponse response) {
        if (webUser.isConnected()) {
            try {
                webUser.getDataOutputStream().writeUTF(response.toJson());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void resendChangesToClients(Party party) throws IOException {
        for (WebUser webUser : party.getWebUsers()) {
            if (webUser.isConnected()) {
                PerudoServerResponse response;
                if (party.isNewTurn()) {
                    if (party.getLoser().equals(webUser.getLogin()) && party.getPlayers().get(webUser.getLogin()).getNumberOfDices() == 1) {
                        response = new PerudoServerResponse(party.getModel(), PerudoServerResponseEnum.IS_MAPUTO, party.getPlayers().get(webUser.getLogin()).getDices());
                        response.setMessage(party.getMessage());
                    } else {
                        response = new PerudoServerResponse(party.getModel(), PerudoServerResponseEnum.ROUND_RESULT, party.getPlayers().get(webUser.getLogin()).getDices());
                        response.setMessage(party.getMessage());
                    }
                } else {
                    if (party.isNewChatMessage()) {
                        response = new PerudoServerResponse(PerudoServerResponseEnum.NEW_CHAT_MESSAGE, party.getChatMessages().getLast());
                    }
                    else {
                        response = new PerudoServerResponse(party.getModel(), PerudoServerResponseEnum.TURN_ACCEPTED, party.getPlayers().get(webUser.getLogin()).getDices());
                    }

                }
                sendResponse(webUser, response);
//            if (party.getModel().isGameEnded()) {
//                response = new PerudoServerResponse(PerudoServerResponseEnum.GAME_END);
//                dataOutputStream.writeUTF(response.toJson());
//            }
            }
        }
    }

    private boolean cpuTurn(Party party) {
        if (party.getModel().getCurrentTurnPlayer().isBot()) {
            int quantity = party.getModel().getCurrentBidQuantity();
            int value = party.getModel().getCurrentBidValue();
            double choosed_prob = 0;
            int min_quantity = 0;
            int min_value = 0;
            double min_part = 1;
            int totalDicesCount = party.getModel().getTotalDicesCount();

            if (quantity == 0 || value == 0) {
                System.out.println("Bot bidding!");
                return party.getModel().tryMakeBid(party.getModel().getCurrentTurnPlayer(), 1, 6);
            }

            if (quantity > (totalDicesCount/2 - (int)(Math.random()*2))) {
                System.out.println("Bot doubting!");
                Player bot = party.getModel().getCurrentTurnPlayer();
                String loser;
                if (party.getModel().doubt(bot)) {
                    loser = party.getModel().getCurrentBidPlayer().getName();
                } else {
                    loser = bot.getName();
                }
                party.setMessage(loser + " loosing one dice!\n" + party.getModel().getDoubtMessage());
                party.setLoser(loser);
                if (party.getModel().getPlayers().size() == 1) {
                    party.setMessage(party.getMessage() + "\n" + party.getModel().getPlayers().get(0).getName() + " is the winner!");
                    party.getModel().setGameEnded(true);
                    parties.remove(party);
                }
                party.setNewTurn(true);
                return true;
            }

            if (value != 1 && !party.getModel().isMaputo()) {
                for (int i = 1; i < 5; ++i) {
                    for (int j = 0; j < 3; ++j) {
                        int[] dices = party.getModel().getCurrentTurnPlayer().getDices();
                        choosed_prob = ((double) (quantity - dices[0] - dices[i] + j) / ((double) totalDicesCount - dices[0] - dices[i]));
                        if (choosed_prob < min_part) {
                            if (j == 0 && i > value || j != 0) {
                                min_quantity = quantity + j;
                                min_value = i;
                                min_part = choosed_prob;
                            }
                        }
                    }
                }
                if (min_value != 0 && min_quantity >= totalDicesCount / 2) {
                    min_value = 0;
                    if (totalDicesCount % 2 == 1) {
                        min_quantity = (totalDicesCount / 2) + 1;
                    } else {
                        min_quantity = (totalDicesCount / 2);
                    }
                }
                System.out.println("Bot bidding!");
                return party.getModel().tryMakeBid(party.getModel().getCurrentTurnPlayer(), min_quantity, min_value + 1);
            }
            else {
                if (quantity > totalDicesCount/4) {
                    System.out.println("Bot doubting!");
                    Player bot = party.getModel().getCurrentTurnPlayer();
                    String loser;
                    if (party.getModel().doubt(bot)) {
                        loser = party.getModel().getCurrentBidPlayer().getName();
                    } else {
                        loser = bot.getName();
                    }
                    int d = 1;
                    party.setMessage(loser + " loosing one dice!\n" + party.getModel().getDoubtMessage());
                    party.setLoser(loser);
                    if (party.getModel().getPlayers().size() == 1) {
                        party.setMessage(party.getMessage() + "\n" + party.getModel().getPlayers().get(0).getName() + " is the winner!");
                        party.getModel().setGameEnded(true);
                        parties.remove(party);
                    }
                    party.setNewTurn(true);
                    return true;
                }
                else {
                    return party.getModel().tryMakeBid(party.getModel().getCurrentTurnPlayer(), quantity+1, value);
                }
            }
        }
        return false;
    }

    private class DelayedGameStartThread extends Thread {
        private final Party party;
        private final int delay;

        DelayedGameStartThread(Party party, int delay) {
            this.party = party;
            this.delay = delay;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startGame(party);
            dataIO.refreshParty(party);
        }
    }

    private class PerudoServerThread extends Thread {

        private WebUser webUser;

        PerudoServerThread(WebUser webUser) {
            this.webUser = webUser;
        }

        @Override
        public void run() {
            DataInputStream dataInputStream = webUser.getDataInputStream();
            PerudoClientCommand perudoClientCommand;
            while (true) {
                try {
                    perudoClientCommand = gson.fromJson(dataInputStream.readUTF(), PerudoClientCommand.class);
                    System.out.println(perudoClientCommand.toJson());
                    if (perudoClientCommand != null) {
                        proceedCommand(webUser, perudoClientCommand);
                        if (perudoClientCommand.getCommand().equals(PerudoClientCommandEnum.DISCONNECT))
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        webUser.disconnect();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }
    }
}
