package gov.alaska.gmc_handheld_v2_simpleJSON;

public class LookupDisplayObjInstance {
	// https://stackoverflow.com/a/19620252
	static LookupDisplayObjInstance obj = null;
	public static LookupDisplayObjInstance instance()
	{
		if (obj == null)
			obj = new LookupDisplayObjInstance();
		return obj;
	}

	public LookupLogicForDisplay lookupLogicForDisplayObj;

}