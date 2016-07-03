package br.edu.ifspsaocarlos.sdm.mensageiro.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONArray;
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
import br.edu.ifspsaocarlos.sdm.mensageiro.helper.VolleyHelper;

/**
 * Created by kaiov on 03/07/2016.
 */
public class MessagesService extends Service implements Runnable {

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private HashMap<Long, Long> userLastMessageId = new HashMap<>(); //last message id by contact
    private VolleyHelper volleyHelper;
    private ArrayList<String> contactIds; //all contacts
    private Long ownerID; //loggedUser

    public void onCreate() {
        super.onCreate();
        volleyHelper = new VolleyHelper(this);

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

        Collection<String> urls = new ArrayList<>();

        for (String contactID : contactIds) {
            if(contactID == null) {
                continue;
            }

            if(userLastMessageId.containsKey(contactID)) {
                urls.add(ConstantsWS.MESSAGE_WS_BASEURL + "/" + userLastMessageId.get(contactID) + "/" + ownerID + "/" + contactID);
            } else {
                urls.add(ConstantsWS.MESSAGE_WS_BASEURL + "/0/" + ownerID + "/" + contactID);
            }
        }

        VolleyHelper.VolleyCallback volleyCallback = new VolleyHelper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject, Context context) throws Exception {

                JSONArray messages = jsonObject.getJSONArray("mensagens");
                if(messages == null || messages.length() <= 0) {
                    return;
                }

                JSONObject lastMessage = messages.getJSONObject(messages.length() - 1);
                if(lastMessage == null) {
                    return; //ignore
                }

                Long messageID = lastMessage.getLong("id");
                Long contactID = lastMessage.getLong("destino_id");

                boolean firstMessage = false;
                if(!userLastMessageId.containsKey(contactID)) {
                    firstMessage = true;
                }

                if(firstMessage || (messageID != userLastMessageId.get(contactID))) {
                    showNotification(contactID);
                    userLastMessageId.put(contactID, messageID); //update last message
                }
            }

            @Override
            public void onError(VolleyError volleyError, Context context) {
                Log.e("ms_error_message", volleyError.getMessage() == null ? "REQUEST ERROR" : volleyError.getMessage());
            }

            @Override
            public void onFatalError(Exception e, Context context) {
                Log.e("ms_exception_message", "FATAL_ERROR", e);
            }
        };

        volleyHelper.getMany(urls, volleyCallback); //run all requests
    }


    public void showNotification(Long contactID) {

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MessagesActivity.class);
        intent.putExtra(Constants.CONTACT_ID, contactID);

        PendingIntent p = PendingIntent.getActivity(this, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setTicker(getString(R.string.not_new_message_title));
        builder.setContentTitle(getString(R.string.not_new_message_title));
        builder.setContentText(getString(R.string.not_new_message_text));
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(p);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        Notification notification = builder.build();
        //notification.vibrate = new long[] {100, 250};

        nm.notify(R.mipmap.ic_launcher, notification);
    }

}
