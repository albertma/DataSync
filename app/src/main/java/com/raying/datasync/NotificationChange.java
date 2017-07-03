package com.raying.datasync;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by albertma on 26/06/2017.
 */

public class NotificationChange {

    public long getChangeSignature() {
        return changeSignature;
    }

    public void setChangeSignature(long changeSignature) {
        this.changeSignature = changeSignature;
    }

    public long getChangeTimestamp() {
        return changeTimestamp;
    }

    public void setChangeTimestamp(long changeTimestamp) {
        this.changeTimestamp = changeTimestamp;
    }

    public ArrayList<Map<String, Object>> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Map<String, Object>> messages) {
        this.messages = messages;
    }

    private long changeSignature;

    private long changeTimestamp;

    private ArrayList<Map<String, Object>> messages;


}
