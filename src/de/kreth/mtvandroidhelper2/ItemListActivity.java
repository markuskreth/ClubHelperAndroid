package de.kreth.mtvandroidhelper2;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import de.kreth.mtvandroidhelper2.R;
import de.kreth.mtvandroidhelper2.data.Person;
import de.kreth.mtvandroidhelper2.ui.PersonDetailActivity;
import de.kreth.mtvandroidhelper2.ui.PersonListActivity;
import de.kreth.mtvandroidhelper2.ui.fragments.ItemListFragment;
import de.kreth.mtvandroidhelper2.ui.fragments.PersonDetailFragment;
import de.kreth.mtvandroidhelper2.ui.fragments.PersonListFragment;

/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class ItemListActivity extends FragmentActivity implements ItemListFragment.Callbacks, PersonListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			Factory.getInstance().init(this);
		} catch (NameNotFoundException e) {
			Log.e(Factory.getInstance().TAG(), "Fehler bei Factory init", e);
		}
		setContentView(R.layout.activity_item_list);

		if (findViewById(R.id.item_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((ItemListFragment) getSupportFragmentManager().findFragmentById(
					R.id.item_list)).setActivateOnItemClick(true);
		}

		// TODO: If exposing deep links into your app, handle intents here.
	}

	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(MenuItem menuItem) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			Fragment fragment = createFragment(menuItem);
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.item_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, PersonListActivity.class);
			startActivity(detailIntent);
		}
	}

	private Fragment createFragment(MenuItem menuItem) {
		switch (menuItem) {
		case PersonContacts:
			return new PersonListFragment();
		case PersonList:
			return new PersonListFragment();
		default:
			throw new IllegalArgumentException("Kein Fragment gefunden für MenuItem " + menuItem);
		}
	}

	@Override
	public void onPersonSelected(Person person) {
		if (mTwoPane){
			Bundle arguments = new Bundle();
			
			if(person != null)
				arguments.putInt(PersonDetailFragment.ARG_PERSON_ID, person.getId());
			
			Fragment fragment = new PersonDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.item_detail_container, fragment).commit();
		} else {

			Intent detailIntent = new Intent(this, PersonDetailActivity.class);
			detailIntent.putExtra(PersonDetailFragment.ARG_PERSON_ID, person.getId());
			startActivity(detailIntent);
		}
	}

}
