package gov.alaska.gmchandheld;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class RemoteApiDownload {
    private Exception exception = null;
    private int responseCode;
    private String rawJson, urlFirstParameter, addedContainerName, addedContainerRemark,newBarcode,
            destinationBarcode;
    private ArrayList<String> containerList;
    private final Context context;

    public RemoteApiDownload(Context context) { this.context = context; }

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

    public String getRawJson() {
        return rawJson;
    }

    public String getDataFromURL() {
        InputStream inputStream;
        HttpURLConnection connection;
        String url = BaseActivity.sp.getString("urlText", "");
        StringBuilder sb1 = new StringBuilder();
        try {
            switch (context.getClass().getSimpleName()) {
                case "Lookup":
                case "LookupDisplay": {
                    String barcode = null;
                    try {
                        barcode = URLEncoder.encode(urlFirstParameter, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        exception = new Exception(e.getMessage());
                    }
                    url = url + "inventory.json?barcode=" + barcode;
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
                    url = url + "summary.json?barcode=" + barcode;
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
                    if (source != null) {
                        sb1.append("src=").append(source);
                    }
                    if (destination != null) {
                        sb1.append("&dest=").append(destination);
                    }
                    url = url + "movecontents.json?" + sb1.toString();
                    break;
                }
                case "MoveDisplay": {
                    String destination = null;
                    try {
                        destination = URLEncoder.encode(urlFirstParameter, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        exception = new Exception(e.getMessage());
                    }
                    url = url + "move.json?d=" + destination + containersToUrlList(containerList, "c");
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
                    if (barcode != null) {
                        sb1.append("barcode=").append(barcode);
                    }
                    if (name != null) {
                        sb1.append("&name=").append(name);
                    }
                    if (remark != null) {
                        sb1.append("&remark=").append(remark);
                    }
                    url = url + "addcontainer.json?" + sb1.toString();
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
                    if (barcode != null) {
                        sb1.append("barcode=").append(barcode);
                    }
                    if (remark != null) {
                        sb1.append("&remark=").append(remark);
                    }
                    if (containerList != null) {
                        sb1.append(containersToUrlList(containerList, "i"));
                    }
                    url = url + "addinventory.json?" + sb1.toString();
                    break;
                }
                case "Quality": {
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
                    if (barcode != null) {
                        sb1.append("barcode=").append(barcode);
                    }
                    if (remark != null) {
                        sb1.append("&remark=").append(remark);
                    }
                    if (containerList != null) {
                        sb1.append(containersToUrlList(containerList, "i"));
                    }
                    url = url + "addinventoryquality.json?" + sb1.toString();
                    break;
                }
                case "AuditDisplay": {
                    String remark = null;
                    try {
                        remark = URLEncoder.encode(urlFirstParameter, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        exception = new Exception(e.getMessage());
                    }
                    url = url + "audit.json?remark=" + remark + containersToUrlList(containerList, "c");
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
                    if (barcode != null) {
                        sb1.append("old=").append(barcode);
                    }
                    if (mNewBarcode != null) {
                        sb1.append("&new=").append(mNewBarcode);
                    }
                    url = url + "recode.json?" + sb1.toString();
                    break;
                }
            }
            URL myURL = new URL(url);
            connection = (HttpURLConnection) myURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Token " + BaseActivity.apiKeyBase);
            connection.setReadTimeout(10 * 1000);
            connection.setConnectTimeout(5 * 1000);
            connection.connect();
            responseCode = connection.getResponseCode();
            try {
                inputStream = connection.getInputStream();
            } catch (Exception e) {
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
                return rawJson;
            }catch (Exception e) {
                exception = e;
                inputStream.close();
                connection.disconnect();
            }
        } catch (ProtocolException e) {
            exception = e;
        } catch (MalformedURLException e) {
            exception = e;
            exception = new Exception(String.valueOf(sb1));
        } catch (IOException e) {
            exception = e;
        }
        return null;
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
