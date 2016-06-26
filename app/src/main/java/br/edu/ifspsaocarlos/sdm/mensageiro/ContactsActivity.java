package br.edu.ifspsaocarlos.sdm.mensageiro;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import br.edu.ifspsaocarlos.sdm.mensageiro.constant.Constants;
import br.edu.ifspsaocarlos.sdm.mensageiro.helper.ContactArrayAdapter;
import br.edu.ifspsaocarlos.sdm.mensageiro.helper.VolleyHelper;
import br.edu.ifspsaocarlos.sdm.mensageiro.model.Contact;

public class ContactsActivity extends ListActivity {

    private static final String WS_CONTACT_URL = "http://www.nobile.pro.br/sdm/mensageiro/contato";

    private VolleyHelper volleyHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        volleyHelper = new VolleyHelper(this);

        final AsyncTask<Void, Void, Void> loadContactsTask = new AsyncTask<Void, Void, Void>() {

            protected Void doInBackground(Void... params) {

                populateListAdapter();

                return null;
            }
        };

        loadContactsTask.execute();
    }

    public void populateListAdapter() {

        volleyHelper.get(WS_CONTACT_URL, new VolleyHelper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject, Context context) throws Exception {

                JSONArray contactsJSONArray = jsonObject.getJSONArray("contatos");

                ArrayList<Contact> contactList = new ArrayList<Contact>();

                for(int i = 0 ; i < contactsJSONArray.length() ; i++) {

                    try {
                        JSONObject contactJSON = contactsJSONArray.getJSONObject(i);

                        Contact contact = new Contact();
                        contact.setNickName(contactJSON.getString("apelido"));
                        contact.setName(contactJSON.getString("nome_completo"));
                        contact.setId(contactJSON.getLong("id"));

                        contactList.add(contact);

                    } catch (JSONException e) {
                        /* Skip invalid item */
                    }

                }

                //show contacts in list view
                setListAdapter(new ContactArrayAdapter(context, contactList));
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

    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        Contact contact = (Contact) l.getItemAtPosition(position);

        Intent messages = new Intent(this, MessagesActivity.class);
        messages.putExtra(Constants.CONTACT_ID, contact.getId());
        startActivity(messages);
    }

}
