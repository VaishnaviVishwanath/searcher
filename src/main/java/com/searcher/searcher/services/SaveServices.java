package com.searcher.searcher.services;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.searcher.beans.Entity;
import com.searcher.utils.Utils;
import com.google.gson.Gson;



@Service
public class SaveServices {
      @Autowired
      RedisServices redisServices;
      @Autowired
      private MongoClient mongoClient;
      @Autowired
      private MongoTemplate mongoTemplate;
	  private final String DEFAULT_DB = "searcher_default";
	  private final String COLLECTION_CLIENT_DB_INFO="client_db_info";
	  private final String COLLECTION_APPID_INDICE_MAPPING="appid_indice_mapping";
	  private MongoDatabase db;
	  private MongoCollection<Document> indice;
	  //Needed to convert the db object to json.
	  private Gson gson = new Gson();  

	  private final String[] dbsList = {"default","default_1","default_2"};
	  
	  public ArrayList<String> fetchFieldsForIndice(String dbName, String indice){
		System.out.println("===data===dbName,indice"+dbName+indice);  
		MongoDatabase db = mongoClient.getDatabase(dbName);
		  MongoCollection col = db.getCollection(indice);
		  FindIterable<Document> resp = col.find(new Document());
		 System.out.println("hasNext"+resp.iterator().hasNext()); 
		  if(resp.iterator().hasNext()){
			  Document obj = resp.iterator().next();
			  if(obj.get("objContent")!=null && obj.get("objContent") instanceof Document){

				Document objContent = (Document) obj.get("objContent");
				ArrayList<String> keys = new ArrayList<>(objContent.keySet());
				return keys;
			}
			  
			//   System.out.println("====debug====objContent"+obj.get("objContent").getClass());
			//   System.out.println("===debug===keyset"+obj.keySet().getClass());
			 
		
		   }
	      	return new ArrayList<String>();
		}

	  public ArrayList<String> fetchIndicesForAppId(String appId){
		  MongoDatabase db = mongoClient.getDatabase(DEFAULT_DB);
		  MongoCollection col = db.getCollection(COLLECTION_APPID_INDICE_MAPPING);
		  FindIterable<Document> res = col.find(new Document("appId",appId));
          if(res.iterator().hasNext()!=false){
			Document result = res.iterator().next();
			if(result.get("indices")!=null && result.get("indices") instanceof ArrayList){
               return (ArrayList<String>) result.get("indices");
			} 
			System.out.println("===debug===result_indices"+result.get("indices").getClass());
			// if(result.get("indices"))
		    }
	     return null;
		}

      public String getDbForAppId(String appId) {
		  String clientDb = redisServices.getClientDbInfo(appId); 
          
    	  //    	  System.out.println("hey thre"+clientDb);
    	  if(clientDb!=null) {
		     System.out.println("Fetched from cache"+clientDb);	  
    	     return clientDb;
    	  }
		  else {
			  //fetch from db and set in cache.
			    MongoDatabase database = mongoClient.getDatabase(DEFAULT_DB);
			    MongoCollection<Document> myCollection = database.getCollection(COLLECTION_CLIENT_DB_INFO);
		        
			    /*KNOW: needed to create query object which is needed, same object can be used with projections */
			    BasicDBObject whereQuery = new BasicDBObject();
			    whereQuery.put("appId",appId);
			    FindIterable<Document> iterable =  myCollection.find(whereQuery);
			    MongoCursor<Document> cursor=  iterable.iterator();
			    while(cursor.hasNext()) {
			        Object dbName = cursor.next().get("dbName");
			    	if(dbName!=null) {
			           //Set into redis cache and then return.
			    		redisServices.setClientDbInfo(appId,dbName);
			    		System.out.println("fetched from db");
			    		return dbName.toString();	
			        }
			   }
			    //Before sending to response also set to redis-cache.
	          
		  }
		return appId;
	  }
     
      private void filterNewAndOldEntities(String indice,ArrayList<Entity> entities,ArrayList<Entity> newEntities,ArrayList<Entity> entitiesWithIds) {
    	  
          if(entities!= null && entities.size()!=0) {
   		   entities.forEach((obj)->{
   			  if(obj.get_id()!=null) {
//   				JSONObject jsonObj = new JSONObject(obj);
//   				System.outˇ.println("===jsonObj==="+jsonObj);
   				  entitiesWithIds.add(obj);    			  
   				}
   			  else {
   				  newEntities.add(obj);
   			  }
   		   });
   		  }
      }
      
