package br.edu.ifspsaocarlos.sdm.mensageiro;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import br.edu.ifspsaocarlos.sdm.mensageiro.constant.Constants;
import br.edu.ifspsaocarlos.sdm.mensageiro.constant.ConstantsWS;
import br.edu.ifspsaocarlos.sdm.mensageiro.helper.MessageHelper;
import br.edu.ifspsaocarlos.sdm.mensageiro.model.Message;

public class MessagesActivity extends Activity {

    private ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);

    private ReentrantLock lock = new ReentrantLock();

    private RequestQueue volley;
    private ListView messageListView;
    private EditText messageEditText;
    private Button btnSend;

    private Long contactID; // selected contact id
    private Long ownerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        volley = Volley.newRequestQueue(this);
        messageListView = (ListView) findViewById(R.id.lvMessages);
        messageEditText = (EditText) findViewById(R.id.etMessage);
        btnSend = (Button) findViewById(R.id.btnEnviar);

        contactID = getIntent().getExtras().getLong(Constants.CONTACT_ID);
        ownerID = getSharedPreferences(Constants.LOGGED_USER, MODE_PRIVATE).getLong(Constants.CONTACT_OWNER_ID, 0);

        threadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadAllMessages();
                    }
                });
            }
        },0, 5, TimeUnit.SECONDS);
    }

    @Override
    protected void onDestroy() {
        threadPool.shutdownNow();

        super.onDestroy();
    }

    //load all messages async
    private void loadAllMessages() {
        try {
            lock.lock();

            final AsyncTask<Void, Void, ArrayList<Message>> loadMessagesOfUserStartup = new AsyncTask<Void, Void, ArrayList<Message>>() {

                @Override
                protected ArrayList<Message> doInBackground(Void... params) {
                    ArrayList<Message> allMessages = new ArrayList<>();
                    allMessages.addAll(MessageHelper.getMessages(0, ownerID, contactID));
                    allMessages.addAll(MessageHelper.getMessages(0, contactID, ownerID));
                    return allMessages;
                }
            };

            loadMessagesOfUserStartup.execute();

            ArrayList<Message> allMessages = null;
            try {
                allMessages = loadMessagesOfUserStartup.get(15, TimeUnit.SECONDS);
            } catch (Exception e) {
                Toast.makeText(this, R.string.message_fail_list, Toast.LENGTH_SHORT);
                return;
            }

            //order by ID
            Collections.sort(allMessages, new Comparator<Message>() {
                @Override
                public int compare(Message m1, Message m2) {
                    return Long.compare(m1.getId(), m2.getId());
                }
            });

            ArrayList<String> messages = new ArrayList<>();

            for(Message message : allMessages) {
                String messageBody = "";
                if (ownerID == message.getOrigID()) {
                    messageBody += "[eu]";
                }
                messageBody += " [" + message.getSubject() + "] " + message.getPayload();

                messages.add(messageBody);
            }

            messageListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages));
            messageListView.setSelection(messages.size()); //focus on last item

        } finally {
            lock.unlock();
        }
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

            JsonObjectRequest sendMessageRequest = new JsonObjectRequest(ConstantsWS.ADD_MESSAGE_WS_BASEURL, message.toJSON(), new com.android.volley.Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    handleSendMessageSuccess();
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    handleSendMessageError();
                }
            });

            volley.add(sendMessageRequest);
        }
    }

    private void handleSendMessageSuccess() {
        loadAllMessages();
        messageEditText.setText(""); // clean field
    }

    private void handleSendMessageError() {
        Toast.makeText(this, R.string.message_fail_send, Toast.LENGTH_SHORT).show();
    }

    private String nowDate() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
    }
}
