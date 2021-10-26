package gov.alaska.gmchandheld;

public interface RemoteAPIDownloadCallback {
    void displayData(String data,
                     int responseCode,
                     String responseMessage,
                     int requestType);

    void displayException(Exception e);
}
