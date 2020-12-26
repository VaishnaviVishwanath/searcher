package com.searcher.searcher.services;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.redis.connection.RedisZSetCommands.Weights;
import org.springframework.stereotype.Service;

import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

@Service
public class SettingsServices {
	@Autowired
	private MongoClient mongoClient;

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private RedisServices redisServices;

	private MongoDatabase db;
	private MongoCollection<Document> collection;
	private final String OBJ_CONTENT_PREFIX="objContent.";
	private final String TEXT_INDEX_NAME="textIndex";
	private static final String CACHE_INDEX_KEY = "TEXT_INDEXES";
    
	private void init(String dbName,String collectionName) {
		this.db = mongoClient.getDatabase(dbName);
		if(this.db!=null) {
			this.collection = db.getCollection(collectionName);
		}
	}
	 
	
	public boolean setSearchIndices(String dbName,String collectionName,ArrayList<String> fields) {
		
		        this.init(dbName, collectionName);
			
			
				//INFO: Indexes is the model used in createIndex api for all kind of indexes.
				//doc:https://mongodb.github.io/mongo-java-driver/3.4/driver/tutorials/indexes/
				Document indexesObj = new Document();
				for(String field: fields) {
					String fieldForObjContent = OBJ_CONTENT_PREFIX+field;
					indexesObj.append(fieldForObjContent, "text"); 
				}
//				System.out.println("===debug====indexesObj"+indexesObj);
				
				//INFO: index options is the second argument provided in createIndex function call. We can't set a document here.
			    
				//dropping previous text indexes as mongo doesn't allow multiple index
                
				
				if(isIndexExists(TEXT_INDEX_NAME)) {
					this.collection.dropIndex(TEXT_INDEX_NAME);
				}
				IndexOptions indexOptions = new IndexOptions().name(TEXT_INDEX_NAME);
					
				/*INFO: The second argument is specifying the index name. because mongodb creates indexes by dynamically creating name
				 * we want to give static name so that we can drop it easily.
				 * Index dropping is required before updation of index because mongodb gives option of maximum one text index. 
				 * */
				   
				String createIndexResp = this.collection.createIndex(indexesObj, indexOptions);
				
				String redisKey = dbName+collectionName;
				    if(createIndexResp!=null) {
			        	redisServices.saveInCache(CACHE_INDEX_KEY, redisKey, fields);
			        }
		
		
     return true;		
	}
	
	private boolean isIndexExists(String indexName) {
		
		ListIndexesIterable<Document> indexList = this.collection.listIndexes();
		ArrayList<Document> fieldsDocs = new ArrayList<Document>();
	    //INFO: way of converting iterator to arrayList.
	    indexList.iterator().forEachRemaining(fieldsDocs::add);
	    if(fieldsDocs!=null && fieldsDocs.size()>=0) {
	    	 Document textIndex = fieldsDocs.stream().filter((obj)->{ return obj.get("name").equals(indexName);}).findFirst().orElse(null); 
		    if(textIndex!=null) {
		    	return true;
		    	}
	    }
		
		return false;
	}
	
	
	public ArrayList<String> listTextIndices(String dbName, String collectionName, MongoCollection<Document> collectionObj){
	    ArrayList<String> textIndices = new ArrayList<String>();
	    if(collectionObj==null) {
	    	this.init(dbName, collectionName);
		    collectionObj = this.collection;
	    }
	    ListIndexesIterable<Document> indexList = collectionObj.listIndexes();
//		System.out.println("===debug===indexList"+indexList.iterator()); 
	    ArrayList<Document> fieldsDocs = new ArrayList<Document>();
	    //INFO: way of converting iterator to arrayList.
	    indexList.iterator().forEachRemaining(fieldsDocs::add);
	    if(fieldsDocs!=null && fieldsDocs.size()>=0) {
	    	
	    	 Document textIndex = fieldsDocs.stream().filter((obj)->{ System.out.println("obj"+obj.get("name")+obj.get("name").equals(TEXT_INDEX_NAME)); return obj.get("name").equals(TEXT_INDEX_NAME);}).findFirst().orElse(null); 
	         if(textIndex!=null) {
	              Document weights = (Document) textIndex.get("weights");	 
	              if(weights!=null) {
		          
	            	  //INFO: keySet() returns a set, to convert set to ArrayList we need the arrayList constructor just like in JS.
	            	  textIndices = new ArrayList<String>(weights.keySet());  
	            	  //INFO: spilt by '.' is not possible without escapting the character by '\\'
	                  textIndices = (ArrayList<String>) textIndices.stream().map((objStr)->{ return objStr.split("\\.")[1]!=null?objStr.split("\\.")[1]:null;}).collect(Collectors.toList());
	                  
	              }
	            
	          }
	    }
		return textIndices;
	}

}
