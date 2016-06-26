package br.edu.ifspsaocarlos.sdm.mensageiro;

import android.app.Activity;
import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import br.edu.ifspsaocarlos.sdm.mensageiro.model.Message;
import io.realm.Realm;
import io.realm.RealmResults;

public class MessagesActivity extends Activity {

    private String contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        this.contactId = savedInstanceState.getString("contactID");
    }

}
