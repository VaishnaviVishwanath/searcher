package com.searcher.beans;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SaveObjectRequest {
     /*What if a new app id comes, we would need to write a new db ?*/
	 private String appId;
     private String index;
     
     private ArrayList<Entity> objects = new ArrayList<Entity>();

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public ArrayList<Entity> getObjects() {
		return objects;
	}

	@Override
	public String toString() {
		return "SaveObjectRequest [appId=" + appId + ", index=" + index + ", objects=" + objects + "]";
	}
	
	/*The function is required to change the object in objects request to Entity object*/
	private ArrayList<Entity> formatObjectsToEntity(ArrayList<Object> objs){
	  ArrayList<Entity> formattedEntities = new ArrayList<Entity>();
		objs.forEach((obj)->{
		   Entity entityObj = new Entity(obj);
		   formattedEntities.add(entityObj);
		});
	   return formattedEntities;
	}

	public void setObjects(ArrayList<Object> objects) {
		//converting general objects to Entity type. 
		ArrayList<Entity> formattedEntities = formatObjectsToEntity(objects);
		this.objects = formattedEntities;
	}
	
	
}
