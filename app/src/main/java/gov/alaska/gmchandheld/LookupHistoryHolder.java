package gov.alaska.gmchandheld;

import java.util.LinkedList;

public class LookupHistoryHolder {
	// https://stackoverflow.com/a/51344957
	private final LinkedList<String> lookupHistory = new LinkedList<>();

	public LinkedList<String> getLookupHistory() {
		return lookupHistory;
	}

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
