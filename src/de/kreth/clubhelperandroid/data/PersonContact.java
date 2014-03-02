package de.kreth.clubhelperandroid.data;

import java.util.Locale;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class PersonContact extends PersistentDataObject {
	private long personId;
	private ContactType type;
	private String value;

	public PersonContact(long personId, ContactType type, String value) {
		super();
		this.personId = personId;
		this.type = type;
		initValue(value);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": id=" + getId() + ", personId=" + getPersonId() + ", type=" + getType() + ", value=" + getValue();
	}
	
	public PersonContact(long id, int personId, ContactType type, String value) {
		super(id);
		this.personId = personId;
		this.type = type;
		initValue(value);
	}

	private void initValue(String value2) {
		if(type == ContactType.MOBILE || type == ContactType.TELEPHONE)
			this.value = value2.replaceAll("[^\\d.]", "");
		else
			this.value = value2;
	}

	public long getPersonId() {
		return personId;
	}

	public ContactType getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public String getFormattedValue(){
		if(type == ContactType.MOBILE || type == ContactType.TELEPHONE)
		try {
			PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
			PhoneNumber phone = phoneNumberUtil.parse(getValue(), Locale.getDefault().getCountry());
			return phoneNumberUtil.format(phone, PhoneNumberFormat.NATIONAL);
		} catch (NumberParseException e) {
			return value;
		}
		return value;		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersonContact other = (PersonContact) obj;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
