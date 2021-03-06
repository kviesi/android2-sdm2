package br.edu.ifspsaocarlos.sdm.mensageiro.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kaiov on 25/06/2016.
 */
public class Contact {

    private String name;
    private String nickName;
    private long id;

    public static Contact of(JSONObject json) {
        try {
            Contact contact = new Contact();
            contact.setNickName(json.getString("apelido"));
            contact.setName(json.getString("nome_completo"));
            contact.setId(json.getLong("id"));
            return contact;
        } catch (JSONException e) {
            return null;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean isValid() {
        return name != null && !name.isEmpty() && name.length() <= 50 &&
                nickName != null && !nickName.isEmpty() && nickName.length() <= 100;
    }

    public JSONObject toJSON() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("apelido", nickName);
            jsonObject.put("nome_completo", name);
            return jsonObject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", nickName='" + nickName + '\'' +
                ", id=" + id +
                '}';
    }
}
