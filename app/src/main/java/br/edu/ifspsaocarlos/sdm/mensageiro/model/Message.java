package br.edu.ifspsaocarlos.sdm.mensageiro.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by kaiov on 25/06/2016.
 */
public class Message {

    private long id;
    private String payload;
    private Date dateSended;
    private long ownerId;
    private long destinationId;

    public Message() {
        this.dateSended = new Date();
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }


}
