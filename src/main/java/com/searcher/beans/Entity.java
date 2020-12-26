package com.searcher.beans;

import java.util.Date;
import java.util.LinkedHashMap;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Entity {
    private ObjectId _id;
    private Date createTs;
    public ObjectId get_id() {
		return _id;
	}
	public void set_id(String _id) {
		if(_id!=null) {
			this._id = new ObjectId(_id);	
		}
		else {
			this._id = null;
		}
		
	}

	private Date updateTs;
    private Object objContent;
    
    public Entity(Object obj) {
    	if(obj instanceof LinkedHashMap) {
		   LinkedHashMap castedObj = (LinkedHashMap) obj;
		   set_id((String) castedObj.get("objectID"));	   
           //If objectId doesn't exists it means it's a new object so setting createTs and updateTs to now
		   //else not touching the createTs
		   if(castedObj.get("objectID")==null) {
			   setCreateTs(new Date());
		   }
		   //Always setting updateTs to now.
		   setUpdateTs(new Date());
		   //Removing redundant objects before saving it to objectContent field
		   final String redundantFields[]= {"objectID","createTs","updateTs"};
		   for(String field:redundantFields) {
			   castedObj.remove(field);
		   }
		   setObjContent(castedObj);
		  }
    }
	public Object getObjContent() {
		return objContent;
	}

	public void setObjContent(Object objContent) {
		this.objContent = objContent;
	}

		@Override
	public String toString() {
		return "Entity [_id=" + _id + ", createTs=" + createTs + ", updateTs=" + updateTs + ", objContent="
				+ objContent + "]";
	}

		public Date getCreateTs() {
		return createTs;
	}

	public void setCreateTs(Date createTs) {
		this.createTs = createTs;
	}

	public Date getUpdateTs() {
		return updateTs;
	}

	public void setUpdateTs(Date updateTs) {
		this.updateTs = updateTs;
	}

//	public String getObjectID() {
//		return objectID;
//	}
//
//	public void setObjectID(String objectID) {
//		this.objectID = objectID;
//	}
    
	
	
	
}
