package Common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Conversation implements Serializable {
    private static final long serialVersionUID = 1234567L;

    private final int id;
    private final Integer founder;
    private final List<Integer> clients;
    private final List<Message> messages;
    private String name;
    private final String description;
    private final Date date;

    public Conversation(int id, String name, String description, Integer founder, List <Integer> clients, List <Message> messages)  {
        this.name = name;
        this.description = description;
        this.founder = founder;
        this.clients = clients;
        this.messages = messages;
        date = new Date();
        this.id = id;
    }

    public Conversation(int id, String name, String description, Integer founder, List <Integer> clients)  {
        this.name = name;
        this.description = description;
        this.founder = founder;
        this.clients = clients;
        this.messages = new ArrayList<>();
        date = new Date();
        this.id = id;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public List<Integer> getClients() {
        return clients;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addClient(Integer client) {
        this.clients.add(client);
    }
}