package com.searcher.beans;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class SearchQuery {
	@NotBlank
	private String appId;
	@NotBlank
	private String index;
	private String query;
	private Map<String, Integer> paginateInfo = new HashMap<String, Integer>();
	private Map<String,Object> filterInfo = new HashMap<String, Object>();
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
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public Map<String, Integer> getPaginateInfo() {
		return paginateInfo;
	}
	public void setPaginateInfo(Map<String, Integer> paginateInfo) {
		this.paginateInfo = paginateInfo;
	}
	public Map<String, Object> getFilterInfo() {
		return filterInfo;
	}
	public void setFilterInfo(Map<String, Object> filterInfo) {
		this.filterInfo = filterInfo;
	}
	
	

}
