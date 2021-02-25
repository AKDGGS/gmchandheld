package gov.alaska.gmchandheld;

import java.util.LinkedList;

public class AddContainerHistoryHolder {
	// https://stackoverflow.com/a/51344957
	private final LinkedList<String> addContainerHistoryList = new LinkedList<>();

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