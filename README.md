# The bank application
This is the sample application for IV1201. The purpose is to show usage of frameworks and tools. It is similar to the bank application used in hw4 in ID1212, Network Programming, but more extensive.

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

### To run with mariadb in docker.

1. Start the mariadb container.  
```docker run --name bank-mariadb -d -e MYSQL_ROOT_PASSWORD=jpa mariadb:10.3```

2. Start the mysql client against the mariadb container.  
```docker run --link bank-mariadb:mysql -it --rm mariadb sh -c 'exec mysql -h${MYSQL_PORT_3306_TCP_ADDR} -P${MYSQL_PORT_3306_TCP_PORT} -uroot -p'```

    1. Create the database  
    `create database appservspringbank;`
    2. Create the tables  
    `use appservspringbank;`  
    `<run the provided script>`

3. Start the bank server  
```docker run --link bank-mariadb:mysql -p8080:8080 -e spring.datasource.url='jdbc:mariadb://${MYSQL_PORT_3306_TCP_ADDR}:${MYSQL_PORT_3306_TCP_PORT}/appservspringbank?serverTimezone=UTC' -e spring.datasource.username=root -e spring.datasource.password=`your password` se.kth.id1212/appserv-spring:2.0```
