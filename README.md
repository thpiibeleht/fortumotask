# fortumotask
Fortumo SE Test Task

## Usage instructions
Instructions are for IntelliJ IDEA, with the war file deployed on a Tomcat server.

1. Clone this repo. 

2. Import all the Maven dependencies.

3. You may need to change the Language Level under Project Structure -> Modules -> Language Level to 12.

4. Build the artifact under Project Structure -> Artifacts -> '+' -> Web Application: Archive

5. Deploy the .war file in a Tomcat server.

6. Use curl or some other tool to send requests to the application.

  * If you're using a default Tomcat installation, the application will work on port 8080
  * Example to send a number request with curl: *curl -d 6 http://localhost:8080/fortumotesttask_war/*
  * Example to send an end request with curl: *curl -d end http://localhost:8080/fortumotesttask_war/*
 
### Notes and comments

* If the application receives 20 number requests, it will have no way to clear the requests. It would have been possible to safeguard against this by clearing the queue when full, but it was not specified in the task description.
