package Server;

import Common.Client;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class Server {
    private static Server instance = null;
    private ServerSocket serverSocket = null;
    private Socket dataReceiveSocket = null;
    private Socket dataSendSocket = null;
    private final List<ClientConnected> clientsOnline = new LinkedList<>();
    private final List<Client> clientsLogged = new LinkedList<>();
    private final List<Client> clientsAccounts = new LinkedList<>();

    private Connection connection;

    private int conversationsId = 0;
    private final int BASE_NUMBER = 1000;
    private int amountOfAccounts = 0;

    public static void main(String[] args) {
        Server server = getInstance();
        server.startServer(4999);

        while(true) {
            server.listenForClients();
        }
    }

    Server() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/communicator?serverTimezone=Europe/Warsaw", "communicator", "pass");

            Statement statement = connection.createStatement();

            String query = "SELECT * from `USER`";
            ResultSet resultSet = statement.executeQuery(query);

            while(resultSet.next()) {
                amountOfAccounts++;
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static Server getInstance() {
        if(instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public List<ClientConnected> getClientsOnline() {
        return clientsOnline;
    }

    public List<Client> getClientsAccounts() {
        return clientsAccounts;
    }

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listenForClients() {
        try {
            dataReceiveSocket = serverSocket.accept();
            dataSendSocket = serverSocket.accept();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("client connected");
        ClientConnected clientConnected = new ClientConnected(dataReceiveSocket, dataSendSocket);
        clientsOnline.add(clientConnected);
        clientConnected.start();
    }

    public Connection getConnection() {
        return connection;
    }

    public int getConversationsId() {
        return conversationsId;
    }

    public void incrementConversationsId() {
        this.conversationsId++;
    }

    public int getBASE_NUMBER() {
        return BASE_NUMBER;
    }

    public int getAmountOfAccounts() {
        return amountOfAccounts;
    }

    public void incrementAmountOfAccounts() {
        this.amountOfAccounts++;
    }

    public List<Client> getClientsLogged() {
        return clientsLogged;
    }
}