package gov.alaska.gmchandheld;

public class GenericThread extends Thread{
    String url, jsonData;

    public GenericThread( String url) {
        this.url = url;
    }

    @Override
    public void run() {
        NewRemoteAPIDownload newRemoteAPIDownload = new NewRemoteAPIDownload();
        jsonData = newRemoteAPIDownload.getDataFromURL(url);
    }

    public String getJsonData(){
        return jsonData;
    }
}
