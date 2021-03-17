package Server;

import java.io.*;
import java.math.BigDecimal;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Common.*;

final class UserTable {
    static final String NAME = "USER";

    static final class Columns {
        static final String USER_ID = "USER_ID";
        static final String NUMBER = "NUMBER";
        static final String NAME = "NAME";
        static final String SURNAME = "SURNAME";
        static final String USERNAME = "USERNAME";
        static final String PASSWORD = "PASSWORD";
        static final String CREATION_DATE = "CREATION_DATE";
        static final String EMAIL = "E_MAIL";
    }
}

final class ConversationTable {
    static final String NAME = "CONVERSATION";

    static final class Columns {
        static final String CONV_ID = "CONVERSATION_ID";
        static final String FOUNDER_ID = "FOUNDER_ID";
        static final String NAME = "NAME";
        static final String DESCRIPTION = "DESCRIPTION";
    }
}

final class UserConversationTable {
    static final String NAME = "USER_CONVERSATION";

    static final class Columns {
        static final String USER_ID = "USER_ID";
        static final String CONV_ID = "CONVERSATION_ID";
    }
}

final class MessageTable {
    static final String NAME = "MESSAGE";

    static final class Columns {
        static final String MESSAGE_ID = "MESSAGE_ID";
        static final String AUTHOR_ID = "AUTHOR_ID";
        static final String CONV_ID = "CONVERSATION_ID";
        static final String CONTENT = "CONTENT";
        static final String SEND_DATE = "SEND_DATE";
    }
}

public class ClientConnected extends Thread {
    private final Server server;

    ExecutorService executorService;

    private final Socket dataReceiveSocket;
    private ObjectOutputStream objectOutputReceiveStream = null;
    private ObjectInputStream objectInputReceiveStream = null;

    private final Socket dataSendSocket;
    private ObjectOutputStream objectOutputSendStream = null;
    private ObjectInputStream objectInputSendStream = null;

    private Client client = null;

    public ClientConnected(Socket socketReceive, Socket socketSend) {
        server = Server.getInstance();
        System.out.println(socketReceive + " " + socketSend);
        this.dataReceiveSocket = socketReceive;
        this.dataSendSocket = socketSend;
        executorService = Executors.newFixedThreadPool(2);


    }

