# Car Sharing Service project

[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)
[![Mentioned in Awesome Awesome README](https://awesome.re/mentioned-badge-flat.svg)](https://github.com/matiassingers/awesome-readme)

## Table of contents
* [Introduction](#introduction)
* [Project description](#project-description)
* [Project tools & technologies](#project-tools-and-technologies)
* [Provided controller's and related services functionalities](#provided-controllers-functionalities)
* [Test's functionalities](#tests-functionalities)
* [Install and Run the application](#install-and-run-the-application)
* [Usage with Postman](#usage-with-postman)
* [Contacts](#contacts)

## Introduction

This project was developed as part of the **Mate Academy Java Developer** program. It is a basic version of a **Spring Boot application** for a car sharing service, providing essential functionality and can be further expanded with new features.

The project addresses the issues of an outdated manual system for tracking cars, rentals, users, and payments. It provides an online management system for **Car rentals service** in your city for instance, streamlining the work of service administrators and significantly improving the user experience.
## Project description
**In this app is implemented Car Sharing an online service and therefore it has the following domain models :**

- **Car** : Represents a cars available in the service for a rent with specific parameters and inventory available.
- **User** : Contains information about the registered user including their authentication details and some personal information.
- **Role** : Represents the role of a user in the system, can operate in such roles as *MANAGER*, *CUSTOMER*.
- **Rental** : Represents rentals made by a user.
- **Payment** : Represents payments with some specific parameters (status, type, rental, payment session details).

**People involved :**
- **Renter (Customer)**: Someone who looks at available cars, choose period for rental, rents selected cars for this period, completes and pays for rental.
- **Administrator (Manager)**: Someone who arranges the cars in the service, monitors rentals and payments, manages or update some data in service.

**Actions Renters Can Do**:
- **Join and sign in**: Join the service as a Customer; Sign in to look at cars and rent them.
- **Look at and change own profile**: Look at own profile and update registration information in profile.
- **Look at and search for Cars**: Look at all the cars or search at one specific car.
- **Rent a car**: Add a new rental with specific car and period for rental.
- **Look at rental**: Look at specific rental using parameter is it active or not.
- **Complete rental**: Complete specific rental with actual date, return a car.
- **Create a payment session**: Add a new payment session to pay by credit card for a specific rental.
- **Look at payments**: Look at own payments and its details.
  
**Actions Administrators Can Do**:
- **Arrange cars**: Add a new car to the service; Change details of a specific car like inventory and daily fee; Delete a specific car decreasing its inventory.
- **Look at and change user’s role**: Change the role of a specific user.
- **Look at user’s rentals**: Look at rental of a specific user using parameter is it active or not.
- **Look at user’s payments**: Look at payments of a specific user.


## Project tools & technologies
This Book Store is a stand-alone **Spring based** application and uses **Spring Boot** extension. In context of *SpringBoot* it includes :
- **Spring Boot Starters, DevTools** : For development .
- **DTO and MapStruct** : For data transfer, appropriate data conversion and mapping .
- **Spring MVC** : For controller operations and user input and data validation .
- **Spring Data JPA** :  For project architecture and database operations .
- **Spring Boot Security** : For security-related functionalities .
- **GlobalExceptionHandler** : For handling implemented exceptions such as *RegistrationException, EntityNotFoundException, DataNotFoundException, IncorrectArgumentException, PaymentProcessingException and AccessDeniedException* classes .
- **Lombok** : For reducing number of template code and better readability .
- **Liquibase** : For managing and applying database schema changes .
- **Swagger UI** : For API documentation of existing controllers .
- **Third-Party APIs** : Such as Stripe for payments and Telegram for notifications .
- **Scheduling feature** : To provide notification service .


## Provided controller's and related services functionalities
The following controllers with related services ensure the operation of the application and its functions :

- **AuthenticationController** Handles user registration and login : **Register a new user** with role "Customer" for basic abovesaid operations, can throw *RegistrationException*;  **Login** the user by email and password and can throw exception message if user not found ;
- **CarController** Manages car operations (add, view, update, delete) and available with 5 functions : **Create a new car** adds a new car and sets inventory to 1 but if such a car with specific details already exists increases its inventory by 1 , **Get all cars** into list with pagination and sorting, **Get car by ID** shows available information by car ID, **Delete car by ID** is available for *MANAGER* only and decreases inventory by 1 for a car with a specific ID, but if inventory riches zero delete this car, **Update car by ID** is also available for *MANAGER* only can update inventory and daily fee for a specific car. If a requested car ID is wrong the exception message is thrown ;
- **UserController** Manages user profiles and roles and available with 3 functions : **Get profile** to look at own profile including roles and rentals, **Update profile** to change the registration information, **Update user’s role by ID** is available for *MANAGER* only is to update the role of a specific user. If a requested user ID is wrong the exception message is thrown ;
- **RentalController** Manages rental operations and available with 4 functions : **Create new rental** creates a new rental for authenticated user with a specific car and desired rental/return dates, also sets status as *Active*, decreases the inventory by 1 for a selected car, sends *Telegram* notification with details of a new rental. In case of wrong rental/returns dates, wrong car ID or left car inventory is zero the exception message is thrown; **Get rental by ID** allows to look at rental’s details, in case of wrong rental ID or not owned rental the exception message is thrown; **Get rentals list by status isActive** supports *ROLE-BASED* functionality, so it gets a list of owned rentals for authenticated *Customer* user or gets a rentals list by specific user ID by authenticated *Manager* user. The request parameter *”isActive”* provides ability to get such a rentals with a specific statuses. If a requested rental ID is wrong the exception message is thrown ; **Set actual return date by rental ID** allows to set actual return date as current day when car is returned for authenticated user, also sets status as *NotActive*, increases the inventory by 1 for a related car. In case of wrong rental, car ID or selected rental is already not active  the exception message is thrown ;
Anoher functional issue provided by **Rental Service** is a *special scheduled function* as **Get All Overdue Rentals** to monitor *overdue rentals* twice a day and to send *Telegram notification* with *detailed* information about such rentals or notification to inform about no overdue rentals ;
- **PaymentController** Handles payment sessions and statuses with its 4 functions : **Creates Stripe Payment Session** checks for available rentals of authenticated user, calculates a *TOTAL* to pay for the specific rental and if it also has *FINE* type adds extra money to pay according to conditions of *FINE* payments, creates a *Stripe Payment Session* with a credit card and stores payment into DB ; **Get all payments list** supports *ROLE-BASED* functionality, so it gets a list of owned payments for authenticated *Customer* user or gets all payments list by specific user ID by authenticated *Manager* user ; **Handle Successful Payment** is to handle payment by Stripe session ID and if status is *PAID* updates appropriate payment status and sends *Telegram notification* about successful payment  ; **Handle Cancelled Payment** is to handle payment by Stripe session ID and  updates appropriate payment status. All the processes are ensured to throw specific exception message if error occurs during processing.

 
## Test's functionalities
**In the process of developing this application, the following 117 tests were developed to check the overall operation and functionality at the following levels :**
- **Service** : *CarService, RentalService, UserService, PaymentService*  - 51 tests in total ;
- **Repository** : *CarRepository, RentalRepository, UserRepository, PaymentRepository*  - 19 tests in total. Running these tests is possible while *Docker* is running ;
- **Controller** : *CarController, RentalController, AuthenticationController. UserController, PaymentController*  - 47 tests in total. Running these tests is possible while *Docker* is running .

## Install and Run the application
### Prerequisites
To install and run this project you will need to have on your machine :
- **MySQL database**
- [**Maven**](https://maven.apache.org)
- [**Docker**](https://www.docker.com)
- [**Postman**]( https://www.postman.com)

**Getting started** this Spring Boot app is possible in 2 ways : **Locally**  from command line prompt or using a **Docker** within an insulated container as below.
### Running from command line
1.	Connect to and Log in as a MySQL user from command line ```mysql – u root(or enter user_name) -p``` and enter your password;
2.	Create MySQL Schema for this app with a command : ```mysql> CREATE DATABASE car_sharing_app;``` ;
3.	*Note.* The application is running on local port ```3306```, make sure it is available;
4.	Make sure that **Maven** is already installed using command line : ```mvn -v``` command. And if it is not you need to install it first;
5.	In your destination folder with this app  run command ```mvn clean package``` to generate a .jar file;
6.	Change work directory with command ```cd target```and finally Run application with command : ```java -jar project-0.0.1-SNAPSHOT.jar```

### Running with Docker
This app is Docker ready! The **Dockerfile** and **docker-compose.yml** files are available at the root of the project.

**Docker** must be already installed on your PC. When starting the app with Docker, a container for the API and a container for MySQL data base are created.
1.	Connect to and Log in as a MySQL user from command line ```mysql – u root(or enter user_name) -p``` and enter your password;
2.	Create MySQL Schema for this app with a command : ```mysql> CREATE DATABASE car_sharing_app;``` ;
3.	Make sure that **Maven** is already installed using command line : ```mvn -v``` command. And if it is not you need to install it first;
4.	In your destination folder with this app  run command ```mvn clean package``` to generate a .jar file;
5.	In your root project folder create a text file named ```.env``` with the following :
```
MYSQL_ROOT_PASSWORD=your password to MySQL
MYSQL_DATABASE=car_sharing_app
DB_USER=root (or your MySQL user_name)
DB_PASSWORD=your password to MySQL

MYSQL_LOCAL_PORT=3308
MYSQL_DOCKER_PORT=3306
SPRING_LOCAL_PORT=8090
SPRING_DOCKER_PORT=8080
```
6.	In your root folder run a command ```docker -compose up``` .

## Usage with Postman
For easy start and using of the app’s features with *Postman* for instance, there are options to use already pre-installed user as *MANAGER* in *project changelog* for logging-in or to register and use as new user *CUSTOMER*. For the first option use the following parameters :
```
User with Role MANAGER:
“email” : “admin@example.com”
“password” : “12345678”
```
**The following are examples of how to use Postman requests to operate with the functions of application controllers :**

1.	**AuthenticationController** is available at :
   - ```http://localhost:8080/api/auth/register``` for [register](https://imgur.com/Bbbor6m) a user ;
   - ```http://localhost:8080/api/auth/login``` for [login](https://imgur.com/DQ9uUMt) a user .
2.	**CarController** is available at :
   - ```http://localhost:8080/cars``` for [add a new car](https://imgur.com/Zw61kot) , [add car if already exists)](https://imgur.com/RcmyUGQ) , [get all cars](https://imgur.com/qA1fCXJ) ;
   - ```http://localhost:8080/cars/{id}``` where {id} should be replaced with desired id for [get car by id](https://imgur.com/qA1fCXJ), [update car by id](https://imgur.com/AHtQMaC), [delete car by id](https://imgur.com/WfM88fN) .
3.	**RentalController** is available at :
  - ```http://localhost:8080/rentals``` for [add new rental](https://imgur.com/XedI18B) ;
  - ```http://localhost:8080/rentals{id}``` where {id} should be replaced with desired id for [get rental's info by id](https://imgur.com/6jHAmjs). It's *ROLE-BASED* fumction and the only *MANAGER* can see the information of another user but *CUSTOMER* sees only the owned ones ; 
  - ```http://localhost:8080/rentals{id}/return``` where {id} should be replaced with desired id for [set rental's actual return date](https://imgur.com/aG4VIUg) ;
  - ```http://localhost:8080/rentals?{QueryParams} ``` where is needed to assign {QueryParams} by  using on "Params" tab the "isActive" key with values "true" or "false" and "userId" as another key with values of desired user id (like pattern *http://localhost:8080/rentals?isActive=true&userId=1*) for [Get user's rentals with IsActive](https://imgur.com/KeBOtFe) .
4.	**UserController** is available at :
  - ```http://localhost:8080/users/me``` for [get own profile](https://imgur.com/Xpy0nmI) or [update user's profile](https://imgur.com/BNoS3HB) ;
  - ```http://localhost:8080/users/{id}```  where {id} should be replaced with desired user id for [update user's role by id](https://imgur.com/J6uFAmK) .
5.	**PaymentController** is available at :
  - ```http://http://localhost:8080/payments``` for [create session](https://imgur.com/lO2BKj1) ;
  - ```http://localhost:8080/payments?userId={value}``` where is needed to use a *userId* as a parameter added on Params with desired value for [get all payments by user id](https://imgur.com/CS9VPWA). It's *ROLE-BASED* fumction and the only *MANAGER* can see the information of another user but *CUSTOMER* sees only the owned ones; 
  - ```http://localhost:8080/payments/success?{sessionId}}``` where {sessionId} should be replaced on Params tab with a key parameter with value od session id for [get successful payment by session id](https://imgur.com/undefined) ;
  - ```http://localhost:8080/payments/cansel?{sessionId}}``` where {sessionId} should be replaced on Params tab with a key parameter with value od session id for [get canceled payment by session id](https://imgur.com/R6EOmgN) ;


## Contacts
For any inquiries or questions, feel free to reach out via email:

* **Yuriy Kononchuk** - *Java Developer Student of Mate Academy* - **Email:** [yurkononchuk@gmail.com](mailto:yurkononchuk@gmail.com)
