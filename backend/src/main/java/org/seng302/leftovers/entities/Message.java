package org.seng302.leftovers.entities;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.time.Instant;

/**
 * Entity that represents a single message sent throughout a conversation
 */
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false)
    private Instant created = Instant.now();

    @Column(nullable = false)
    private String content;

    protected Message() {} // Required by JPA

    /**
     * Construct a new message
     * @param sender Sender of the message
     * @param content Initial message content
     */
    public Message(User sender, String content) {
        if (sender == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message sender cannot be null");
        this.sender = sender;
        setContent(content);
    }

    /**
     * Gets the generated id
     * @return Message Id
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the conversation that owns this message
     * @return Conversation with this message
     */
    public Conversation getConversation() {
        return conversation;
    }

    /**
     * Gets the sender of the message
     * @return Message sender
     */
    public User getSender() {
        return sender;
    }

    /**
     * Gets the moment the message was created
     * @return Message creation time
     */
    public Instant getCreated() {
        return created;
    }

    /**
     * Gets the message content
     * @return Content of the message
     */
    public String getContent() {
        return content;
    }

    /**
     * Validates and sets the message content
     * @param content New message content
     */
    public void setContent(String content) {
        if (content == null || content.isBlank()) {
            content = "";
        }
        if (content.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Messages must be 200 characters or less");
        }
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", conversationId=" + conversation.getId() +
                ", senderId=" + sender.getUserID() +
                ", created=" + created +
                ", content='" + content + '\'' +
                '}';
    }
}
