/*package com.indexisto.dit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.indexisto.dit.data.object.wordpress.Post;
import com.indexisto.dit.helper.HttpClient;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;


public class SnatcherThread extends Thread {

	private AtomicBoolean finish = new AtomicBoolean(false);
	
	private static Logger log = Logger.getLogger(SnatcherThread.class);
	
	@Override
	public void run(){
	    try {
	        while (!isFinished()) {
	            Thread.sleep(Settings.SNATCHING_REQUEST_TIMEOUT);

	    		String snatch = HttpClient.getSnatch("http://46.4.39.138:8082/snatcher.php?query=SELECT%20*%20FROM%20wp_posts%20LIMIT%200,%2030");
	    		//log.debug("snatch: " + snatch);
	    		
	    		List<Post> posts = new JSONDeserializer<ArrayList<Post>>()
	    				.use("values", Post.class).deserialize(snatch);
    		
	    		for (Post post : posts) {
	    			JSONSerializer serializer = new JSONSerializer();
	    		    String postJson = serializer.serialize(post);
	    			log.info(postJson);
	    			HttpClient.postIndex(post.getID(), postJson);
	    		}
	        }
	    } catch (InterruptedException e) {}
	}
	
	public void launch() {
		start();
	}
	
	public void terminate() {
		try {
			log.info("stopping snatcher");	
			setFinish(true);
			interrupt();
			join(Settings.TERMINATE_JOIN_TIME);
			log.info("snatcher thread stopped");	
		} catch (InterruptedException e) {}		
	}
	
	public boolean isFinished() {
		return finish.get();
	}

	private void setFinish(boolean finish) {
		this.finish.set(finish);
	}	
}*/