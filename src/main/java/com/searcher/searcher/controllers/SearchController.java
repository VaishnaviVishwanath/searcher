package com.searcher.searcher.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.searcher.beans.Entity;
import com.searcher.beans.SearchQuery;
import com.searcher.searcher.services.QueryServices;
import com.searcher.searcher.services.SaveServices;

@RestController
public class SearchController {
    @Autowired
    private QueryServices queryServices;
    
    @Autowired
    private SaveServices saveServices;
	
	@PostMapping (path="/query")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Map<String, Object>>query(@Valid @RequestBody SearchQuery searchQuery) {
    	
		//Not performing external validation here because already using @Valid thing for bean level validation.
		String appId = searchQuery.getAppId();
		String dbName = saveServices.getDbForAppId(appId);
		String query = searchQuery.getQuery();
		String index = searchQuery.getIndex();
		
		Map<String,Integer> pageInfo = searchQuery.getPaginateInfo();
		
		System.out.println("===debug===pageInfo"+pageInfo);
		//TODO: apply filteration and pagination logic as well.
		
		//TODO:try to convert the object to Entity bean.
    	Map<String, Object> searchResp = queryServices.search(dbName,index,query,pageInfo);
       
    	return new ResponseEntity<Map<String, Object>>(searchResp,HttpStatus.OK);
   
	}
	
}
