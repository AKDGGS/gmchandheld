package gov.alaska.gmc_handheld_v2_simpleJSON;

import android.text.SpannableString;

import java.util.LinkedList;

public class LookupHistoryHolder {
	// https://stackoverflow.com/a/51344957
	final LinkedList<SpannableString> lookupHistory = new LinkedList<>();

	private LookupHistoryHolder() {
	}

	static LookupHistoryHolder getInstance() {
		if (instance == null) {
			instance = new LookupHistoryHolder();
		}
		return instance;
	}

	private static LookupHistoryHolder instance;

}