    @Override
    public void run() {
        OutputStream outputReceiveStream;
        InputStream inputReceiveStream;
        OutputStream outputSendStream;
        InputStream inputSendStream;

        InformationType informationType;

        try {
            outputReceiveStream = dataReceiveSocket.getOutputStream();
            inputReceiveStream = dataReceiveSocket.getInputStream();
            objectOutputReceiveStream = new ObjectOutputStream(outputReceiveStream);
            objectInputReceiveStream = new ObjectInputStream(inputReceiveStream);

            outputSendStream = dataSendSocket.getOutputStream();
            inputSendStream = dataSendSocket.getInputStream();
            objectOutputSendStream = new ObjectOutputStream(outputSendStream);
            objectInputSendStream = new ObjectInputStream(inputSendStream);
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }

        while(true) {
            try {
                informationType = (InformationType) objectInputReceiveStream.readObject();
                if(informationType.checkType(InformationType.LOGIN)) {
                    String username = (String) objectInputReceiveStream.readObject();
                    String password = (String) objectInputReceiveStream.readObject();

                    login(username, password);
                }
                else if(informationType.checkType(InformationType.REGISTER)) {
                    Account account = (Account) objectInputReceiveStream.readObject();
                    String name = (String) objectInputReceiveStream.readObject();
                    String surname = (String) objectInputReceiveStream.readObject();

                    signup(account, name, surname);
                }
                else if(informationType.checkType(InformationType.MESSAGE)) {
                    Message message = (Message) objectInputReceiveStream.readObject();

                    String query = String.format(
                            "INSERT INTO `%s`(`%s`, `%s`, `%s`, `%s`) VALUES (" +
                                    "%d, %d, '%s', CURRENT_DATE())",
                            MessageTable.NAME,
                            MessageTable.Columns.AUTHOR_ID,
                            MessageTable.Columns.CONV_ID,
                            MessageTable.Columns.CONTENT,
                            MessageTable.Columns.SEND_DATE,
                            message.getAuthorId(),
                            message.getConversation(),
                            message.getContent());

                    Statement statement = server.getConnection().createStatement();

                    int inserted = statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);

                    if(inserted == 1) {
                        ResultSet rs = statement.getGeneratedKeys();

                        BigDecimal idColVar = BigDecimal.valueOf(0);
                        while (rs.next()) {
                            idColVar = rs.getBigDecimal(1);
                        }
                        int mess_id = idColVar.intValue();
                        Message mess = new Message(mess_id, message.getContent(), message.getAuthorId(), message.getAuthorName(), message.getConversation(), new Date());

                        objectOutputReceiveStream.writeObject(ResponseType.CONFIRMATION);
                        objectOutputReceiveStream.writeObject(mess);

                        query = String.format("SELECT * from `%s` WHERE `%s` = %d",
                                UserConversationTable.NAME,
                                UserConversationTable.Columns.CONV_ID,
                                message.getConversation());

                        ResultSet resultSet = statement.executeQuery(query);
                        List<Integer> clients = new LinkedList<>();

                        while(resultSet.next()) {
                            int client_id = resultSet.getInt(UserConversationTable.Columns.USER_ID);
                            clients.add(client_id);
                        }

                        executorService.submit(() -> {
                            for(int cl : clients) {
                                for(ClientConnected cc : server.getClientsOnline()) {
                                    if(cc.client == null || cc.equals(this)) {
                                        continue;
                                    }
                                    if(cc.client.getId() == cl) {
                                        try {
                                            cc.sendMessage(mess);
                                        } catch (IOException e) {
                                            System.out.println(e.getMessage());
                                        }
                                    }
                                }
                            }
                        });
                    }
                    else {
                        objectOutputReceiveStream.writeObject(ResponseType.FAILURE);
                    }
                }
                else if(informationType.checkType(InformationType.FIND_USER_BY_NICKNAME)) {
                    String nickname = (String) objectInputReceiveStream.readObject();

                    Client c = findNickname(nickname);
                    if(c == null) {
                        objectOutputReceiveStream.writeObject(ResponseType.FAILURE);
                    }
                    else {
                        objectOutputReceiveStream.writeObject(ResponseType.CONFIRMATION);
                        objectOutputReceiveStream.writeObject(c.getUsername());
                        objectOutputReceiveStream.writeObject(c.getNumber().toString());
                        objectOutputReceiveStream.writeObject(c.getName());
                        objectOutputReceiveStream.writeObject(c.getSurname());
                    }
                }
                else if(informationType.checkType(InformationType.FIND_USER_BY_NUMBER)) {
                    String number = (String) objectInputReceiveStream.readObject();

                    Integer numberInt = Integer.parseInt(number);

                    Client c = findNumber(numberInt);
                    if(c == null) {
                        objectOutputReceiveStream.writeObject(ResponseType.FAILURE);
                    }
                    else {
                        objectOutputReceiveStream.writeObject(ResponseType.CONFIRMATION);
                        objectOutputReceiveStream.writeObject(c.getUsername());
                        objectOutputReceiveStream.writeObject(c.getNumber().toString());
                        objectOutputReceiveStream.writeObject(c.getName());
                        objectOutputReceiveStream.writeObject(c.getSurname());
                    }
                }
                else if(informationType.checkType(InformationType.NEW_CONVERSATION)) {

                    List <String> usernames = (List<String>) objectInputReceiveStream.readObject();
                    String convname = (String) objectInputReceiveStream.readObject();

                    List<Integer> clientsIdList = new ArrayList<>();
                    clientsIdList.add(client.getId());

                    List <Client> clientsInConversation = new LinkedList<>();

                    for(String username : usernames) {
                        Client c = findNickname(username);
                        if(c == null) {
                            objectOutputReceiveStream.writeObject(ResponseType.FAILURE);
                            continue;
                        }
                        clientsIdList.add(c.getId());
                        clientsInConversation.add(c);
                    }

                    String query = String.format(
                            "INSERT INTO `%s`(`%s`, `%s`, `%s`) VALUES ('%d', '%s', '%s')",
                            ConversationTable.NAME,
                            ConversationTable.Columns.FOUNDER_ID,
                            ConversationTable.Columns.NAME,
                            ConversationTable.Columns.DESCRIPTION,
                            client.getId(),
                            convname,
                            "brak");

                    Statement statement = server.getConnection().createStatement();

                    int inserted = statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);

                    if(inserted == 1) {
                        ResultSet rs = statement.getGeneratedKeys();

                        BigDecimal idColVar = BigDecimal.valueOf(0);
                        while (rs.next()) {
                            idColVar = rs.getBigDecimal(1);
                        }
                        int conv_id = idColVar.intValue();

                        Conversation conversation = new Conversation(conv_id, convname, "", client.getId(), clientsIdList);
                        server.incrementConversationsId();
                        client.addConversation(conversation);

                        for(Client cl : clientsInConversation) {
                            cl.addConversation(conversation);
                        }

                        for(int cl : conversation.getClients()) {
                            query = String.format(
                                    "INSERT INTO `%s`(`%s`, `%s`) VALUES ('%d', '%d')",
                                    UserConversationTable.NAME,
                                    UserConversationTable.Columns.USER_ID,
                                    UserConversationTable.Columns.CONV_ID,
                                    cl,
                                    conv_id);

                            statement.executeUpdate(query);

                            for(ClientConnected cc : server.getClientsOnline()) {
                                if(cc.client == null || cc.equals(this)) {
                                    continue;
                                }

                                if(cc.client.getId() == cl) {
                                    cc.objectOutputSendStream.writeObject(InformationType.NEW_CONVERSATION);
                                    cc.objectOutputSendStream.writeObject(conversation);
                                    break;
                                }
                            }
                        }
                        objectOutputReceiveStream.writeObject(ResponseType.CONFIRMATION);
                        objectOutputReceiveStream.writeObject(conversation);
                    }
                    else {
                        objectOutputReceiveStream.writeObject(ResponseType.FAILURE);
                    }
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                System.out.println(e.getMessage());
                    try {
                        objectOutputReceiveStream.writeObject(ResponseType.FAILURE);
                    } catch (IOException ioException) {
                        System.out.println(ioException.getMessage());
                    }
                if(client != null) {
                    server.getClientsLogged().remove(client);
                    client = null;
                }
                server.getClientsOnline().remove(this);
                return;
            }
        }
    }

    private void sendMessage(Message message) throws IOException {
        objectOutputSendStream.writeObject(InformationType.MESSAGE);
        objectOutputSendStream.writeObject(message);
    }

    public void login(String username, String password) throws SQLException, IOException {
        client = null;
        String query = String.format("SELECT * from `%s` WHERE `%s` = '%s' AND `%s` = '%s'",
                UserTable.NAME,
                UserTable.Columns.USERNAME,
                username,
                UserTable.Columns.PASSWORD,
                password);
        //password case insensitive

        ResultSet resultSet;
            Statement statement = server.getConnection().createStatement();
            Statement statement2 = server.getConnection().createStatement();
            Statement statement3 = server.getConnection().createStatement();
            Statement statement4 = server.getConnection().createStatement();
            Statement statement5 = server.getConnection().createStatement();
            resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            int id = resultSet.getInt(UserTable.Columns.USER_ID);
            int number = resultSet.getInt(UserTable.Columns.NUMBER);
            String name = resultSet.getString(UserTable.Columns.NAME);
            String surname = resultSet.getString(UserTable.Columns.SURNAME);
            String readUsername = resultSet.getString(UserTable.Columns.USERNAME);
            String readPassword = resultSet.getString(UserTable.Columns.PASSWORD);
            Date creation_date = resultSet.getDate(UserTable.Columns.CREATION_DATE);
            String email = resultSet.getString(UserTable.Columns.EMAIL);

            client = new Client(id, number, readUsername, readPassword, name, surname, creation_date, email);

            query = String.format("SELECT * from `%s` WHERE `%s` = '%d'",
                    UserConversationTable.NAME,
                    UserConversationTable.Columns.USER_ID,
                    id);

            ResultSet resultSet2 = statement2.executeQuery(query);

            List<Conversation> conversations = client.getConversations();

            while (resultSet2.next()) {
                int conv_id = resultSet2.getInt(UserConversationTable.Columns.CONV_ID);

                query = String.format("SELECT * from `%s` WHERE `%s` = '%d'",
                        UserConversationTable.NAME,
                        UserConversationTable.Columns.CONV_ID,
                        conv_id);

                ResultSet resultSet3 = statement3.executeQuery(query);
                List<Integer> clients = new LinkedList<>();

                while(resultSet3.next()) {
                    int client_id = resultSet3.getInt(UserConversationTable.Columns.USER_ID);
                    clients.add(client_id);
                }

                query = String.format("SELECT * from `%s` WHERE `%s` = '%d'",
                        ConversationTable.NAME,
                        ConversationTable.Columns.CONV_ID,
                        conv_id);

                resultSet3 = statement3.executeQuery(query);

                while(resultSet3.next()) {
                    int read_conv_id = resultSet3.getInt(ConversationTable.Columns.CONV_ID);
                    int founder_id = resultSet3.getInt(ConversationTable.Columns.FOUNDER_ID);
                    String conv_name = resultSet3.getString(ConversationTable.Columns.NAME);
                    String conv_description = resultSet3.getString(ConversationTable.Columns.DESCRIPTION);

                    query = String.format("SELECT * from `%s` WHERE `%s` = '%d'",
                        MessageTable.NAME,
                        MessageTable.Columns.CONV_ID,
                            read_conv_id);

                    ResultSet resultSet4 = statement4.executeQuery(query);
                    List<Message> messages = new LinkedList<>();

                    while(resultSet4.next()) {
                        int message_id = resultSet4.getInt(MessageTable.Columns.MESSAGE_ID);
                        int author_id = resultSet4.getInt(MessageTable.Columns.AUTHOR_ID);
                        int readConv_id = resultSet4.getInt(MessageTable.Columns.CONV_ID);
                        String content = resultSet4.getString(MessageTable.Columns.CONTENT);
                        Date sendDate = resultSet4.getDate(MessageTable.Columns.SEND_DATE);

                        query = String.format("SELECT `%s` from `%s` WHERE `%s` = '%s'",
                                UserTable.Columns.USERNAME,
                                UserTable.NAME,
                                UserTable.Columns.USER_ID,
                                author_id);

                        ResultSet resultSet5 = statement5.executeQuery(query);
                        String authorName = "";
                        while(resultSet5.next()) {
                            authorName = resultSet5.getString(UserTable.Columns.USERNAME);
                        }

                        messages.add(new Message(message_id, content, author_id, authorName, readConv_id, sendDate));
                    }

                    conversations.add(new Conversation(read_conv_id, conv_name, conv_description, founder_id, clients, messages));
                }
            }
        }

        if(client == null) {
            objectOutputReceiveStream.writeObject(ResponseType.WRONG_USERNAME_PASSWORD);
        }
        else {
            for (Client c : server.getClientsLogged()) {
                if (c.getId().equals(this.client.getId())) {
                    objectOutputReceiveStream.writeObject(ResponseType.ALREADY_LOGGED_IN);
                    client = null;
                    return;
                }
            }
            objectOutputReceiveStream.writeObject(ResponseType.CONFIRMATION);
            objectOutputReceiveStream.writeObject(client);
            Server.getInstance().getClientsLogged().add(client);
        }
    }


    public void signup(Account newAccount, String name, String surname) throws SQLException, IOException {
        String query = String.format("SELECT * from `%s` WHERE `%s` = '%s'",
                UserTable.NAME,
                UserTable.Columns.USERNAME,
                newAccount.getUsername());

        ResultSet resultSet;
        Statement statement = server.getConnection().createStatement();
        resultSet = statement.executeQuery(query);

        if(resultSet.next()) {
            objectOutputReceiveStream.writeObject(ResponseType.LOGIN_TAKEN);
            return;
        }

        query = String.format("SELECT * from `%s` WHERE `%s` = '%s'",
                UserTable.NAME,
                UserTable.Columns.EMAIL,
                newAccount.geteMail());

        resultSet = statement.executeQuery(query);

        if(resultSet.next()) {
            objectOutputReceiveStream.writeObject(ResponseType.EMAIL_TAKEN);
            return;
        }

        query = String.format(
                "INSERT INTO `%s`(`%s`, `%s`, `%s`, `%s`, `%s`, `%s`, `%s`) VALUES (" +
                        "%d, '%s', '%s', '%s', '%s', CURRENT_DATE(), '%s')",
                UserTable.NAME,
                UserTable.Columns.NUMBER,
                UserTable.Columns.NAME,
                UserTable.Columns.SURNAME,
                UserTable.Columns.USERNAME,
                UserTable.Columns.PASSWORD,
                UserTable.Columns.CREATION_DATE,
                UserTable.Columns.EMAIL,
                server.getBASE_NUMBER()+server.getAmountOfAccounts(),
                name,
                surname,
                newAccount.getUsername(),
                newAccount.getPassword(),
                newAccount.geteMail());

        server.incrementAmountOfAccounts();

        int inserted = statement.executeUpdate(query);

        if(inserted == 1) {
            objectOutputReceiveStream.writeObject(ResponseType.CONFIRMATION);
        }
        else {
            objectOutputReceiveStream.writeObject(ResponseType.FAILURE);
        }
    }

    public Client findNickname(String nickname) throws SQLException {
        String query = String.format("SELECT * from `%s` WHERE `%s` = '%s'",
                UserTable.NAME,
                UserTable.Columns.USERNAME,
                nickname);

        ResultSet resultSet;
        Client result = null;
        Statement statement = server.getConnection().createStatement();
        resultSet = statement.executeQuery(query);

        while(resultSet.next()) {
            int id = resultSet.getInt(UserTable.Columns.USER_ID);
            int number = resultSet.getInt(UserTable.Columns.NUMBER);
            String name = resultSet.getString(UserTable.Columns.NAME);
            String surname = resultSet.getString(UserTable.Columns.SURNAME);
            String readUsername = resultSet.getString(UserTable.Columns.USERNAME);
            String readPassword = resultSet.getString(UserTable.Columns.PASSWORD);
            Date creation_date = resultSet.getDate(UserTable.Columns.CREATION_DATE);
            String email = resultSet.getString(UserTable.Columns.EMAIL);

            result = new Client(id, number, readUsername, readPassword, name, surname, creation_date, email);
        }
        return result;
    }

    public Client findNumber(Integer number) throws SQLException {
        String query = String.format("SELECT * from `%s` WHERE `%s` = '%d'",
                UserTable.NAME,
                UserTable.Columns.NUMBER,
                number);

        ResultSet resultSet;
        Client result = null;
        Statement statement = server.getConnection().createStatement();
        resultSet = statement.executeQuery(query);

        while(resultSet.next()) {
            int id = resultSet.getInt(UserTable.Columns.USER_ID);
            int readNumber = resultSet.getInt(UserTable.Columns.NUMBER);
            String name = resultSet.getString(UserTable.Columns.NAME);
            String surname = resultSet.getString(UserTable.Columns.SURNAME);
            String username = resultSet.getString(UserTable.Columns.USERNAME);
            String password = resultSet.getString(UserTable.Columns.PASSWORD);
            Date creation_date = resultSet.getDate(UserTable.Columns.CREATION_DATE);
            String email = resultSet.getString(UserTable.Columns.EMAIL);

            result = new Client(id, readNumber, username, password, name, surname, creation_date, email);
        }
        return result;
    }

    public ObjectOutputStream getObjectOutputReceiveStream() {
        return objectOutputReceiveStream;
    }

    public ObjectInputStream getObjectInputReceiveStream() {
        return objectInputReceiveStream;
    }

    public ObjectOutputStream getObjectOutputSendStream() {
        return objectOutputSendStream;
    }

    public ObjectInputStream getObjectInputSendStream() {
        return objectInputSendStream;
    }
}