package de.kreth.mtvandroidhelper2.data;

public abstract class PersistentDataObject {

	private static int nextUnpersistentId = -1;
	private int _id;

	public PersistentDataObject() {
		super();
		_id = nextUnpersistentId--;
	}
	
	public PersistentDataObject(int id) {
		this();
		if(id <0)
			throw new IllegalArgumentException("Dieser Konstruktor ist nur für persistente Objekte - der Primary Key darf daher nicht <0 sein! War aber " + id);
		this._id = id;
	}

	public int getId() {
		return _id;
	}

	void setId(int id) {
		if(id <0)
			throw new IllegalArgumentException("der Primary Key darf nicht <0 sein! War aber " + id);
		this._id = id;
	}
	
	/**
	 * Ist Objekt in Db gespeichert?
	 * @return true, wenn in Db vorhanden.
	 */
	public boolean isPersistent(){
		return _id >= 0;
	}
}
