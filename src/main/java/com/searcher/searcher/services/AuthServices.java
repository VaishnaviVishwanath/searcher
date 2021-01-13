package com.searcher.searcher.services;

import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.searcher.beans.User;

import org.bson.BsonObjectId;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServices {
    @Autowired
    private MongoClient mongoClient;
    private final String DEFAULT_DB = "searcher_default";
    private final String USERS_COLLECTION = "users";
    
    private MongoDatabase db;
    private MongoCollection usersCollection;
    private Gson gson = new Gson();
    private void init(){
        this.db = mongoClient.getDatabase(DEFAULT_DB);
        this.usersCollection = this.db.getCollection(USERS_COLLECTION);
    }
    public User getUserForEmail(String email){
      this.init();
    //   User foundUser = null;
      if(this.usersCollection!=null){
        FindIterable<Document> user = this.usersCollection.find(new Document("email",email));
        // System.out.println("temp thing"+user.iterator().next());
        if(user.iterator().hasNext()){
            Document foundUser = user.iterator().next();
            if(foundUser!=null){
                return gson.fromJson(foundUser.toJson(), User.class);
            }
        }
        }
      return null;
    }
    public User addUser(User newUser){
        //INFO: universal way of converting objects into one another.
        this.init();
        ObjectMapper m = new ObjectMapper();
        Document obj = m.convertValue(newUser, Document.class);
        InsertOneResult res = this.usersCollection.insertOne(obj);
        if(res!=null && res.getInsertedId()!=null){
            BsonObjectId objectId= (BsonObjectId)res.getInsertedId();
            newUser.setAppId(objectId.getValue().toString());
        }
        return newUser;
    }
    public User getUserForEmailPwd(String email, String password){
        this.init();
        if(this.usersCollection!=null){
            Document queryDoc = new Document();
            queryDoc.put("email", email);
            queryDoc.put("password", password);
            FindIterable<Document> user = this.usersCollection.find(queryDoc);
            // System.out.println("temp thing"+user.iterator().next());
            if(user.iterator().hasNext()){
                Document foundUser = user.iterator().next();
                foundUser.put("appId", foundUser.get("_id").toString());
                if(foundUser!=null){
                    return gson.fromJson(foundUser.toJson(), User.class);
                }
               }
            }
          return null;
    }
}