      private ArrayList<Entity> getDifferentEntities(ArrayList<Entity> entities){
    	  ArrayList<ObjectId> objIds = new ArrayList<ObjectId>();
    	  ArrayList<Entity> differentEntities = new ArrayList<Entity>();
     	  entities.forEach((ent)->{
    	      objIds.add(ent.get_id());	  
    	  });
    	  ArrayList<Entity> fetchedEntities = new ArrayList<Entity>(); 
    	  if(objIds.size()>0) {
    		  /*
    		  * It's equivalant to 
    		  * json's {"objectId":{"$in":objIds}}
    		  * */
    		 
    		 FindIterable<Document> iterable  = this.indice.find(new Document().append("_id", new Document("$in",objIds))); 
    		 MongoCursor<Document> cursor=  iterable.iterator();
			    while(cursor.hasNext()) {
			    	Document obj = cursor.next();
			    	//TODO_bug: here mongoDb _id is set in obj, so need to add that as string to the obj and then convert by following lines.
			    	Entity ent = gson.fromJson(obj.toJson(), Entity.class);	
			    	fetchedEntities.add(ent);
			    }
     	 }
    	  
    	  entities.forEach(ent->{
    		  Entity foundEnt = fetchedEntities.stream().filter(fe->{
    			   return true;
    		  }).findFirst().orElse(null);
              //If entity found then compare ent with foundEntity. if different fields then add to different entities array.
    		  
    		  if(foundEnt!=null) {
                        Map<String, Object> foundEntObj = (Map<String, Object>) foundEnt.getObjContent();
                        Map<String, Object> entObj = (Map<String, Object>) ent.getObjContent();
                       if(!Utils.compareMaps(foundEntObj,entObj)) {
                    	   differentEntities.add(ent);  
                       }
               }
    		  else {
    			  //If entity not found in db, means it's a different entity
    			  differentEntities.add(ent);
    		  }
    	});
    	  
    	  
    	  //Finding out which entities have different values.
//    	  entities.forEach((ent)->{
//    		  fetchedEntities.stream().filter((fe)->{System.out.println("===debug===fe"+fe);return true;})
//;    	  });
    	 return differentEntities;
      }
      private void initializeDbAndCollection(String dbName,String indice) {
    	  this.db = mongoClient.getDatabase(dbName);
          this.indice = db.getCollection(indice);
      }
      
