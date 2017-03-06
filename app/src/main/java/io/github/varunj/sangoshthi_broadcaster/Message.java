package io.github.varunj.sangoshthi_broadcaster;

/**
 * Created by Varun on 04-03-2017.
 */

public class Message implements java.io.Serializable {
    private String sender;
    private String message;
    private String timestamp;
    private String receiver;

    public void setSender(String sender) {
        this.sender = sender ;
    }
    public String getSender() {
        return sender ;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public String getTimestamp() {
        return timestamp;
    }

    public void setReciever(String receiver) {
        this.receiver = receiver ;
    }
    public String getReceiver() {
        return receiver ;
    }
}
