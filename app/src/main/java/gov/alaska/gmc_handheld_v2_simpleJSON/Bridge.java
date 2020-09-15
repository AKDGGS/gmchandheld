package gov.alaska.gmc_handheld_v2_simpleJSON;

public class Bridge {
	// https://stackoverflow.com/a/19620252
	static Bridge obj = null;
	public static Bridge instance()
	{
		if (obj == null)
			obj = new Bridge();
		return obj;
	}

	public LookupLogicForDisplay lookupLogicForDisplayObj;

}