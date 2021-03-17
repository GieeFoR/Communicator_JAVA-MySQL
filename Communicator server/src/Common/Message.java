package Common;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private static final long serialVersionUID = 1234567L;
    private final String content;
    private final int id;
    private final int authorId;
    private final String authorName;
    private final int conversation;
    private final Date send_date;

    public Message(String content, int authorId, String authorName, int conversation) {
        this.id = -1;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.conversation = conversation;
        this.send_date = null;
    }

    public Message(int id, String content, int authorId, String authorName, int conversation, Date send_date) {
        this.id = id;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.conversation = conversation;
        this.send_date = send_date;
    }

    public String getContent() {
        return this.content;
    }

    public int getAuthorId() {
        return authorId;
    }

    public int getConversation() {
        return conversation;
    }

    public Date getSend_date() {
        return send_date;
    }

    public String getAuthorName() {
        return authorName;
    }
}
