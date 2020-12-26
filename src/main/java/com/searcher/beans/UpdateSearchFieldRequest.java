package com.searcher.beans;

import java.util.ArrayList;

public class UpdateSearchFieldRequest {
	private String appId;
	private String index;
    private ArrayList<String> fields;
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
	public ArrayList<String> getFields() {
		return fields;
	}
	public void setFields(ArrayList<String> fields) {
		this.fields = fields;
	}
	
	

}
