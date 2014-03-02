package de.kreth.clubhelperandroid.adapter;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import de.kreth.clubhelperandroid.R;
import de.kreth.clubhelperandroid.data.Attendance;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class PersonAttendenceListAdapter extends ArrayAdapter<Attendance> {

	private Calendar date;
	private OnCheckedChangeListener checkedChangeListener;
	
	public PersonAttendenceListAdapter(Context context, List<Attendance> objects, OnCheckedChangeListener checkedChangeListener) {
		super(context, R.layout.person_attendance_item, objects);
		date = new GregorianCalendar();
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
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
			holder.attendent.setOnCheckedChangeListener(checkedChangeListener);
			attItem.setTag(holder);
		} else{
			attItem = convertView;
			holder = (AttendanceHolder) attItem.getTag();
		}
		
		Attendance item = getItem(position);
		holder.personname.setText(item.getPerson().toString());
		holder.attendent.setChecked(false);
		holder.attendent.setId(position);
		
		if(item.getDate() != null && item.getDate().compareTo(date) == 0)
			holder.attendent.setChecked(true);
			
		return attItem;
	}
	
	private static class AttendanceHolder {
		TextView personname;
		CheckBox attendent;
	}
}
