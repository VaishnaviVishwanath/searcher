package com.searcher.searcher.controllers;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
	public boolean saveObjects(@RequestBody SaveObjectRequest saveObjectRequest) {
		String appId = saveObjectRequest.getAppId();
		String dbName = saveServices.getDbForAppId(appId);
	    System.out.println("===debug===saveObject controller called"+dbName+saveObjectRequest.getIndex());
		saveServices.addOrUpdateObjects(dbName, saveObjectRequest.getIndex(),  saveObjectRequest.getObjects());
		return true;
	}
	
	

}
