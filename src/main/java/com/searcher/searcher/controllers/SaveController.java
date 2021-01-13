package com.searcher.searcher.controllers;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.searcher.beans.Entity;
import com.searcher.beans.SaveObjectRequest;
import com.searcher.searcher.services.SaveServices;

@RestController
public class SaveController {
	
	@Autowired
    private MongoClient mongoClient;
	
	@Autowired
	private SaveServices saveServices;
	
//	@GetMapping(path="/save/objects")
//	public boolean addObjects() {
//		System.out.println("is singleton"+mongoClient);
//		
//		return true;
//	}
	
	/*
	 * TODO:
	 * 1. Check how many objects already exist
	 * 2. Create collection scenareo
	 * 
	 * 
	 * */
	
	
	
	@PostMapping(path="/save/objects")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	public boolean saveObjects(@RequestBody SaveObjectRequest saveObjectRequest) {
		String appId = saveObjectRequest.getAppId();
		String dbName = saveServices.getDbForAppId(appId);
	   saveServices.addOrUpdateObjects(dbName, saveObjectRequest.getIndex(),  saveObjectRequest.getObjects());
		saveServices.addAppIdIndiceMapping(appId,saveObjectRequest.getIndex());
        
		return true;
	}
	
	@GetMapping (path="/getIndicesForApp")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	public ResponseEntity<ArrayList<String>> getIndicesForApp(@RequestParam (value="appId",required=true) String appId){
        ArrayList<String> resp = saveServices.fetchIndicesForAppId(appId);
		return new ResponseEntity<ArrayList<String>>(resp,HttpStatus.OK);
	}

	@GetMapping (path="/getFieldsForIndice")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	public ResponseEntity<ArrayList<String>> getFieldsForIndice(@RequestParam (value="appId",required=true) String appId,@RequestParam (value="indice",required=true) String indice){
		String dbName = saveServices.getDbForAppId(appId);
		System.out.println("====debug===appOId dbName"+appId+dbName);
		ArrayList<String> resp = saveServices.fetchFieldsForIndice(dbName,indice);
		return new ResponseEntity<ArrayList<String>>(resp,HttpStatus.OK);
	}
	


}
