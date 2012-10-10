/**
 * 
 */
package com.dgsd.android.uws.ShuttleTracker.Util;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;

/**
 * Helper methods for communicating over http
 * 
 * @author Daniel Grech
 */
public class Http {
	private static final String TAG = Http.class.getSimpleName();

	public static final int TIMEOUT_MILLIS = 60000;
	public static final String UTF8 = "UTF-8";
	public static HttpParams mHttpParams;

	static {
		mHttpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(mHttpParams, TIMEOUT_MILLIS);
		HttpConnectionParams.setSoTimeout(mHttpParams, TIMEOUT_MILLIS);
	}

	/**
	 * Issues a GET command to the given url
	 * 
	 * Note that this is a blocking call, and should not be invoked on the main
	 * UI thread
	 * 
	 * @param url
	 *            The url to query
	 * @return The response from the url as a String
	 * @throws java.io.IOException
	 * @throws org.apache.http.client.ClientProtocolException
	 */
	public static String get(String url) throws IOException {
		HttpClient client = new DefaultHttpClient(mHttpParams);
		return client.execute(new HttpGet(url), new BasicResponseHandler());
	}
}
