package gov.alaska.gmc_handheld_v2_simpleJSON;

import java.util.LinkedList;

public class AddContainerHistoryHolder {
	// https://stackoverflow.com/a/51344957
	private LinkedList<String> addContainerHistoryList = new LinkedList<>();

	public LinkedList<String> getAddContainerHistory() {
		return addContainerHistoryList;
	}

	private AddContainerHistoryHolder() {
	}

	static AddContainerHistoryHolder getInstance() {
		if (instance == null) {
			instance = new AddContainerHistoryHolder();
		}
		return instance;
	}

	private static AddContainerHistoryHolder instance;
}
