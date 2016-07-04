package br.edu.ifspsaocarlos.sdm.mensageiro;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import br.edu.ifspsaocarlos.sdm.mensageiro.constant.Constants;
import br.edu.ifspsaocarlos.sdm.mensageiro.constant.ConstantsWS;
import br.edu.ifspsaocarlos.sdm.mensageiro.adapter.ContactArrayAdapter;
import br.edu.ifspsaocarlos.sdm.mensageiro.helper.MessageHelper;
import br.edu.ifspsaocarlos.sdm.mensageiro.model.Contact;
import br.edu.ifspsaocarlos.sdm.mensageiro.service.MessagesService;

public class ContactsActivity extends ListActivity {

    private RequestQueue volley;
    private Long ownerID = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        //check if user is registred.
        Long userID = getSharedPreferences(Constants.LOGGED_USER, MODE_PRIVATE).getLong(Constants.CONTACT_OWNER_ID, 0);
        if(userID == null || userID == 0) {
            startAddContactActivity(true);
        } else {
            ownerID = userID;
        }

        volley = Volley.newRequestQueue(this);
        loadContactsAsync();
    }

    @Override
    protected void onDestroy() {
        Intent serviceIntent = new Intent("NEW_MESSAGES_SERVICE");
        stopService(serviceIntent);

        super.onDestroy();
    }

    public void loadContactsAsync() {

        StringRequest contactRequest = new StringRequest(ConstantsWS.WS_CONTACT_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                ArrayList<Contact> contactList = new ArrayList<Contact>();
                ArrayList<String> contactIds = new ArrayList<String>();

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray contactsJSONArray = jsonObject.getJSONArray("contatos");

                    for (int i = 0; i < contactsJSONArray.length(); i++) {
                        Contact contact = Contact.of(contactsJSONArray.getJSONObject(i));
                        if(contact.getId() != ownerID) {
                            contactIds.add(String.valueOf(contact.getId()));
                            contactList.add(contact);
                        }
                    }

                } catch (JSONException e) {
                    Log.e("list_contact_volley", "Parse JSON resposta da listagem de contatos", e);
                }

                //show contacts in list view
                ContactsActivity.this.setListAdapter(new ContactArrayAdapter(getBaseContext(), contactList));
                if (!contactIds.isEmpty()) {
                    Intent serviceIntent = new Intent();
                    serviceIntent.setClassName("br.edu.ifspsaocarlos.sdm.mensageiro.service", MessagesService.class.getName());
                    serviceIntent.putExtra("contactIds", contactIds);
                    serviceIntent.putExtra("ownerID", ownerID);
                    startService(serviceIntent);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleError();
            }
        });

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getResources().getString(R.string.listuser_load));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        volley.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Void>() {

            @Override
            public void onRequestFinished(Request<Void> request) {
                progress.dismiss();
            }
        });

        volley.add(contactRequest);
    }

    private void startAddContactActivity(boolean isUserRegister) {
        Intent loginIntent = new Intent(this, AddContactActivity.class);
        loginIntent.putExtra(Constants.OP_USER_REGISTER, isUserRegister);
        startActivity(loginIntent);
    }

    private void handleError() {
        Toast.makeText(this, R.string.listuser_fail, Toast.LENGTH_SHORT).show();
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        Contact contact = (Contact) l.getItemAtPosition(position);

        Intent messages = new Intent(this, MessagesActivity.class);
        messages.putExtra(Constants.CONTACT_ID, contact.getId()); //send contact to messages
        startActivity(messages);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onMenuItemSelected(int panel, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.add_contact:
                startAddContactActivity(false);
                break;
            case R.id.update_contact:
                loadContactsAsync();
                break;
        }

        return true;
    }
}
