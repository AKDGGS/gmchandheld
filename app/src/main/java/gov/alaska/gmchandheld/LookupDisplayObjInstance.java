package gov.alaska.gmchandheld;

import java.util.LinkedList;

public class LookupDisplayObjInstance {
	// https://stackoverflow.com/a/19620252
	private static LookupDisplayObjInstance obj = null;
	private final LinkedList<String> lookupHistory = new LinkedList<>();
	public LinkedList<String> getLookupHistory() {
		return lookupHistory;
	}
	public static LookupDisplayObjInstance getInstance()
	{
		if (obj == null)
			obj = new LookupDisplayObjInstance();
		return obj;
	}
	public LookupLogicForDisplay lookupLogicForDisplayObj;
}