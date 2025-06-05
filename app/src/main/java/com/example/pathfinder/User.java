package com.example.pathfinder;

import java.io.Serializable;

public class User implements Serializable {
    private String UID;
    private String name;
    private String telegramId;

    public User(String UID, String name, String telegramId){
        this.UID=UID;
        this.name=name;
        this.telegramId=telegramId;

    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUID() {
        return this.UID;
    }

    public String getTelegramId() { return this.telegramId; }

    public void setTelegramId(String telegramId) {
        this.telegramId = telegramId;
    }
}
