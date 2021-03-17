package Common;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Client extends Account implements Serializable {
    private static final long serialVersionUID = 1234567L;

    private final Integer id;
    private final Integer number;
    private String name;
    private String surname;
    List<Conversation> conversations;

    public Client(Integer id, Integer number, String login, String password, String name, String surname, Date creationDate, String eMail) {
        super(login, password, eMail, creationDate);
        this.number = number;
        this.id = id;
        this.name = name;
        this.surname = surname;
        conversations = new ArrayList<>();
    }

//    public Client(Integer id, Integer number, Account account) {
//        super(account.getLogin(), account.getPassword(), account.geteMail(), account.getCreationDate());
//        this.number = number;
//        this.id = id;
//        conversations = new ArrayList<>();
//    }

    public Integer getId() {
        return id;
    }

    public void addConversation(Conversation conversation) {
        conversations.add(conversation);
    }

//    public Conversation getConversation(int conversationId) {
//        if(conversationId < 0 || conversationId > conversations.size()-1) {
//            throw new ArrayIndexOutOfBoundsException();
//        }
//        return conversations.get(conversationId);
//    }

    public List<Conversation> getConversations() {
        return conversations;
    }

//    public void setConversations(List<Conversation> conversations) {
//        this.conversations = conversations;
//    }
//
//    public boolean checkNumber(Integer number) {
//        return this.number.equals(number);
//    }
//
    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }
}