package br.edu.ifspsaocarlos.sdm.mensageiro;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import br.edu.ifspsaocarlos.sdm.mensageiro.helper.VolleyHelper;
import br.edu.ifspsaocarlos.sdm.mensageiro.model.Contact;

public class ContactsActivity extends ListActivity implements AdapterView.OnItemClickListener {

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

                ArrayList<String> contactList = new ArrayList<String>();

                for(int i = 0 ; i < contactsJSONArray.length() ; i++) {

                    try {
                        JSONObject contactJSON = contactsJSONArray.getJSONObject(i);

                        contactList.add(contactJSON.getString("nome_completo") + " - " + contactJSON.getString("apelido"));

                    } catch (JSONException e) {
                        /* Skip invalid item */
                    }

                }

                //show contacts in list view
                setListAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1, contactList));
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent messages = new Intent(MessagesActivity.class.getName());
        messages.putExtra("contactID", 1L);

        startActivity(messages);
    }
}
