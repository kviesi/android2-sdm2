package br.edu.ifspsaocarlos.sdm.mensageiro.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import br.edu.ifspsaocarlos.sdm.mensageiro.R;
import br.edu.ifspsaocarlos.sdm.mensageiro.model.Contact;

/**
 * Created by kaiov on 26/06/2016.
 */
public class ContactArrayAdapter extends ArrayAdapter<Contact> {

    public ContactArrayAdapter(Context context, ArrayList<Contact> contacts) {
        super(context, R.layout.item_contact, contacts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Contact contact = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_contact, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.itemContactName);

        name.setText(contact.getName());

        return convertView;
    }

}
