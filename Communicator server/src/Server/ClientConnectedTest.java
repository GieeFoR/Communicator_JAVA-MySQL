package Server;

import Common.Client;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.sql.*;

class ClientConnectedTest {

    static Socket socketIn;
    static Socket socketOut;

    static ClientConnected clientConnected;

    @BeforeAll
    static void setUp() throws IOException {
        Server server = Server.getInstance();
        server.startServer(4999);

        socketIn = new Socket("127.0.0.1", 4999);
        socketOut = new Socket("127.0.0.1", 4999);
        clientConnected = new ClientConnected(socketIn, socketOut);
    }

    @Test
    void findNickname_nicknameNull() throws SQLException {
        //given
        String nickname = null;

        //when
        Client client = clientConnected.findNickname(nickname);

        //then
        Assert.assertNull(client);
    }

    @Test
    void findNickname_nicknameCorrect() throws SQLException {
        //given
        String nickname = "kamil";

        //when
        Client client = clientConnected.findNickname(nickname);

        //then
        Assert.assertEquals(nickname, client.getUsername());
    }

    @Test
    void findNickname_nicknameIncorrect() throws SQLException {
        //given
        String nickname = "kamil2";

        //when
        Client client = clientConnected.findNickname(nickname);

        //then
        Assert.assertNull(client);
    }

    @Test
    void findNumber_numberNull() throws SQLException {
        //given
        Integer number = null;

        //when
        Client client = clientConnected.findNumber(number);

        //then
        Assert.assertNull(client);
    }

    @Test
    void findNumber_numberCorrect() throws SQLException {
        //given
        Integer number = 1000;

        //when
        Client client = clientConnected.findNumber(number);

        //then
        Assert.assertEquals(number, client.getNumber());
    }

    @Test
    void findNumber_numberIncorrect() throws SQLException {
        //given
        Integer number = 999;

        //when
        Client client = clientConnected.findNumber(number);

        //then
        Assert.assertNull(client);
    }
}