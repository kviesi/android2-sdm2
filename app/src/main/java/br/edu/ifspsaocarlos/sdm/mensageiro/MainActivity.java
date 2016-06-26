package br.edu.ifspsaocarlos.sdm.mensageiro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Iterator;

import br.edu.ifspsaocarlos.sdm.mensageiro.model.Message;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String userID = getSharedPreferences("LOGGED_USER", MODE_PRIVATE).getString("ownerID", null);

        if(userID == null) {
            Intent loginIntent = new Intent();
            startActivity(loginIntent);
        }



    }
}