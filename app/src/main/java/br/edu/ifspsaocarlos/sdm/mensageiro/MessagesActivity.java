package br.edu.ifspsaocarlos.sdm.mensageiro;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import br.edu.ifspsaocarlos.sdm.mensageiro.helper.VolleyHelper;
import br.edu.ifspsaocarlos.sdm.mensageiro.model.Message;

public class MessagesActivity extends Activity {

    private static final String MESSAGE_WS_BASEURL = "http://www.nobile.pro.br/sdm/mensageiro/mensagem";

    private VolleyHelper volleyHelper;
    private ListView messageListView;
    private EditText messageEditText;

    private Long contactID; // selected contact id
    private Long ownerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        volleyHelper = new VolleyHelper(this);
        messageListView = (ListView) findViewById(R.id.lvMessages);
        messageEditText = (EditText) findViewById(R.id.etMessage);

        contactID = 3L;
        ownerID = 2L;

        loadAllMessages();
    }

    //load all messages async
    private void loadAllMessages() {
        final AsyncTask<Void, Void, Void> loadMessagesStartup = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                volleyHelper.get(MESSAGE_WS_BASEURL + "/0/" + ownerID + "/" + contactID, new VolleyHelper.VolleyCallback() {

                    @Override
                    public void onSuccess(JSONObject jsonObject, Context context) throws Exception {

                        JSONArray messagesJson = jsonObject.getJSONArray("mensagens");

                        ArrayList<String> messageList = new ArrayList<String>(messagesJson.length());

                        for(int i = 0 ; i < messagesJson.length() ; i++) {

                            JSONObject messageJson = messagesJson.getJSONObject(i);

                            messageList.add("[" + messageJson.getString("assunto") + "] " + messageJson.getString("corpo"));
                        }

                        messageListView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, messageList));
                        messageListView.setSelection(messageList.size()); //focus on last item
                    }

                    @Override
                    public void onError(VolleyError volleyError, Context context) {
                        Toast.makeText(context, R.string.message_fail_list, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFatalError(Exception e, Context context) {
                        Toast.makeText(context, R.string.message_fail_list, Toast.LENGTH_SHORT).show();
                    }
                });

                return null;
            }
        };

        loadMessagesStartup.execute();
    }

    public void onClickSendMessage(View v) {

        Message message = new Message();
        message.setDestID(contactID);
        message.setOrigID(ownerID);
        message.setSubject(nowDate());
        message.setPayload(messageEditText.getText().toString());

        if(!message.isValid()) {

            Toast.makeText(this, R.string.message_invalid, Toast.LENGTH_SHORT).show();

        } else {

            volleyHelper.post(MESSAGE_WS_BASEURL, message, new VolleyHelper.VolleyCallback() {
                @Override
                public void onSuccess(JSONObject jsonObject, Context context) throws Exception {
                    loadAllMessages();
                    messageEditText.setText(""); // clean field
                }

                @Override
                public void onError(VolleyError volleyError, Context context) {
                    Toast.makeText(context, R.string.message_fail_send, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFatalError(Exception e, Context context) {
                    Toast.makeText(context, R.string.message_fail_send, Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    private String nowDate() {
        return new SimpleDateFormat("dd/mm/yyyy HH:mm:ss").format(new Date());
    }
}
