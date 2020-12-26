package com.searcher.searcher.services;

import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.print.Doc;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import com.searcher.beans.DataEvent;
import com.searcher.beans.Entity;
//import com.sun.tools.javac.code.Attribute.Array;
//import com.sun.tools.javac.code.Attribute.Array;

@Service
public class QueryServices {
    
//	@Autowired
//	private MongoTemplate mongoTemplate;
	
	//coming singleton from AppConfig.
	@Autowired
	private MongoClient mongoClient;
	
	//coming singleton from AppConfig.
	@Autowired
	private RedisServices redisServices;
	
	@Autowired
	private SettingsServices settingsServices;
	
	//coming singleton from AppConfig.
	@Autowired
	private RestTemplate restTemplate;
	
    private MongoDatabase db;
	private MongoCollection<Document> indice;
	private Gson gson = new Gson();
	private static final String TEXT_INDEX_NAME = "textIndex";
	private static final String CACHE_INDEX_KEY = "TEXT_INDEXES";
	private static final String ANALYZER_BASE_URL = "http://localhost:8082";
	
	private static final String EVENT_FIELD_DB_NAME="dbName";
	private static final String EVENT_FIELD_COLLECTION="collection";
	private static final String EVENT_FIELD_EVENT_CATEGORY="eventCategory";
	private static final String EVENT_FIELD_EVENT_LABEL="eventLabel";
	private static final String EVENT_FIELD_VALUE="value";
	
	
	@Async
	public boolean sendDataEvent(Map<String, Object> searchResult,String collectionName,String query) {
		final String EVENT_CATEGORY="query_info";
		final String EVENT_LABEL="search_result";
		
		
		Map<String, Object> eventValue =  new HashMap();
		eventValue.put("query", query);
		eventValue.put("count", searchResult.get("totalCount"));
         		
        Map<String, Object> event = new HashMap<String, Object>();
        event.put(EVENT_FIELD_DB_NAME,this.db.getName());
        event.put(EVENT_FIELD_COLLECTION,collectionName);
        event.put(EVENT_FIELD_EVENT_CATEGORY, EVENT_CATEGORY);
        event.put(EVENT_FIELD_EVENT_LABEL, EVENT_LABEL);
        event.put(EVENT_FIELD_VALUE,eventValue);		
		
        
        
        System.out.println("====debug===i',m called asynchornously"+event);
//        restTemplate.pos 
        
        org.springframework.http.HttpHeaders headers =  new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<Map<String,Object>>(event,headers);
        
        ResponseEntity<String> resp = restTemplate.postForEntity(ANALYZER_BASE_URL+"/addEvent", request, String.class);
        
//        System.out.println("===debug===request"+request);
        return true;
	}
	
	private  void init(String dbName, String index) {
		this.db = mongoClient.getDatabase(dbName);
		this.indice = db.getCollection(index);
	}
	
	private ArrayList<String> getTextIndicesForCollection(String dbName,String collection){
		
		//INFO: fetching from cache, if not available then fetch from mongodb collection object and put to the cache.
		String redisKey = dbName+collection;
		Object indexInfo = redisServices.getFromCache(CACHE_INDEX_KEY, redisKey);
		if(indexInfo==null) {
		    ArrayList<String> indexes = settingsServices.listTextIndices(dbName, collection, this.indice);	
	        if(indexes!=null) {
	        	redisServices.saveInCache(CACHE_INDEX_KEY, redisKey, indexes);
	        }
		 }
		else {
			return (ArrayList<String>) indexInfo;
		}
		return null;
		
	}
	
	private void getFallbackSearchResults(String dbName,String index,String query,Map<String, Integer> pageInfo,Map<String, Object> searchResult){
		ArrayList<String> indexesSetForCollection = this.getTextIndicesForCollection(dbName,index);
		final String INDEX_PREFIX = "$objContent.";
		
		if(indexesSetForCollection.size()!=0) {
			//INFO: converting to this format.
            //db.getCollection("default_sample_index").aggregate( [ { $addFields: { results: {$or: [{$regexMatch: { input: "$objContent.field_1", regex: /tom/ }},{$regexMatch: { input: "$objContent.field_2", regex: /bla/ }}]  } } } ] );
			
			ArrayList<Document> regexMatchQuery = new ArrayList<Document>();
		
			
			for(String fieldIndex : indexesSetForCollection) {
				Document regexMatchDocumentForIndex = new Document();
				regexMatchDocumentForIndex.put("input", INDEX_PREFIX+fieldIndex);
				//INFO: this is the way of forming reqular expression.
				Pattern pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
				regexMatchDocumentForIndex.put("regex", pattern);
				regexMatchQuery.add(new Document("$regexMatch",regexMatchDocumentForIndex));
			}
			Document aggregateQuery = new Document();
			//INFO: converting to this format:db.getCollection("default_sample_index").aggregate( [ { $addFields: { results: {$or: [{$regexMatch: { input: "$objContent.field_1", regex: /tom/ }},{$regexMatch: { input: "$objContent.field_2", regex: /bla/ }}]  } } } ] );
			aggregateQuery.append("$addFields", new Document("results",new Document("$or",regexMatchQuery)));
	        //INFO: pipeline is a list
//			ArrayList<Document> pipeline = new ArrayList<>();
			
//			pipeline.add(aggregateQuery);			
//			System.out.println("===debug===aggregateQuery"+aggregateQuery);
			Iterable<Document> aggregateResp = this.indice.aggregate(Arrays.asList(aggregateQuery));
      		this.setSearchResultFallback(aggregateResp,searchResult,pageInfo);
		}
	}
	
