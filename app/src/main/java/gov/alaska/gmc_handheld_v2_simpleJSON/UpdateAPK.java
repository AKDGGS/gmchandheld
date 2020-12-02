package gov.alaska.gmc_handheld_v2_simpleJSON;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class UpdateAPK{
// https://stackoverflow.com/a/11856143
HttpURLConnection connection;

	public List<String> getTextFromWeb(String urlString)
	{
		URLConnection feedUrl;
		List<String> placeAddress = new ArrayList<>();

		try
		{
			feedUrl = new URL(urlString).openConnection();
			InputStream is = feedUrl.getInputStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line = null;

			while ((line = reader.readLine()) != null) // read line by line
			{
				placeAddress.add(line); // add line to list
			}
			is.close(); // close input stream

			System.out.println(placeAddress.get(0));
			return placeAddress; // return whatever you need
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}




}