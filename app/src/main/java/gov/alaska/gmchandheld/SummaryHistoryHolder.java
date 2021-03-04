package gov.alaska.gmchandheld;

import java.util.LinkedList;

public class SummaryHistoryHolder {    // https://stackoverflow.com/a/51344957
    private final LinkedList<String> summaryHistory = new LinkedList<>();

    public LinkedList<String> getSummaryHistory() {
        return summaryHistory;
    }

    private SummaryHistoryHolder() {
    }

    static SummaryHistoryHolder getInstance() {
        if (instance == null) {
            instance = new SummaryHistoryHolder();
        }
        return instance;
    }

    private static SummaryHistoryHolder instance;
}