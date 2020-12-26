package com.searcher.searcher.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.searcher.beans.ClientDbInfo;



//TODO: have to make functions generic. functions shouldnt be specific to clientDbInfo obviously.
@Service
public class RedisServices {
	@Autowired
    private RedisTemplate redisTemplate;
    private static final String CLIENT_DB_KEY = "CLIENT_DB";
	
	public boolean setClientDbInfo(String key, Object clientDbInfoObj) {
		try {
    	redisTemplate.opsForHash().put(CLIENT_DB_KEY, key, clientDbInfoObj);
	    System.out.println("Save to redis"); 
    	return true;
		} catch (Exception e) {
			//LEARN way of printing stack trace of an exception.
			e.printStackTrace();
			return false;
		}
	}
    
    public String getClientDbInfo(String key) {
    	try {
    	   Object element = redisTemplate.opsForHash().get(CLIENT_DB_KEY, key);
    	   if(element==null) {
    		   return null;
    	   }
    	   else {
    		   return element.toString();
    		 
    	   }
    	  } catch(Exception e) {
    		e.printStackTrace();
    		return null; 
    	}
    }
    
    //INFO: these are more generic utilities for setting and retrieving values from redis.
    public boolean saveInCache(String module, String key, Object obj) {
    	try {
          redisTemplate.opsForHash().put(module,key,obj);
    	  System.out.println("Saved to cache"+module+key);
          return true;
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    	
    }
    
    public Object getFromCache(String module, String key) {
    	try { 
    		 Object element = redisTemplate.opsForHash().get(module, key);
    	     return element;
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    	
    }
	
    
}
