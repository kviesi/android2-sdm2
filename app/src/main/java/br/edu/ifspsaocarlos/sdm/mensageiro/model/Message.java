package br.edu.ifspsaocarlos.sdm.mensageiro.model;

import org.json.JSONObject;

import java.util.Date;

import br.edu.ifspsaocarlos.sdm.mensageiro.helper.VolleyHelper;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by kaiov on 25/06/2016.
 */
public class Message implements VolleyHelper.JSONModel {

    private long id;
    private String payload;
    private String subject;
    private long destID;
    private long origID;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public long getDestID() {
        return destID;
    }

    public void setDestID(long destID) {
        this.destID = destID;
    }

    public long getOrigID() {
        return origID;
    }

    public void setOrigID(long origID) {
        this.origID = origID;
    }

    public boolean isValid() {
        return payload != null && !payload.isEmpty() && payload.length() <= 150;

    }

    @Override
    public JSONObject toJSON() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("origem_id", origID);
            jsonObject.put("destino_id", destID);
            jsonObject.put("assunto", subject);
            jsonObject.put("corpo", payload);
            return jsonObject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", payload='" + payload + '\'' +
                ", subject='" + subject + '\'' +
                ", destID=" + destID +
                ", origID=" + origID +
                '}';
    }
}
