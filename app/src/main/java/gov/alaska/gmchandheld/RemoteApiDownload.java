package gov.alaska.gmchandheld;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class RemoteApiDownload {
    private Exception exception = null;
    private StringBuilder sb;
    private int responseCode;
    private String responseMsg;
    private String rawJson;
    private String urlFirstParameter;
    private String addedContainerName;
    private String addedContainerRemark;
    private ArrayList<String> containerList;
    private String newBarcode;
    private String destinationBarcode;
    private final Context context;

    SimpleDateFormat sdf;

//	public static final String SHARED_PREFS = "sharedPrefs";

    @SuppressLint("SimpleDateFormat")
    public RemoteApiDownload(Context context) {
        this.context = context;
        sdf = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss zzz"
        );
    }

    public void setUrlFirstParameter(String firstQueryParameter) {
        this.urlFirstParameter = firstQueryParameter;
    }

    public void setContainerList(ArrayList<String> containerList) {
        this.containerList = containerList;
    }

    public void setAddedContainerName(String addedContainerName) {
        this.addedContainerName = addedContainerName;
    }

    public void setAddedContainerRemark(String addedContainerRemark) {
        this.addedContainerRemark = addedContainerRemark;
    }

    public void setDestinationBarcode(String destinationBarcode) {
        this.destinationBarcode = destinationBarcode;
    }


    public void setNewBarcode(String newBarcode) {
        this.newBarcode = newBarcode;
    }

    public boolean isErrored() {
        return exception != null;
    }

    public Exception getException() {
        return exception;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public String getRawJson() {
        return rawJson;
    }

    public void getDataFromURL() {

        InputStream inputStream;
        HttpURLConnection connection;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Configuration.SHARED_PREFS, Context.MODE_PRIVATE);
        String url = sharedPreferences.getString("urlText", "");

        try {
            Date date = new Date();
            String HDATE = getDateFormat().format(date);

            String QUERYPARAM = null;

            switch (context.getClass().getSimpleName()) {
                case "Lookup":
                case "LookupDisplay": {
                    String barcode = null;
                    try {
                        barcode = URLEncoder.encode(urlFirstParameter, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        exception = new Exception(e.getMessage());
                    }
                    QUERYPARAM = "barcode=" + barcode;
                    url = url + "inventory.json?barcode=" + urlFirstParameter;
                    break;
                }
                case "Summary":
                case "SummaryDisplay": {
                    String barcode = null;
                    try {
                        barcode = URLEncoder.encode(urlFirstParameter, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        exception = new Exception(e.getMessage());
                    }
                    QUERYPARAM = "barcode=" + barcode;
                    url = url + "summary.json?barcode=" + urlFirstParameter;
                    break;
                }
                case "MoveContents": {
                    String source = null;
                    String destination = null;

                    try {
                        source = URLEncoder.encode(urlFirstParameter, "utf-8");
                        destination = URLEncoder.encode(destinationBarcode, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        exception = new Exception(e.getMessage());
                    }

                    StringBuilder sb = new StringBuilder();
                    if (source != null) {
                        sb.append("src=").append(source);
                    }
                    if (destination != null) {
                        sb.append("&dest=").append(destination);
                    }

                    QUERYPARAM = sb.toString();
                    url = url + "movecontents.json?" + sb.toString();
                    break;
                }
                case "MoveDisplay": {
                    String query;
                    String destination = null;
                    try {
                        destination = URLEncoder.encode(urlFirstParameter, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        exception = new Exception(e.getMessage());
                    }
                    query = "d=" + destination + containersToUrlList(containerList, "c");

                    QUERYPARAM = query;
                    url = url + "move.json?" + query;
                    break;
                }

                case "AddContainer": {
                    String barcode = null;
                    String name = null;
                    String remark = null;
                    try {
                        barcode = URLEncoder.encode(urlFirstParameter, "utf-8");
                        name = URLEncoder.encode(addedContainerName, "utf-8");
                        remark = URLEncoder.encode(addedContainerRemark, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        exception = new Exception(e.getMessage());
                    }

                    StringBuilder sb = new StringBuilder();
                    if (barcode != null) {
                        sb.append("barcode=").append(barcode);
                    }
                    if (name != null) {
                        sb.append("&name=").append(name);
                    }
                    if (remark != null) {
                        sb.append("&remark=").append(remark);
                    }

                    QUERYPARAM = sb.toString();
                    url = url + "addcontainer.json?" + sb.toString();
                    break;
                }

                case "AddInventory": {
                    String barcode = null;
                    String remark = null;
                    try {
                        barcode = URLEncoder.encode(urlFirstParameter, "utf-8");
                        if (addedContainerRemark != null) {
                            remark = URLEncoder.encode(addedContainerRemark, "utf-8");
                        }
                    } catch (UnsupportedEncodingException e) {
                        exception = new Exception(e.getMessage());
                    }

                    StringBuilder sb = new StringBuilder();
                    if (barcode != null) {
                        sb.append("barcode=").append(barcode);
                    }

                    if (remark != null) {
                        sb.append("&remark=").append(remark);
                    }

                    if (containerList != null) {
                        sb.append(containersToUrlList(containerList, "i"));
                    }

                    QUERYPARAM = sb.toString();
                    url = url + "addinventory.json?" + sb.toString();
                    break;
                }

                case "Quality": {
                    String query;
                    String barcode = null;
                    String remark = null;
                    try {
                        barcode = URLEncoder.encode(urlFirstParameter, "utf-8");
                        if (remark != null) {
                            remark = URLEncoder.encode(addedContainerRemark, "utf-8");
                        }
                    } catch (UnsupportedEncodingException e) {
                        exception = new Exception(e.getMessage());
                    }

                    StringBuilder sb = new StringBuilder();
                    if (barcode != null) {
                        sb.append("barcode=").append(barcode);
                    }
                    if (remark != null) {
                        sb.append("&remark=").append(remark);
                    }
                    if (containerList != null) {
                        sb.append(containersToUrlList(containerList, "i"));
                    }
                    query = sb.toString();
                    QUERYPARAM = query;
                    url = url + "addinventoryquality.json?" + query;
                    break;
                }

                case "AuditDisplay": {
                    String query;
                    String remark = null;
                    try {
                        remark = URLEncoder.encode(urlFirstParameter, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        exception = new Exception(e.getMessage());
                    }
                    query = "remark=" + remark + containersToUrlList(containerList, "c");

                    QUERYPARAM = query;
                    url = url + "audit.json?" + query;
                    break;
                }

                case "Recode": {
                    String barcode = null;
                    String mNewBarcode = null;

                    try {
                        barcode = URLEncoder.encode(urlFirstParameter, "utf-8");
                        mNewBarcode = URLEncoder.encode(newBarcode, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        exception = new Exception(e.getMessage());
                    }

                    StringBuilder sb = new StringBuilder();
                    if (barcode != null) {
                        sb.append("old=").append(barcode);
                    }
                    if (mNewBarcode != null) {
                        sb.append("&new=").append(mNewBarcode);
                    }

                    QUERYPARAM = sb.toString();
                    url = url + "recode.json?" + sb.toString();
                    break;
                }
            }

            URL myURL = new URL(url);

            connection = (HttpURLConnection) myURL.openConnection();
            connection.setRequestMethod("GET");

            sharedPreferences = context.getSharedPreferences(Configuration.SHARED_PREFS, Context.MODE_PRIVATE);
            String accessToken = sharedPreferences.getString("apiText", "");

//            String accessToken = "6Ve0DF0rRLH0RDDomchEdkCwU83prZbAEWqb27q9fs34o4zSisV6rgXSU3iLato9OlW6eXPBKyzj2x1OvMbv7WhANMKKjGgmJlNAkKQvR2s0SMmGN26m6hr3pbXp49NG";
            connection.setRequestProperty("Authorization", "Token " + accessToken);

            connection.setReadTimeout(10 * 1000);
            connection.setConnectTimeout(5 * 1000);

            connection.connect();

            responseCode = connection.getResponseCode();
            responseMsg = connection.getResponseMessage();


            try {
                inputStream = connection.getInputStream();
            } catch (IOException e) {
                inputStream = connection.getErrorStream();
            }

            try {
                StringBuilder sb = new StringBuilder();
                byte[] buffer = new byte[4096];
                int buffer_read = 0;

                while (buffer_read != -1) {
                    buffer_read = inputStream.read(buffer);
                    if (buffer_read > 0) {
                        sb.append(new String(buffer, 0, buffer_read));
                    }
                }

                if (connection.getErrorStream() != null) {
                    exception = new Exception(String.valueOf(sb));
                }

                if (sb.length() <= 2) {
                    exception = new Exception("No results found.\n\nIs the barcode correct? " + urlFirstParameter);
                } else {
                    rawJson = sb.toString();
                }

                inputStream.close();
                connection.disconnect();
            } catch (IOException e) {
                exception = e;
                inputStream.close();
                connection.disconnect();
            }
        } catch (ProtocolException e) {
            exception = e;
        } catch (MalformedURLException e) {
            exception = e;
            exception = new Exception(String.valueOf(sb));
        } catch (IOException e) {
            exception = e;
        }

    }

    public SimpleDateFormat getDateFormat() {
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf;
    }

    private String getDGST(String APIKEY, String message) {
        Mac mac;
        byte[] hmac256 = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec sks = new SecretKeySpec(APIKEY.getBytes(), "HmacSHA256");
            mac.init(sks);
            hmac256 = mac.doFinal(message.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return android.util.Base64.encodeToString(hmac256, android.util.Base64.DEFAULT);
    }

    public String containersToUrlList(ArrayList<String> list, String paramKeyword) {
        String delim = "&" + paramKeyword + "=";

        StringBuilder sb = new StringBuilder();
        if (list != null && list.size() > 0) {
            sb.append(delim);
            int i = 0;
            while (i < list.size() - 1) {
                try {
                    sb.append(URLEncoder.encode(list.get(i), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                sb.append(delim);
                i++;
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }
}
