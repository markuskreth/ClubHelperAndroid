package de.kreth.mtvandroidhelper2.ui.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.kreth.mtvandroidhelper2.R;
import de.kreth.mtvandroidhelper2.data.PersonContact;

public class DialogDoConnection {
	private PersonContact contact;

	public DialogDoConnection(PersonContact contact) {
		super();
		this.contact = contact;
	}

	public void showDialog(final Activity activity) {

		int tmpTitleId = -1;
		
		switch(contact.getType()){
		case EMAIL:
			tmpTitleId = R.string.title_connect_email;
			break;
		case MOBILE:
			tmpTitleId = R.string.title_connect_mobile;
			break;
		case TELEPHONE:
			tmpTitleId = R.string.title_connect_telephone;
			break;
		default:
			break;
			
		}

		final View layout = createLayout(activity);
		
		final String titleId = activity.getString(tmpTitleId) + contact.getFormattedValue();
		
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				AlertDialog.Builder bld = new AlertDialog.Builder(activity);
				bld.setTitle(titleId);
				bld.setView(layout);
				bld.setNegativeButton(R.string.label_cancel, null);
				bld.create().show();
			}
		});
	}

	private View createLayout(final Activity activity) {
		LinearLayout linearLayout = new LinearLayout(activity);

		int left = 20;
		int top = 20;
		int right = 20;
		int bottom = 20;
		
		switch(contact.getType()){
		case EMAIL:
			ImageButton email = new ImageButton(activity);
			email.setImageResource(android.R.drawable.sym_action_email);
			email.setPadding(left, top, right, bottom);
			email.setOnClickListener(new ImageButton.OnClickListener(){

				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("message/rfc822");
					i.putExtra(Intent.EXTRA_EMAIL  , new String[]{contact.getValue()});
					i.putExtra(Intent.EXTRA_SUBJECT, "");
					i.putExtra(Intent.EXTRA_TEXT   , "\n\nMarkus Kreth\nTrainer Trampolinturnen - MTV Groß-Buchholz");
					try {
						activity.startActivity(Intent.createChooser(i, "Send mail..."));
					} catch (android.content.ActivityNotFoundException ex) {
					    Toast.makeText(activity, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
					}
				}
				
			});
			linearLayout.addView(email);
			break;
		case MOBILE:
			ImageButton sms = new ImageButton(activity);
			sms.setImageResource(android.R.drawable.sym_action_chat);
			sms.setPadding(left, top, right, bottom);
			sms.setOnClickListener(new ImageButton.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent smsIntent = new Intent(Intent.ACTION_VIEW);
			        smsIntent.putExtra("sms_body", ""); 
			        smsIntent.putExtra("address", contact.getValue());
			        smsIntent.setType("vnd.android-dir/mms-sms");

			        activity.startActivity(Intent.createChooser(smsIntent, activity.getText(R.string.title_connect_sms)));
				}
			});
			linearLayout.addView(sms);
//			break;	// Handy hat außerdem die Möglichkeiten von TELEPHONE 
		case TELEPHONE:
			ImageButton phoneCall = new ImageButton(activity);
			phoneCall.setImageResource(android.R.drawable.sym_action_call);
			phoneCall.setPadding(left, top, right, bottom);
			phoneCall.setOnClickListener(new ImageButton.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String url = "tel:" + contact.getValue();
				    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
			        activity.startActivity(intent);
				}
			});
			linearLayout.addView(phoneCall);
			break;
		default:
			break;
			
		}
		return linearLayout;
	}
	
}
