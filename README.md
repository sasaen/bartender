# Bartender
 Bartender test implementation using Spring Boot, Akka, Tomcat and Rest endpoints.
 
 
 Requirements: 
 - Java 8
 - Maven 3.3.9
 

 Instructions to use: 
 - Download the code from https://github.com/sasaen/bartender
 - Run it by opening a terminal, cd to the downloaded folder and type:  mvn spring-boot:run
 - Use a rest client (i.e. curl or Restlet client) to execute the endpoints:
	- http://localhost:8080/bartender/request/{customerName}/{drinkType} It´s a POST endpoint to request a DRINK or BEER by a {customerName}. 
	  Possible values for a drink: DRINK or BEER (case sensitive).
	- http://localhost:8080/bartender/served-drinks  It´s a GET end point to get all the served drinks.
	- To see the Rest documentation generated by Swagger, please visit: http://localhost:8080/swagger-ui.html	
 - All the tests can executed typing: mvn test	
 - The timeout properties can be customised here: bartender/src/main/resources/application.properties.
 - The capacity (number of beer/drinks that the bartender can handle at the same time) can be also customised in application.properties.
 - The log level can be customised here: logback-spring.xml I intentionally not included the thread names [%thread] for easy reading of the logs. 
 
 Assumption:
  - Just to clarify the spec, while the bartender is preparing a BEER, he can also take another BEER request without affecting the preparation time of the first BEER.
 
 Notes: 
  - I chose Akka because it is a modern toolkit based on the actor model that provides a level of abstraction that makes it easier to write correct concurrent, 
    parallel and distributed systems. 
  - I expected a better integration between Akka and Spring, so that actors would be created by the spring container and injected in other classes but I did not find a 
    way to do that. Injecting the dependencies would help implementing the unit tests by injecting mock Actors.  
  - Using Akka was a proof of concept to me and seems to work ok, but as I mentioned the integration with Spring could be better. 
  - Another option would be to implement it using Spring Reactor.
  - As commented above, I used Swagger for the documentation of the endpoints.
  - As an extra, added Spring  Actuator endpoint which provides links to other end points like: health, metrics, beans, etc: 
	  http://localhost:8080/actuator
	  http://localhost:8080/health   
	  http://localhost:8080/metrics   
	  http://localhost:8080/beans   	    
  - I added an Integration test that tests multiple requests and gets the list of served drinks. This is useful for invoking concurrently the endpoints.  
  - I did not implement a front end (html/javascript), instead I tested it manually with Reslet Client and programatically with the integration test.
  
 
 TODO:
  - Write unit tests for the Actor classes. I was expecting an intuitive way to mock the behaviour of these actors. This needs further investigation.
  - Find out best practices for Akka actor creation, supervision, life cycle, error handling and integration with Spring.  
  
