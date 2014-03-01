package de.kreth.mtvandroidhelper2.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import de.kreth.mtvandroidhelper2.data.Person;
import de.kreth.mtvandroidhelper2.data.PersonPersisterImpl;

public class PersonListAdapter extends ArrayAdapter<Person> {
	
	public PersonListAdapter(Context context, PersonPersisterImpl personPersister, List<Person> values) {
		super(context, android.R.layout.simple_list_item_1, values);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		view.setFocusable(false);
		return view;
	}
	@Override
	public long getItemId(int position) {
		Person item = getItem(position);
		return item.getId();
	}
	
}
