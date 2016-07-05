package br.edu.ifspsaocarlos.sdm.mensageiro;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import br.edu.ifspsaocarlos.sdm.mensageiro.model.Contact;

public class AddContactActivity extends Activity {

    private EditText nameEditText;
    private EditText nickNameEditText;

    private RequestQueue volley;

    private boolean isUserRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        volley = Volley.newRequestQueue(this);
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
            addContact(contact);
        }
    }

    public void addContact(Contact contact) {

        JsonObjectRequest contactAddRequest = new JsonObjectRequest(ConstantsWS.WS_CONTACT_URL, contact.toJSON(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    if (isUserRegister) {

                        Long contactId = response != null ? response.getLong("id") : null;

                        if (contactId != null && contactId > 0) {
                            SharedPreferences preferences = getSharedPreferences(Constants.LOGGED_USER, MODE_PRIVATE);
                            preferences.edit().putLong(Constants.CONTACT_OWNER_ID, contactId).commit();
                        }

                    }

                    Intent contactsIntent = new Intent(AddContactActivity.this, ContactsActivity.class);
                    startActivity(contactsIntent);


                } catch (JSONException e) {
                    Log.e("add_contact_volley", "Falha ao parsear JSON resposta do adicionar contato", e);
                    handleError();
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

        volley.add(contactAddRequest);
    }

    private void handleError() {
        Toast.makeText(this, R.string.adduser_fail, Toast.LENGTH_SHORT).show();
    }

}
