package de.kreth.clubhelperandroid.data;

public interface DataChangeHandler<T> {

	T getData();
	void storeData(T data);
	void deleteData(T data);
	void dataHasChanged();
	void cancelEdit();
}
