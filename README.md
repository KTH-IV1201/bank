# The Bank Application
This is the bank sample application for IV1201. The purpose is to show usage of Java frameworks and tools. 

## Tools
The following software development tools are used.
 - Version control (Git)
 - Project management (Maven)
 - Test (Spring MVC Test Framework)
 - Static analysis (Spotbugs and PMD)
 - Containerization (Docker)
 - Continous integration, CI (Travis)
 - Cloud runtime (Heroku)

## Frameworks
The following frameworks are used.
 - Java Servlets
 - Spring core technologies, in particular the IoC container
 - Spring Boot
 - Spring Web MVC
 - Thymeleaf
 - Spring Data (Commons and JPA)
 
## Help

Below follows instructions on how to perform particular tasks.

### To run with MySQL in docker.
1. Set the environment variables `MYSQL_PORT_3306_TCP_ADDR` and `MYSQL_PORT_3306_TCP_PORT`.

2. Start the MySQL container.  
```docker run --name bank-mysql -d -e MYSQL_ROOT_PASSWORD=your-password mysql:8.0```

3. Start the MySQL client against the MySQL container.  
```docker run --link bank-mysql:mysql -it --rm mysql sh -c 'exec mysql -h${MYSQL_PORT_3306_TCP_ADDR} -P${MYSQL_PORT_3306_TCP_PORT} -uroot -pyour-password'```

    1. Create the database  
    `create database appservspringbank;`
    2. Create the tables  
    `use appservspringbank;`  
    run the commands in the SQl script `create-appservspringbank-mysql.sql`
    3. Quit the MySQL client
    `\q`

4. Start the bank server  
```docker run --link bank-mysql:mysql -p8080:8080 -e spring.datasource.url='jdbc:mysql://${MYSQL_PORT_3306_TCP_ADDR}:${MYSQL_PORT_3306_TCP_PORT}/appservspringbank?serverTimezone=UTC' -e spring.datasource.username=root -e spring.datasource.password=your-password se.kth.id1212/appserv-spring:2.0```
