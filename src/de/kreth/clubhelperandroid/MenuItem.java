package de.kreth.clubhelperandroid;

import de.kreth.clubhelperandroid.R;

public enum MenuItem {
	PersonList(R.string.label_menu_personanwesenheit),
	PersonContacts(R.string.label_menu_personcontacts);
	
	private int resourceId;
	
	private MenuItem(int resourceId) {
		this.resourceId = resourceId;
	}
	
	public String toString() {
		return Factory.getInstance().getString(resourceId);
	};
}
