package gov.alaska.gmchandheld;

public interface RemoteAPIDownloadCallback {
    void displayData(String data, int responseCode, String responseMessage);
}
