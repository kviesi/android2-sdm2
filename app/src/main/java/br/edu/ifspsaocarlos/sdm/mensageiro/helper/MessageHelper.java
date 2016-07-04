package br.edu.ifspsaocarlos.sdm.mensageiro.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import br.edu.ifspsaocarlos.sdm.mensageiro.constant.ConstantsWS;
import br.edu.ifspsaocarlos.sdm.mensageiro.model.Message;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by kaiov on 03/07/2016.
 */
public class MessageHelper {

    private static final OkHttpClient client = new OkHttpClient();

    public static Collection<Message> getMessages(int messageStart, long destId, long origId) {

        try {
            Request requestOfUser = new Request.Builder()
                    .url(ConstantsWS.MESSAGE_WS_BASEURL + "/" + messageStart + "/" + origId + "/" + destId)
                    .build();

            Response responseOfUser = client.newCall(requestOfUser).execute();

            JSONObject jsonObject = new JSONObject(responseOfUser.body().string());
            JSONArray messages = jsonObject.getJSONArray("mensagens");

            ArrayList<Message> messageList = new ArrayList<>();
            for(int i = 0 ; i < messages.length(); i++) {
                messageList.add(Message.of(messages.getJSONObject(i)));
            }

            return messageList;
        } catch (IOException e) {

        } catch (JSONException e) {

        }

        return new ArrayList<>();
    }
}
