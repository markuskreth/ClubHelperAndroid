package de.kreth.clubhelperandroid.ui;

import java.util.Locale;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import de.kreth.clubhelperandroid.ui.fragments.PersonDetailFragment;
import de.kreth.clubhelperandroid.ui.utils.FragmentNavigator;
import de.kreth.clubhelperandroid.R;

/**
 * An activity representing a single Person detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link PersonListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link PersonDetailFragment}.
 */
public class PersonDetailActivity extends FragmentActivity implements FragmentNavigator {

	private PersonDetailFragment fragment;
	
	private PhoneNumberUtil phoneNumberUtil;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_person_detail);
		
		setupActionBar();
		
		Locale locale = Locale.getDefault();
		phoneNumberUtil = PhoneNumberUtil.getInstance();
		PhoneNumber exampleNumber = phoneNumberUtil.getExampleNumberForType(locale.getCountry(), PhoneNumberType.FIXED_LINE);
		
		System.out.println(phoneNumberUtil.format(exampleNumber, PhoneNumberFormat.NATIONAL));
		System.out.println(exampleNumber.toString());
		exampleNumber = phoneNumberUtil.getExampleNumberForType(locale.getCountry(), PhoneNumberType.MOBILE);
		System.out.println(phoneNumberUtil.format(exampleNumber, PhoneNumberFormat.NATIONAL));
		System.out.println(exampleNumber.toString());
		
		if (savedInstanceState == null) {

			Intent intent = getIntent();
			long personId = intent.getLongExtra(PersonDetailFragment.ARG_PERSON_ID, -1);
			Bundle args = new Bundle();
			args.putLong(PersonDetailFragment.ARG_PERSON_ID, personId);
			
			fragment = new PersonDetailFragment();
			fragment.setArguments(args);
			
			getSupportFragmentManager().beginTransaction()
					.add(R.id.person_detail_container, fragment).commit();
		} else {
			fragment = (PersonDetailFragment) getSupportFragmentManager().findFragmentById(R.id.person_detail_container);
		}

	}

	private void setupActionBar() {

		// Show the Up button in the action bar.
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
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