      private List<WriteModel<Document>> getBulkOperationsForInsert(ArrayList<Entity> entities) {

    	  /*Initializing bulk update actions*/
    	  List<WriteModel<Document>> actions = new ArrayList<WriteModel<Document>>();
    	  entities.forEach((entity)->{
    		  if(entity.get_id()!=null) {
    			 //INFO: way of adding replaceone action to the bulk write.
    			  //INFO: new Document is used for creating {"field':'value'} kind of literal in mongodb.
//    			  System.out.println("===debug===parsedReplacement"+entity+Document.parse(gson.toJson(entity)));
    		      Document parsedDocument = Document.parse(gson.toJson(entity));
    		      //Id is parsed to an invalid json pnject because of parser so setting it back to string
    		      parsedDocument.put("_id", entity.get_id());
    		      
    		      actions.add(new ReplaceOneModel<Document>(new Document("_id",entity.get_id()),parsedDocument));	
    		  }
    		  else {
    			  //If ID is not found then inserting one
    			 actions.add(new InsertOneModel<Document>(Document.parse(gson.toJson(entity))));  
    		  }
//    		  System.out.println("===debug===toString"+entity.get_id().toString());
//    		  actions.add(new ReplaceOneModel<Document>(new Document("_id",entity.get_id().toString()), replacement, options))
    	  });
    	  
    	  //    	  BulkWriteOperation operation = this.indice.
//    	  if(entities.size()!=0) {
//    		  
//    	  }
//    	  
//    	  System.out.println("===debug====actions"+actions);
    	  return actions;	 
      }
      public ArrayList<String> addOrUpdateObjects(String dbName,String indice,ArrayList<Entity> entities){
    	 this.initializeDbAndCollection(dbName, indice);
    	 ArrayList<Entity> entitiesWithIds = new ArrayList<Entity>(); 
         ArrayList<Entity> newEntities = new ArrayList<Entity>();
         filterNewAndOldEntities(indice,entities,newEntities,entitiesWithIds);
         if(entitiesWithIds.size()>0) {
    	   	//TODO fetch objects from db. if found then compare fields and add to newEntities list if different else ignore.
         ArrayList<Entity> entitiesToUpdate = this.getDifferentEntities(entitiesWithIds);             		   
         
         if(entitiesToUpdate.size()!=0) {
        	 //INFO: way of concatenating the objects. but here first arrayList got updated. 
        	 newEntities.addAll(entitiesToUpdate);
          }
         }
         //INFO: way of comcatenating two arrayList
         ArrayList<String> savedObjects = new ArrayList<String>();
         ArrayList<Document> entitiesInJson = new ArrayList<Document>();
         newEntities.forEach((entity)->{
        	entitiesInJson.add(Document.parse(gson.toJson(entity)));
         }); 
         //INFO: Class for mongodb inserted response
         //TODO:uncomment this thing to save object
         /*Info: using the bulk write instead of .save because in mongodb 4.2.+, the save api is replaced */
//         System.out.println("===debug===entities in json"+entitiesInJson);
         
         
         List<WriteModel<Document>> operations = this.getBulkOperationsForInsert(newEntities); 
         if(operations!=null && operations.size()!=0) {
        	 //INFO: way of executing bulkwrite operations on mongodb collection (this.indice is a collection).
        	 System.out.println("==debug==operations"+operations.get(0));
        	 BulkWriteResult bulkWriteRes = this.indice.bulkWrite(operations);
             System.out.println("===debug===bulkWriteResp"+bulkWriteRes);
         }
		 
		 
//         InsertManyResult insertResp =  this.indice.
         //INFO: the InsertManyResult has getInsertedIds function. which returns a map for shown structure
         //Doc: https://mongodb.github.io/mongo-java-driver/4.0/apidocs/mongodb-driver-core/com/mongodb/client/result/InsertManyResult.html
        
        		 
//        Map<Integer, BsonValue> insertedObjects = insertResp.getInsertedIds();
//         if(insertResp!=null) {
//        	 //INFO: way of iterating through a map
//        	 for (Map.Entry<Integer, BsonValue> object: insertedObjects.entrySet()) {
//        		 savedObjects.add(object.toString()); 
//			}
//         }
        
         return savedObjects;
        }
	    public boolean addClientDbInfo(String appId){
			//TODO: need a proper way of handling dbs for client rather than from a hardcoded index with random number generator;

			//INFO: way of generating a random number in Java.
			int randomNum = ThreadLocalRandom.current().nextInt(0, 2 + 1);
			String dbName = this.dbsList[randomNum];
            MongoDatabase db = mongoClient.getDatabase(DEFAULT_DB);
			MongoCollection col = db.getCollection(COLLECTION_CLIENT_DB_INFO);
			//checking if entry already exists
			FindIterable<Document> client_db_map = col.find(new Document("appId",appId));
			if(client_db_map.iterator().hasNext()==false){
			    Document clientDbInfoObject = new Document();
				clientDbInfoObject.put("appId", appId);
				clientDbInfoObject.put("dbName", dbName);
				InsertOneResult insertRes = col.insertOne(clientDbInfoObject);   
				if(insertRes!=null && insertRes.getInsertedId()!=null){
					return true;
				}
			}
			return false;
		}
	  
		
		public boolean addAppIdIndiceMapping(String appId, String indice){
			MongoDatabase db = mongoClient.getDatabase(DEFAULT_DB);
			MongoCollection col = db.getCollection(COLLECTION_APPID_INDICE_MAPPING);
			//checking if mapping already exists
			Document queryDoc = new Document();
			queryDoc.put("appId", appId);
			FindIterable<Document> appIdIndices = col.find(new Document("appId",appId));
			if(appIdIndices.iterator().hasNext()==false){
			   Document insertDocument = new Document();
			   ArrayList<String> indices = new ArrayList<String>();  
			   indices.add(indice);  
			   insertDocument.put("appId", appId);
			   insertDocument.put("indices", indices);
			   InsertOneResult res = col.insertOne(insertDocument);
			   if(res!=null && res.getInsertedId()!=null){
				   return true;
			   }
			}
			else{
				Document appIndices = appIdIndices.iterator().next();
				if(appIndices!=null){
					ArrayList<String> indices = (ArrayList<String>) appIndices.get("indices");
					if(indices.contains(indice)==false){
					  indices.add(indice);
					  appIndices.put("indices", indices);
					//   System.out.println("===debug===appIndices"+appIndices.get("_id").toString());
					//   appIndices.put("_id", appIndices.get("_id").toString());
					// appIndices.remove("_id");  
					 System.out.println("===debug===appIndices"+appIndices);
					 UpdateResult res = col.replaceOne(new Document("appId",appId), appIndices);
					 if(res!=null && res.getModifiedCount()!=0){
						 return true;
					 }
					//  return true;
					}
				  }   
				}
			return false;
			
			
			// return false;
		}
     
	
}
