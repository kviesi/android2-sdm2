package br.edu.ifspsaocarlos.sdm.mensageiro;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import br.edu.ifspsaocarlos.sdm.mensageiro.constant.Constants;
import br.edu.ifspsaocarlos.sdm.mensageiro.constant.ConstantsWS;
import br.edu.ifspsaocarlos.sdm.mensageiro.helper.ContactArrayAdapter;
import br.edu.ifspsaocarlos.sdm.mensageiro.helper.VolleyHelper;
import br.edu.ifspsaocarlos.sdm.mensageiro.model.Contact;

public class ContactsActivity extends ListActivity {

    private VolleyHelper volleyHelper;
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

        volleyHelper = new VolleyHelper(this);

        loadContactsAsync();
    }

    private void startAddContactActivity(boolean isUserRegister) {
        Intent loginIntent = new Intent(this, AddContactActivity.class);
        loginIntent.putExtra(Constants.OP_USER_REGISTER, isUserRegister);
        startActivity(loginIntent);
    }

    private void startMessagesService(ArrayList<String> contacts) {
        Intent serviceIntent = new Intent("NEW_MESSAGES_SERVICE");
        serviceIntent.putExtra("contactIds", contacts);
        serviceIntent.putExtra("ownerID", ownerID);
        startService(serviceIntent);
    }

    public void loadContactsAsync() {

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getResources().getString(R.string.listuser_load));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        final AsyncTask<Void, Void, Void> loadContactsTask = new AsyncTask<Void, Void, Void>() {

            protected Void doInBackground(Void... params) {

                volleyHelper.get(ConstantsWS.WS_CONTACT_URL, new VolleyHelper.VolleyCallback() {
                    @Override
                    public void onSuccess(JSONObject jsonObject, Context context) throws Exception {

                        JSONArray contactsJSONArray = jsonObject.getJSONArray("contatos");

                        ArrayList<Contact> contactList = new ArrayList<Contact>();
                        ArrayList<String> contactIds = new ArrayList<String>();

                        for(int i = 0 ; i < contactsJSONArray.length() ; i++) {

                            try {
                                JSONObject contactJSON = contactsJSONArray.getJSONObject(i);

                                Contact contact = new Contact();
                                contact.setNickName(contactJSON.getString("apelido"));
                                contact.setName(contactJSON.getString("nome_completo"));
                                contact.setId(contactJSON.getLong("id"));
                                contactIds.add(contactJSON.getString("id"));

                                contactList.add(contact);

                            } catch (JSONException e) {
                        /* Skip invalid item */
                            }

                        }

                        //show contacts in list view
                        setListAdapter(new ContactArrayAdapter(context, contactList));
                        if(!contactIds.isEmpty()) {
                            startMessagesService(contactIds);
                        }
                    }

                    @Override
                    public void onError(VolleyError volleyError, Context context) {
                        Toast.makeText(context, R.string.listuser_fail, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFatalError(Exception e, Context context) {
                        Toast.makeText(context, R.string.listuser_fail, Toast.LENGTH_SHORT).show();
                    }

                });

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progress.dismiss();
                super.onPostExecute(aVoid);
            }
        };

        loadContactsTask.execute();
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
