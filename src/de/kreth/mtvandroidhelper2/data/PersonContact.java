package de.kreth.mtvandroidhelper2.data;

public class PersonContact extends PersistentDataObject {
	private int personId;
	private ContactType type;
	private String value;

	public PersonContact(int personId, ContactType type, String value) {
		super();
		this.personId = personId;
		this.type = type;
		this.value = value;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": id=" + getId() + ", personId=" + getPersonId() + ", type=" + getType() + ", value=" + getValue();
	}
	
	public PersonContact(int id, int personId, ContactType type, String value) {
		super(id);
		this.personId = personId;
		this.type = type;
		this.value = value;
	}

	public int getPersonId() {
		return personId;
	}

	public ContactType getType() {
		return type;
	}

	public String getValue() {
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
