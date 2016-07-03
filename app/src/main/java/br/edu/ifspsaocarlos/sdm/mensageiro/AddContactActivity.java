package br.edu.ifspsaocarlos.sdm.mensageiro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import br.edu.ifspsaocarlos.sdm.mensageiro.constant.Constants;
import br.edu.ifspsaocarlos.sdm.mensageiro.constant.ConstantsWS;
import br.edu.ifspsaocarlos.sdm.mensageiro.helper.VolleyHelper;
import br.edu.ifspsaocarlos.sdm.mensageiro.model.Contact;

public class AddContactActivity extends Activity {

    private EditText nameEditText;
    private EditText nickNameEditText;

    private VolleyHelper volleyHelper;

    private boolean isUserRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        volleyHelper = new VolleyHelper(this);
        isUserRegister = getIntent().getExtras().getBoolean(Constants.OP_USER_REGISTER);
    }

    public void onClickSave(View v) {

        nameEditText = (EditText) findViewById(R.id.name);
        nickNameEditText = (EditText) findViewById(R.id.nickname);

        Contact contact = new Contact();
        contact.setName(nameEditText.getText().toString());
        contact.setNickName(nickNameEditText.getText().toString());

        if(!contact.isValid()) {

            Toast.makeText(this, R.string.adduser_invalid, Toast.LENGTH_LONG).show();

        } else {

            AsyncTask<Contact, Void, Void> asyncTask = new AsyncTask<Contact, Void, Void>() {
                @Override
                protected Void doInBackground(Contact... params) {

                    addContact(params.length == 1 ? params[0] : null);

                    return null;
                }
            };

            asyncTask.execute(contact);
        }

    }

    public void addContact(Contact contact) {

        volleyHelper.post(ConstantsWS.WS_CONTACT_URL, contact, new VolleyHelper.VolleyCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject, Context context) throws Exception {

                if(isUserRegister) {

                    Long contactId  = jsonObject.getLong("id");

                    if(contactId != null && contactId > 0) {
                        SharedPreferences preferences = getSharedPreferences(Constants.LOGGED_USER, MODE_PRIVATE);
                        preferences.edit().putLong(Constants.CONTACT_OWNER_ID, contactId).commit();
                    }

                }

                Intent contactsIntent = new Intent(ContactsActivity.class.getName());
                startActivity(contactsIntent);
            }

            @Override
            public void onError(VolleyError volleyError, Context context) {
                Toast.makeText(context, R.string.adduser_fail, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFatalError(Exception e, Context context) {
                Toast.makeText(context, R.string.adduser_fail, Toast.LENGTH_SHORT).show();
            }

        });
    }

}
