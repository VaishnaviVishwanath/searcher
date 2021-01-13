package com.searcher.searcher.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Aggregates;
import com.searcher.beans.UpdateSearchFieldRequest;
import com.searcher.searcher.services.SaveServices;
import com.searcher.searcher.services.SettingsServices;

@RestController
public class SettingController<K, V> {
//    @Autowired
//	private MongoClient mongoClient;
	@Autowired
	private SettingsServices settingServices;
    
	//TODO: put bean level validation for the request.
	@Autowired SaveServices saveServices;
	@PostMapping(path="/setting/updateSearchField")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	public boolean updateSearchField(@RequestBody UpdateSearchFieldRequest updateSearchFieldRequest) {
	String appId = updateSearchFieldRequest.getAppId();
	String dbName = saveServices.getDbForAppId(appId);
	//collection
	String index = updateSearchFieldRequest.getIndex();
	ArrayList<String> fieldsToSet = updateSearchFieldRequest.getFields();
	
	boolean settingResult = settingServices.setSearchIndices(dbName,index,fieldsToSet);
	return true;
	}
	
	
	//INFO: the @RequestParam is the annotation used for getting query parameters
	@GetMapping(path="/setting/getSearchableFields")
	public ResponseEntity<Map<String, Object>> getSearchableFields(@RequestParam(value="appId",required=true) String appId, @RequestParam(value="index",required=true) String index) {
		String dbName = saveServices.getDbForAppId(appId);
		
//		ArrayList<String> fieldsIndexed = 
		
		ArrayList<String> indexesList = settingServices.listTextIndices(dbName, index, null);
		
		Map<String, Object> respObj = new HashMap<String, Object>();
		respObj.put("fields", indexesList);
		
		return new ResponseEntity<Map<String, Object>>(respObj,HttpStatus.OK);
		
	}
	 
}
