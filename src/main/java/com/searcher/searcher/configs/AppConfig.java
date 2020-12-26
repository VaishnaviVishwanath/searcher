package com.searcher.searcher.configs;

import java.net.UnknownHostException;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.Morphia;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.searcher.beans.Entity;

@Configuration
public class AppConfig {
	/* function to return mongo db client */
	/*Whenever a MongoClient bean will be @Autowired, this singleton instance will be used there.*/
	
	
	@Bean
	public MongoClient mongo() {
		MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
		return mongoClient;
	}
	
	/*Redis configurations*/
    @Bean
	public JedisConnectionFactory jedisConnectionFactory() {
    	RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        /*Localhost is 127.0.0.1*/
    	redisStandaloneConfiguration.setHostName("127.0.0.1");
        redisStandaloneConfiguration.setPort(6379);
    	/*If setted password then useful*/
        //redisStandaloneConfiguration.setPassword("blahblah");
        
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration);
        return jedisConnectionFactory;    
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(){
    	RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    	redisTemplate.setConnectionFactory(jedisConnectionFactory());
    	
    	/*Setted to serialize (stringify) the key*/
    	redisTemplate.setKeySerializer(new StringRedisSerializer());
    	redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    	
    	/*Setting cachea value serializer*/
    	redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
    	redisTemplate.setHashValueSerializer(new JdkSerializationRedisSerializer());
    	
    	
    	redisTemplate.setEnableTransactionSupport(true);
      	redisTemplate.afterPropertiesSet();
        return redisTemplate;
     }
    
    //INFO: creating a singleton instance of RestTemplate.
    @Bean
    public RestTemplate restTemplate() {
    	RestTemplate restTemplate = new RestTemplate();
    	return restTemplate;
    }
    
     
    
}

