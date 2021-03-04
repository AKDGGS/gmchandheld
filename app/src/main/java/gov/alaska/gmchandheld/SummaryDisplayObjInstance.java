package gov.alaska.gmchandheld;

public class SummaryDisplayObjInstance {
    // https://stackoverflow.com/a/19620252
    static SummaryDisplayObjInstance obj = null;

    public static SummaryDisplayObjInstance instance() {
        if (obj == null)
            obj = new SummaryDisplayObjInstance();
        return obj;
    }

    public SummaryLogicForDisplay summaryLogicForDisplayObj;
}
