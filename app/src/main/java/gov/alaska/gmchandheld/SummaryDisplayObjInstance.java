package gov.alaska.gmchandheld;

import java.util.LinkedList;

public class SummaryDisplayObjInstance {
    // https://stackoverflow.com/a/19620252
    static SummaryDisplayObjInstance obj = null;
    private final LinkedList<String> summaryHistory = new LinkedList<>();
    public SummaryLogicForDisplay summaryLogicForDisplayObj;

    public static SummaryDisplayObjInstance getInstance() {
        if (obj == null)
            obj = new SummaryDisplayObjInstance();
        return obj;
    }

    public LinkedList<String> getSummaryHistory() {
        return summaryHistory;
    }
}
