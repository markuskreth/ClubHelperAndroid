package de.kreth.mtvandroidhelper2.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import de.kreth.mtvandroidhelper2.R;
import de.kreth.mtvandroidhelper2.data.Person;
import de.kreth.mtvandroidhelper2.ui.fragments.PersonDetailFragment;
import de.kreth.mtvandroidhelper2.ui.fragments.PersonListFragment;

/**
 * An activity representing a list of Personen. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link PersonDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PersonListFragment} and the item details (if present) is a
 * {@link PersonDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link PersonListFragment.Callbacks} interface to listen for item selections.
 */
public class PersonListActivity extends FragmentActivity implements
		PersonListFragment.Callbacks {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_person_list);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	/**
	 * Callback method from {@link PersonListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onPersonSelected(Person person) {

		Intent detailIntent = new Intent(this, PersonDetailActivity.class);
		int personId = -1;
		if(person != null)
			personId = person.getId();
		detailIntent.putExtra(PersonDetailFragment.ARG_PERSON_ID, personId);
		startActivity(detailIntent);
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onNavigateBack();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onNavigateBack() {
		onBackPressed();
	}

}
