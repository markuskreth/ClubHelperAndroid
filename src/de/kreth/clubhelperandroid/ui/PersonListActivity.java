package de.kreth.clubhelperandroid.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import de.kreth.clubhelperandroid.R;
import de.kreth.clubhelperandroid.ui.controller.PersonListController;
import de.kreth.clubhelperandroid.ui.fragments.PersonDetailFragment;
import de.kreth.clubhelperandroid.ui.fragments.PersonListFragment;
import de.kreth.clubhelperbusiness.controller.PersonDetailController;
import de.kreth.clubhelperbusiness.data.Person;

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

	private PersonListFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_person_list);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		setupFragmentArguments();

	}

	private void setupFragmentArguments() {
		fragment = (PersonListFragment) getSupportFragmentManager().findFragmentById(R.id.person_list);
		Bundle arguments = getIntent().getExtras();
		String modeName = arguments.getString(PersonListController.PERSON_LIST_MODE.class.getName());
		fragment.setMode(PersonListController.PERSON_LIST_MODE.valueOf(modeName));
	}

	public PersonListFragment getFragment() {
		return fragment;
	}
	
	/**
	 * Callback method from {@link PersonListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onPersonSelected(Person person) {

		Intent detailIntent = new Intent(this, PersonDetailActivity.class);
		long personId = -1;
		if(person != null)
			personId = person.getId();
		detailIntent.putExtra(PersonDetailController.ARG_PERSON_ID, personId);
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
