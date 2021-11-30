package gov.alaska.gmchandheld;

import java.util.Date;

public interface HTTPRequestCallback {
    void displayData(byte[] byteData,
                     Date date,
                     int responseCode,
                     String responseMessage,
                     int requestType);

    void displayException(Exception e);
}
