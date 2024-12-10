# The Bank Application

This is the bank sample application for IV1201. The purpose is to show usage of Java frameworks and tools. **The latest supported JDK is 22, since it's the latest version supported by the PMD static analyzer.**

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

### To start with maven and postgres

1. Set the following environment variables

    - `POSTGRES_TCP_ADDR` is the host where the postgres server is running
    - `POSTGRES_TCP_PORT` is the port number of the postgres server
    - `SPRING_DATASOURCE_PASSWORD` is the password of the postgres server

1. Start the application with the command `mvn spring-boot:run`
1. The application can now be reached at the url `http://localhost:8080/bank`

### To execute tests and static analyzers with maven and postgres

The same steps as in [To start with maven and postgres](#to-start-with-maven-and-postgres), but give the command `mvn install` instead of `mvn spring-boot:run`

### To start with maven and MySQL

1. Comment out the lines related to postgres in the file `application.properties`, it's written in that file which these lines are.

1. Uncomment the lines related to MySQL in the file `application.properties`, it's written in that file which these lines are.

1. Set the following environment variables

    - `MYSQL_PORT_3306_TCP_ADDR` is the host where the MySQL server is running
    - `MYSQL_PORT_3306_TCP_PORT` is the port number of the MySQL server
    - `SPRING_DATASOURCE_PASSWORD` is the password of the MySQL server

1. Start the application with the command `mvn spring-boot:run`
1. The application can now be reached at the url `http://localhost:8080/bank`

### To run with MySQL in docker

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
```docker run --link bank-mysql:mysql -p8080:8080 -e spring.datasource.url='jdbc:mysql://${MYSQL_PORT_3306_TCP_ADDR}:${MYSQL_PORT_3306_TCP_PORT}/appservspringbank?serverTimezone=UTC' -e spring.datasource.username=root -e spring.datasource.password=your-password se.kth.iv1201/appserv-spring:2.0```
