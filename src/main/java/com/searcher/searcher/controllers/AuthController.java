package com.searcher.searcher.controllers;

import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.validation.Valid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.searcher.beans.User;
import com.searcher.searcher.services.AuthServices;
import com.searcher.searcher.services.SaveServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class AuthController {
    @Autowired
    AuthServices authServices;

    @Autowired
    SaveServices saveServices;
    
    @PostMapping (path="/auth/signup")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody User signupUser) {
        //TODO: check if email exists in usersTable
        Map<String,Object> resp = new HashMap();
        if(authServices.getUserForEmail(signupUser.getEmail())==null){
             User addedUser = authServices.addUser(signupUser);
             ObjectMapper m = new ObjectMapper();
             HashMap obj = m.convertValue(addedUser, HashMap.class);
             String appId = addedUser.getAppId();
             saveServices.addClientDbInfo(appId);
             return new ResponseEntity<Map<String, Object>>(obj,HttpStatus.OK);
            }
        else{
           resp.put("Error", "Email already exists");
           return new ResponseEntity<Map<String, Object>>(resp,HttpStatus.BAD_REQUEST);
        }
     }


    @PostMapping (path="/auth/login") 
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody User loginUser) {
        
        User foundUser = authServices.getUserForEmailPwd(loginUser.getEmail(), loginUser.getPassword());
       
        Map<String,Object> resp =  new Hashtable<>();
        if(foundUser!=null){
            ObjectMapper m = new ObjectMapper();
            HashMap obj = m.convertValue(foundUser, HashMap.class);
            return new ResponseEntity<Map<String, Object>>(obj,HttpStatus.OK);
        }
        resp.put("error","User id or password didn't match");
        return new ResponseEntity<Map<String, Object>>(resp,HttpStatus.BAD_REQUEST);
        
    } 
    
}