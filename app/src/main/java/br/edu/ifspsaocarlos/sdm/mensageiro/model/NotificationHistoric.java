package br.edu.ifspsaocarlos.sdm.mensageiro.model;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by kaiov on 04/07/2016.
 */
public class NotificationHistoric extends RealmObject {

    @PrimaryKey
    private Long id;
    private Long lastMessageID;
    private Long contactID;

    public void setContactID(Long contactID) {
        this.contactID = contactID;
    }

    public void setLastMessageID(Long lastMessageID) {
        this.lastMessageID = lastMessageID;
    }

    public Long getContactID() {
        return contactID;
    }

    public Long getLastMessageID() {
        return lastMessageID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
