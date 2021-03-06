package de.kreth.clubhelperandroid.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import de.kreth.clubhelperandroid.R;
import de.kreth.clubhelperbusiness.data.Attendance;

public class PersonAttendenceListAdapter extends ArrayAdapter<Attendance> {

	private Calendar date;
	private OnCheckedChangeListener checkedChangeListener = null;
	private OnCheckedChangeListener internalCheckChangeListener;
	
	public PersonAttendenceListAdapter(Context context, final List<Attendance> objects) {
		super(context, R.layout.person_attendance_item, objects);
		date = new GregorianCalendar();
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		
		internalCheckChangeListener = new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(checkedChangeListener != null) {
					executeAttendanceReplacement(((Long)buttonView.getTag()).longValue(), isChecked);
					checkedChangeListener.onCheckedChanged(buttonView, isChecked);
				} else {
					buttonView.setChecked(!isChecked);
				}
			}

			private void executeAttendanceReplacement(final long personId, final boolean isChecked) {
				ExecutorService exec = Executors.newSingleThreadExecutor();
				exec.execute(new Runnable() {
					
					@Override
					public void run() {
						List<Attendance> snapshot;
						synchronized (objects) {
							snapshot = new ArrayList<Attendance>(objects);							
						}
						for(int i=0; i<snapshot.size();i++){
							Attendance att = snapshot.get(i);
							if(att.getPerson().getId()==personId) {
								Calendar d;
								if(isChecked){
									d=date;
								} else
									d = null;
								att = new Attendance(att.getPerson(), d);
								synchronized (objects) {
									objects.remove(i);
									objects.add(i, att);
								}
							}
						}
						
					}
				});
			}
		};
	}

	public void setCheckedChangeListener(
			OnCheckedChangeListener checkedChangeListener) {
		this.checkedChangeListener = checkedChangeListener;
	}
	
	public void setDate(Calendar date) {
		this.date = date;
		this.notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View attItem;
		AttendanceHolder holder;
		
		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(getContext());
			attItem = inflater.inflate(R.layout.person_attendance_item, parent, false);
			holder = new AttendanceHolder();
			holder.personname = (TextView) attItem.findViewById(R.id.textView1);
			holder.attendent = (CheckBox) attItem.findViewById(R.id.checkBox1);
			holder.attendent.setOnCheckedChangeListener(internalCheckChangeListener);
			attItem.setTag(holder);
		} else{
			attItem = convertView;
			holder = (AttendanceHolder) attItem.getTag();
		}
		
		Attendance item = getItem(position);
		holder.personname.setText(item.getPerson().toString());
		holder.attendent.setTag(item.getPerson().getId());
		
		if(item.getDate() != null && item.getDate().compareTo(date) == 0) {
			if( ! holder.attendent.isChecked())
				holder.attendent.setChecked(true);
		} else {
			if(holder.attendent.isChecked())
				holder.attendent.setChecked(false);
		}
			
		return attItem;
	}
	
	private class AttendanceHolder {
		public TextView personname;
		public CheckBox attendent;
	}
}
