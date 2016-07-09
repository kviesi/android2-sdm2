package br.edu.ifspsaocarlos.sdm.mensageiro.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import br.edu.ifspsaocarlos.sdm.mensageiro.MessagesActivity;
import br.edu.ifspsaocarlos.sdm.mensageiro.R;
import br.edu.ifspsaocarlos.sdm.mensageiro.constant.Constants;
import br.edu.ifspsaocarlos.sdm.mensageiro.constant.ConstantsWS;
import br.edu.ifspsaocarlos.sdm.mensageiro.model.NotificationHistoric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by kaiov on 03/07/2016.
 */
public class MessagesService extends Service implements Runnable {

    public static final String LOG_TAG = "messages_notification";
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private RequestQueue volley;
    private ArrayList<String> contactIds; //all contacts
    private Long ownerID; //loggedUser

    public void onCreate() {
        super.onCreate();
        volley = Volley.newRequestQueue(this);

        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm.setDefaultConfiguration(realmConfig);



        scheduledExecutorService.scheduleAtFixedRate(this, 5, 10, TimeUnit.SECONDS);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        int startCommand = super.onStartCommand(intent, flags, startId);

        Bundle extras = intent.getExtras();

        contactIds = extras.getStringArrayList("contactIds");
        ownerID = extras.getLong("ownerID");

        return startCommand;
    }

    @Override
    public void onDestroy() {
        scheduledExecutorService.shutdownNow();
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void run() {

        Realm realm = Realm.getDefaultInstance();

        Log.i("messages_notification", "ID do contato: " + ownerID);

        Collection<String> urls = new ArrayList<>();

        for (String contactID : contactIds) {
            if (contactID == null) {
                continue;
            }

            NotificationHistoric historic = realm.where(NotificationHistoric.class)
                    .equalTo("contactID", Long.valueOf(contactID))
                    .findFirst();

            if (Long.valueOf(contactID) != ownerID && historic != null && historic.getLastMessageID() != null) {
                urls.add(ConstantsWS.MESSAGE_WS_BASEURL + "/" + (historic.getLastMessageID() + 1) + "/" + contactID + "/" + ownerID);
            } else {
                urls.add(ConstantsWS.MESSAGE_WS_BASEURL + "/0/" + contactID + "/" + ownerID);
            }
        }

        for (String url : urls) {

            Log.i("messages_notification", "Enviando para fila volley [" + url + "]");

            StringRequest request = new StringRequest(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {

                        JSONObject jsonObject = new JSONObject(response);

                        JSONArray messages = jsonObject.getJSONArray("mensagens");
                        if (messages == null || messages.length() <= 0) {
                            return;
                        }

                        JSONObject lastMessage = messages.getJSONObject(messages.length() - 1);
                        if (lastMessage == null) {
                            return; //ignore
                        }

                        final Long messageID = lastMessage.getLong("id");
                        final Long contactID = lastMessage.getLong("origem_id");

                        //realm instance
                        Realm realm = Realm.getDefaultInstance();

                        NotificationHistoric historic = realm.where(NotificationHistoric.class)
                                                             .equalTo("contactID", contactID)
                                                             .findFirst();

                        Long lastMessageID = historic != null ? historic.getLastMessageID() : null;

                        Log.i(LOG_TAG, "Last message stored ID: " + lastMessageID + " to contact ID: " + contactID);

                        // se nao tem historio ainda.. sera a primeira notificacao
                        if (historic == null || historic.getLastMessageID() == null) {
                            lastMessageID = messageID;
                        } else {
                            //ultima mensagem deve ser diferente da
                            lastMessageID = messageID != lastMessageID ? messageID : -1;
                        }

                        if (lastMessageID > 0 ) {
                            Log.i(LOG_TAG, "Notificando mensagem: " + messageID);
                            showNotification(contactID);

                            //persist last message
                            realm.beginTransaction();
                            if (historic == null) {
                                historic = realm.createObject(NotificationHistoric.class);
                                historic.setContactID(contactID);
                                historic.setLastMessageID(lastMessageID);
                            } else {
                                historic.setLastMessageID(lastMessageID);
                            }
                            realm.commitTransaction();

                        }

                    } catch (JSONException e) {
                        Log.e(LOG_TAG,"Fail to notify user", e);
                    }
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("get_message_listener", error.getMessage());
                }
            });

            volley.add(request);
        }
    }


    public void showNotification(Long contactID) {

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MessagesActivity.class);
        intent.putExtra(Constants.CONTACT_ID, contactID);

        PendingIntent p = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setTicker(getString(R.string.not_new_message_title));
        builder.setContentTitle(getString(R.string.not_new_message_title));
        builder.setContentText(getString(R.string.not_new_message_text));
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(p);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setAutoCancel(true);

        Notification notification = builder.build();
        //notification.vibrate = new long[] {100, 250};

        nm.notify(R.mipmap.ic_launcher, notification);
    }

}
