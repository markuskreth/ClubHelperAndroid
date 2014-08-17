package de.kreth.clubhelperandroid;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import de.kreth.clubhelperandroid.ui.PersonDetailActivity;
import de.kreth.clubhelperandroid.ui.PersonListActivity;
import de.kreth.clubhelperandroid.ui.controller.PersonListController;
import de.kreth.clubhelperandroid.ui.fragments.ItemListFragment;
import de.kreth.clubhelperandroid.ui.fragments.PersonDetailFragment;
import de.kreth.clubhelperandroid.ui.fragments.PersonListFragment;
import de.kreth.clubhelperandroid.ui.utils.FragmentNavigator;
import de.kreth.clubhelperbusiness.controller.PersonDetailController;
import de.kreth.clubhelperbusiness.data.Person;

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
public class ItemListActivity extends FragmentActivity implements ItemListFragment.Callbacks, PersonListFragment.Callbacks, FragmentNavigator {

	// FIXME java.util.ConcurrentModifcationException tritt auf wenn Drop-Event auftritt - scheinbar sowohl im leeren Raum, als auch beim Löschen!?
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private int mPaneCount = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			FactoryAndroid.getInstance().init(this);
		} catch (NameNotFoundException e) {
			Log.e(FactoryAndroid.getInstance().TAG(), "Fehler bei Factory init", e);
		}
		setContentView(R.layout.activity_item_list);

		if (findViewById(R.id.item_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mPaneCount++;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((ItemListFragment) getSupportFragmentManager().findFragmentById(
					R.id.item_list)).setActivateOnItemClick(true);
		}

		if (findViewById(R.id.item_person_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in three-pane mode.
			mPaneCount++;
		}
		
		// TODO: If exposing deep links into your app, handle intents here.
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onNavigateBack();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(MenuItem menuItem) {

		String personListModeName = PersonListController.PERSON_LIST_MODE.NORMAL.name();
		
		if(menuItem == MenuItem.PersonAttendance)
			personListModeName = PersonListController.PERSON_LIST_MODE.ATTENDANCE.name();
		
		Bundle arguments;
		
		PersonListFragment fragment;
		
		switch (mPaneCount) {
		case 1:
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, PersonListActivity.class);
			detailIntent.putExtra(PersonListFragment.ActivateOnItemClick, false);
			detailIntent.putExtra(PersonListController.PERSON_LIST_MODE.class.getName(), personListModeName);
			startActivity(detailIntent);
			break;
		case 2:

			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			arguments = new Bundle();
			arguments.putBoolean(PersonListFragment.ActivateOnItemClick, false);
			arguments.putString(PersonListController.PERSON_LIST_MODE.class.getName(), personListModeName);
			
			fragment = new PersonListFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.item_detail_container, fragment)
					.addToBackStack(null)
					.commit();
			break;
		case 3:
			arguments = new Bundle();
			arguments.putBoolean(PersonListFragment.ActivateOnItemClick, true);
			arguments.putString(PersonListController.PERSON_LIST_MODE.class.getName(), personListModeName);
			
			fragment = new PersonListFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.item_person_container, fragment)
					.addToBackStack(null)
					.commit();
			break;
		default:
			break;
		}
	}

	@Override
	public void onPersonSelected(Person person) {
		if (mPaneCount>1){
			Bundle arguments = new Bundle();
			
			if(person != null)
				arguments.putLong(PersonDetailController.ARG_PERSON_ID, person.getId());
			
			Fragment fragment = new PersonDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.item_detail_container, fragment)
					.addToBackStack(null)
					.commit();
		} else {

			Intent detailIntent = new Intent(this, PersonDetailActivity.class);
			detailIntent.putExtra(PersonDetailController.ARG_PERSON_ID, person.getId());
			startActivity(detailIntent);
		}
	}

	@Override
	public void onNavigateBack() {
		if(mPaneCount>1)
			getSupportFragmentManager().popBackStack();
		else
			onBackPressed();
	}

}
