package gov.alaska.gmc_handheld_v2_simpleJSON;

import java.util.ArrayList;

public class LookupHistoryHolder {
// https://stackoverflow.com/a/51344957
	final ArrayList<String> lookupHistory = new ArrayList<>();

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
