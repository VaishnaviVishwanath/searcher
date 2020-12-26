

package com.searcher.searcher;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

import com.searcher.searcher.configs.AppConfig;

@SpringBootApplication
@EnableAsync
public class SearcherApplication {

	public static void main(String[] args) {
		/*Using the appConfig file here*/ 
		AnnotationConfigApplicationContext factory = new AnnotationConfigApplicationContext(AppConfig.class);
       	SpringApplication.run(SearcherApplication.class, args);
	}
}































