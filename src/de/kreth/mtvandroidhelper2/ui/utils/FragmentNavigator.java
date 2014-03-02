package de.kreth.mtvandroidhelper2.ui.utils;

public interface FragmentNavigator {
	void onNavigateBack();
	/**
	 * Leerer Navigator
	 */
	public static FragmentNavigator DUMMY = new FragmentNavigator() {
		
		@Override
		public void onNavigateBack() {}
	};
}
