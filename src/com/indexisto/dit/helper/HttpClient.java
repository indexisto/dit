package com.indexisto.dit.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient {
	
	private static Logger log = LoggerFactory.getLogger(HttpClient.class);

    public static String httpGet(String url) throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
			
		InputStream inputStream = entity.getContent();
		String textBody = IOUtils.toString(inputStream, "UTF-8");
    	inputStream.close();
    	return textBody;
    }
	
    public static void httpPost(String url, String body) throws ClientProtocolException, IOException {
     	DefaultHttpClient httpClient = new DefaultHttpClient();		
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("charset", "UTF-8");
		httpPost.setEntity(new StringEntity(body, "UTF-8")); 
		HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();

        InputStream inputStream = entity.getContent();
        String responseBody = IOUtils.toString(inputStream, "UTF-8");
        
        log.debug("response header: " + response.toString());
        log.debug("response body: " + responseBody);
    }
}
