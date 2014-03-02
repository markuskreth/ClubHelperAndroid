package de.kreth.clubhelperandroid.ui.fragments;


import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.COLUMN_PERSON_PRENAME;
import static de.kreth.mtvtraininghelper2.database.MtvSqLiteOpenHelper.COLUMN_PERSON_SURNAME;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import de.kreth.clubhelperandroid.Factory;
import de.kreth.clubhelperandroid.adapter.PersonAttendenceListAdapter;
import de.kreth.clubhelperandroid.adapter.PersonListAdapter;
import de.kreth.clubhelperandroid.data.Attendance;
import de.kreth.clubhelperandroid.data.Person;
import de.kreth.clubhelperandroid.data.PersonPersisterImpl;
import de.kreth.clubhelperandroid.ui.utils.FragmentNavigator;
import de.kreth.clubhelperandroid.R;

/**
 * A list fragment representing a list of Personen. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link PersonDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class PersonListFragment extends Fragment implements OnCheckedChangeListener {

	public static final String ActivateOnItemClick = "ActivateOnItemClick to Set";
	public enum PERSON_LIST_MODE {
		NORMAL,
		ATTENDANCE
	}
	
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	private PersonListAdapter adapterNormal;

	private PersonPersisterImpl persister;

	private EditText filter;

	private ViewGroup buttonBarDropTarget;

	private Activity activity;

	private View rootView;

	private ListView listView;

	private PERSON_LIST_MODE mode = PERSON_LIST_MODE.NORMAL;
	private Calendar attendenceDate;

	private PersonAttendenceListAdapter adapterAttendence;

	private List<Attendance> attendancesOriginal;

	private TextView txtTitle;
	
	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks extends FragmentNavigator {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onPersonSelected(Person person);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onPersonSelected(Person person) {}

		@Override
		public void onNavigateBack() {}

	};

	public void onCreateOptionsMenu(Menu menu, android.view.MenuInflater inflater) {
        inflater.inflate(R.menu.data_view, menu);
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_person_list, null);
		Bundle arguments = getArguments();
		
		if(arguments != null) {
			String modeName = arguments.getString(PersonListFragment.PERSON_LIST_MODE.class.getName());
			if(modeName != null && ! modeName.isEmpty()){
				this.mode = PERSON_LIST_MODE.valueOf(modeName);
			}
		}
		attendenceDate = new GregorianCalendar();
		attendenceDate.set(Calendar.HOUR_OF_DAY, 0);
		attendenceDate.set(Calendar.MINUTE, 0);
		attendenceDate.set(Calendar.SECOND, 0);
		attendenceDate.set(Calendar.MILLISECOND, 0);
		recreate();
		return rootView;
	}
	
	public void setMode(PERSON_LIST_MODE mode) {
		this.mode = mode;
		recreate();
	}
	
	private void recreate() {
		initComponents();
		setupAdapter();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_addItem:
			mCallbacks.onPersonSelected(null);
			break;

		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void filterPerson(String filter ) {
		String whereClause;
		if(filter == null || filter.trim().isEmpty()){
			whereClause = null;			
		} else {
			whereClause = COLUMN_PERSON_PRENAME + " like '" + filter + "%' OR " + COLUMN_PERSON_SURNAME + " like '" + filter + "%'";
		}
		if(mode == PERSON_LIST_MODE.NORMAL) {
			List<Person> personsWhere = persister.getPersonsWhere(whereClause);
			adapterNormal.clear();
			adapterNormal.addAll(personsWhere);
		} else if (mode == PERSON_LIST_MODE.ATTENDANCE) {
			adapterAttendence.clear();
			attendancesOriginal = persister.getAttendancesWhere(whereClause, this.attendenceDate);
			adapterAttendence.setDate(attendenceDate);
			adapterAttendence.addAll(attendancesOriginal);
		}
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PersonListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    setHasOptionsMenu(true);
		
		persister = new PersonPersisterImpl(Factory.getInstance().getDatabase());
	}

	private void initComponents() {
		
		filter = (EditText) rootView.findViewById(R.id.txtFilter);

		filter.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				refreshList();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		buttonBarDropTarget = (ViewGroup) rootView.findViewById(R.id.buttonBarDropArea);
		buttonBarDropTarget.setVisibility(View.GONE);
		buttonBarDropTarget.findViewById(R.id.btnItemDelete).setOnDragListener(new ObjectDragListener());

		listView = (ListView) rootView.findViewById(R.id.listView1);

		txtTitle = (TextView) rootView.findViewById(R.id.textTitle);
		
		if(this.mode != PERSON_LIST_MODE.ATTENDANCE) {
			listView.setOnItemClickListener(new OnItemClickListener() {
	
				@Override
				public void onItemClick(AdapterView<?> arg0, View view, int position,
						long id) {
					
					mCallbacks.onPersonSelected(adapterNormal.getItem(position));	
				}
			});
		} else {
			txtTitle.setText(R.string.title_activity_attendance);
		}
		Bundle arguments = getArguments();
		if(arguments != null) {
			boolean activateOnItemClick = arguments.getBoolean(ActivateOnItemClick, false);
			setActivateOnItemClick(activateOnItemClick);
		}
	}

	private void setupAdapter() {
		switch (mode) {
		case ATTENDANCE:
			attendancesOriginal = persister.getAttendancesWhere("", this.attendenceDate);
			adapterAttendence = new PersonAttendenceListAdapter(getActivity(), attendancesOriginal, this);
			listView.setAdapter(adapterAttendence);
			break;
		case NORMAL:
		default:
			adapterNormal = new PersonListAdapter(getActivity(), persister.getAllPersons());
			listView.setAdapter(adapterNormal);
			ListItemDragOnLongClickListener longClickListener = new ListItemDragOnLongClickListener();
			listView.setOnItemLongClickListener(longClickListener);
			listView.setLongClickable(true);
			break;
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
		
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onResume() {
		super.onResume();
		if(mode == PERSON_LIST_MODE.ATTENDANCE){
			showAttendanceDateDialog();
		}
	}

	private void showAttendanceDateDialog() {
		
		int year = attendenceDate.get(Calendar.YEAR);
		int monthOfYear = attendenceDate.get(Calendar.MONTH);
		int dayOfMonth = attendenceDate.get(Calendar.DAY_OF_MONTH);
		DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
				createDateSetListener(), year, monthOfYear, dayOfMonth);
		datePickerDialog.show();
	}

	private OnDateSetListener createDateSetListener() {
		OnDateSetListener listener = new OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				attendenceDate.set(year, monthOfYear, dayOfMonth);
				
				String title = getActivity().getString(R.string.title_activity_attendance) + " " + DateFormat.getDateInstance().format(attendenceDate.getTime());
				txtTitle.setText(title);
				refreshList();
			}
		};
		return listener ;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
		
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	private void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		listView.setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			listView.setItemChecked(mActivatedPosition, false);
		} else {
			listView.setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	public void refreshList(){
		filterPerson(PersonListFragment.this.filter.getText().toString());
	}

	public class ListItemDragOnLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View childView,
				int position, long id) {
			Person person = adapterNormal.getItem(position);

			String text = person.toString();
			ClipData.Item item = new ClipData.Item(text);
			String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
			ClipData data = new ClipData(text, mimeTypes, item);
			DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(childView);

			childView.startDrag(data, shadowBuilder, childView, 0);
			childView.setVisibility(View.INVISIBLE);
			filter.setVisibility(View.GONE);

			buttonBarDropTarget.setVisibility(View.VISIBLE);
			return true;
		}
		
	}
	

	private void askUserForDelete(final Person pers, final View draggedView) {
		
		AlertDialog.OnClickListener listener = new AlertDialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == AlertDialog.BUTTON_POSITIVE){
					persister.deletePerson(pers);
					refreshList();
				}
			}
		};
		
		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(activity);
		dlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		String msg = getResources().getString(R.string.confirm_delete, pers);
		dlgBuilder.setMessage(msg);
		dlgBuilder.setPositiveButton(R.string.label_yes, listener);
		dlgBuilder.setNegativeButton(R.string.label_no, listener);
		dlgBuilder.show();
	}

	private class ObjectDragListener implements OnDragListener {

		Drawable normalShape = getResources().getDrawable(R.drawable.normal_shape);
		Drawable targetShape = getResources().getDrawable(R.drawable.target_shape);
		long lastDrop = -1;
		
		@Override
		public boolean onDrag(final View v, final DragEvent event) {
			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_ENTERED:
				v.setBackground(targetShape);
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				v.setBackground(normalShape);
				break;
			case DragEvent.ACTION_DRAG_ENDED:
				if(isLastDropIsLongAgo()){
					activity.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							v.setBackground(normalShape);
							View view = (View)event.getLocalState();
							view.setVisibility(View.VISIBLE);
							filter.setVisibility(View.VISIBLE);
							buttonBarDropTarget.setVisibility(View.GONE);
						}
					});
				} else {
					System.out.println("=========Drop Skipped!!!!!");
				}
				break;
			case DragEvent.ACTION_DROP:

				final View draggedView = (View)event.getLocalState();
				
				int pos = listView.getPositionForView(draggedView);
				
				final Person person = (Person) adapterNormal.getItem(pos);
				
				if(v.getId()==R.id.btnItemDelete){
					activity.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							askUserForDelete(person, draggedView);
						}
					});
				} 
				break;
			}
			return true;
		}

		private boolean isLastDropIsLongAgo() {
			long now = System.currentTimeMillis();
			long waitTime = now - lastDrop;
			boolean retVal = false;
			
			if(lastDrop<0 || waitTime>500){
				retVal = true;
				lastDrop = now;
			}
			
			return retVal;
		}
		
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int position = buttonView.getId();
		Attendance attendance = attendancesOriginal.remove(position);
		Attendance toStore;
		if(isChecked) {
			toStore = persister.storeAttendance(attendance, this.attendenceDate);
		} else {
			toStore = persister.storeAttendance(attendance, this.attendenceDate);
			
		}
		attendancesOriginal.add(toStore);
		
	}

}