	private ArrayList<HashMap<String, Object>> formatEntitiesToObject(ArrayList<Entity> entities) {
		//INFO: this is same as array of objects in js.
		ObjectMapper m = new ObjectMapper();
		ArrayList<HashMap<String, Object>> formattedObjects = new ArrayList<HashMap<String,Object>>();
		if(entities.size()>0) {
			for (Entity entity:entities) {
				//INFO: converting a bean to object (hashmap)
				HashMap<String, Object> obj = m.convertValue(entity.getObjContent(), HashMap.class);
        		obj.put("objectID",entity.get_id());
				formattedObjects.add(obj);
			}
		}
		return formattedObjects;
	}
	
	private List<Entity> paginateFallback(ArrayList<Entity> entities,Map<String, Integer> pageInfo) {


		Integer skip = pageInfo.get("skip");
		   	Integer limit = pageInfo.get("limit");
			List<Entity> subList = new ArrayList<Entity>();
	
		   		if(entities!=null && entities.size()>=(limit+skip)) {
		   			
//		   			Integer upperLimit = 
		   			subList  = entities.subList(skip, skip+limit);	
//		   	        System.out.println("===debug====subList"+sublist);
		   		}
		   		else if(entities!=null && entities.size()>=(skip)) {
		   			subList  = entities.subList(skip, entities.size());	
			   	}
		   		
		   		
		   
//		   	System.out.println("===debug===subList"+subList);
		   	return subList;
		
	}
	
	private void setSearchResultFallback(Iterable<Document> aggregationResult,Map<String,Object> searchResults,Map<String, Integer> pageInfo) {
	   //TODO:fetch results on the basis of results = true/false value set
	   //TODO:Paginate by yourself, facet is not woriking here.
	   	ArrayList<Entity> foundEntities = new ArrayList<Entity>();
		if(aggregationResult!=null) {
			aggregationResult.forEach(resultObject->{
				if(resultObject.getBoolean("results")==true) {
					foundEntities.add(gson.fromJson(resultObject.toJson(), Entity.class));
					}
			});
		}
		System.out.println("===debug===foundEntotoes"+foundEntities);
		   searchResults.put("totalCount",foundEntities.size());
		   searchResults.put("hits", this.paginateFallback(foundEntities, pageInfo));
      	   	
	}
	
	private void setSearchResults(Iterable<Document> aggregationResult,Map<String, Object> searchResults ) {
		ArrayList<Entity> foundEntities = new ArrayList<Entity>();
		if(aggregationResult != null) {
			aggregationResult.forEach((docObj)->{
				//Resp of aggregation pipeline is always an array.
				Iterable<Document> paginatedResults = (Iterable<Document>) docObj.get("paginatedResults");      
				Iterable<Document> totalCount  = (Iterable<Document>) docObj.get("totalCount");
				if(paginatedResults!=null) {
					paginatedResults.forEach((result)->{
						if(result!=null) {
							foundEntities.add(gson.fromJson(result.toJson(), Entity.class));
						}
					});
				}
				if(totalCount!=null) {
				    if(totalCount.iterator().hasNext()) {
				    	Document totalCountObject = totalCount.iterator().next();	
				        if(totalCountObject.get("count")!=null) {
				          searchResults.put("totalCount",totalCountObject.get("count"));
				        } 
				  }
			   }
			});
		}
		searchResults.put("hits", foundEntities);
	}
	
	
	//INFO:paginateInfo is an object with info of skip and limit (size)
	public Map<String, Object> search(String dbName, String index, String query, Map<String,Integer> paginateInfo){
		this.init(dbName, index);
		Map<String, Object> searchResult = new HashMap<>();
		Document matchAggregateQuery = new Document();
		matchAggregateQuery.append("$match",new Document("$text",new Document("$search",query)));
		
		Document facetDocument = new Document();
//		facetDocument.
		facetDocument.append("paginatedResults", Arrays.asList(new Document("$skip",paginateInfo.get("skip")),new Document("$limit",paginateInfo.get("limit"))));
        facetDocument.append("totalCount",Arrays.asList(new Document("$count","count")));
		
		Document facet = new Document("$facet",facetDocument);
//		
		Iterable<Document> findIterator = this.indice.aggregate(Arrays.asList(matchAggregateQuery,facet));
	   
		
		ArrayList<Entity> foundEntities = new ArrayList<Entity>();
	    
		//TODO:now foundEntities format changed, print format and then change the iterating code etc.
		//TODO: also pass the facet objet into fallback regex filter function call.
		
	     
		this.setSearchResults(findIterator, searchResult);
		if(searchResult.get("hits")!=null && searchResult.get("hits") instanceof ArrayList) {
			Integer hitCounts = ((ArrayList) searchResult.get("hits")).size();
			if(hitCounts>0) {
				return searchResult;
			}
			else {
				//TODO: invoke function for fallback.
				System.out.println("===debug===invoking fallback"); 
				this.getFallbackSearchResults(dbName, index,query,paginateInfo,searchResult);		
 			}
			
		}
		
	   this.sendDataEvent(searchResult,index,query);	
	   System.out.println("printing from vscode temp");
	//    System.out.println("===debug====searchResult"+searchResult);	
	   return searchResult;
	}
}
