package com.searcher.beans;

import java.util.Date;
import java.util.Map;

import javax.validation.constraints.NotBlank;

public class DataEvent {
	private Date createTs;
	@Override
	public String toString() {
		return "DataEvent [createTs=" + createTs + ", dbName=" + dbName + ", eventCategory=" + eventCategory
				+ ", eventLabel=" + eventLabel + ", value=" + value + "]";
	}
	@NotBlank
	private String dbName;
	@NotBlank
	private String eventCategory;
	@NotBlank
	private String eventLabel;
	private Map<String, Object> value;
	public Date getCreateTs() {
		return createTs;
	}
	public void setCreateTs(Date createTs) {
		this.createTs = createTs;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getEventCategory() {
		return eventCategory;
	}
	public void setEventCategory(String eventCategory) {
		this.eventCategory = eventCategory;
	}
	public String getEventLabel() {
		return eventLabel;
	}
	public void setEventLabel(String eventLabel) {
		this.eventLabel = eventLabel;
	}
	public Map<String, Object> getValue() {
		return value;
	}
	public void setValue(Map<String, Object> value) {
		this.value = value;
	}
}
