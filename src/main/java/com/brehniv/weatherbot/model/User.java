package com.brehniv.weatherbot.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.sql.Time;
import java.sql.Timestamp;

@Entity(name="usersDataTable")
public class User {

    @Id
    private long chatid;
    private String first_name;
    private String last_name;
    private Timestamp registeredAt;
    private String fav_city;
    private boolean recieve_msg;

    public long getChatid() {
        return chatid;
    }

    public void setChatid(long chatid) {
        this.chatid = chatid;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getFav_city() {
        return fav_city;
    }

    public void setFav_city(String fav_city) {
        this.fav_city = fav_city;
    }

    public boolean isRecieve_msg() {
        return recieve_msg;
    }

    public void setRecieve_msg(boolean recieve_msg) {
        this.recieve_msg = recieve_msg;
    }
}
