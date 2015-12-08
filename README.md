# Introduction
This sample application was developed to provide an example for implementing authentication and authorization for
Dropwizard. Included in this sample are TLS Client Authentication service auth and OpenID Connect user auth.

# Overview
This application authenticates it's clients in two ways:

1. Service authentication is performed via TLS Client Authentication with another layer of application layer filtering.
2. User authentication and authorization is done via JWTs and OpenID Connect, which is a protocol based on OAuth 2.0

The commit history should give an idea of what is needed for each step of authentication / authorization implemented
here.

The functionality of the application is fairly simple; The application serves information about users. As the backend
data store is not the focus of this application, a simple, static, text file is used to store information with no update
functionality.

- The `UserInfo` class is the representation of the user.
- The endpoints for getting the user info data from the service are in the `UserInfoResource` class.
- `GET /users` is the REST endpoint to call for a list of all the users.
- `GET /users/{id}` is the REST endpoint to call for a specific user, where `{id}` is the line number in the text file.
- `UserInfoApplication` contains the `main` method for the application and is the Application for Dropwizard to execute.
- The configuration object, `UserInfoConfiguration`, contains various config data needed for the application.

The application runs on port 8080, with the dropwizard admin on port 8081.

# Running the application
To test the application, run the following commands.

- To package the application, run:

  ```
  mvn package
  ```

- To run the server, run:

  ```
  java -jar target/auth-example-0.0.1-SNAPSHOT.jar server conf.yml
  ```

- To get all the users, run:

  ```
  curl http://localhost:8080/users
  ```

- To get all the user with an id of `{id}`, run:

  ```
  curl http://localhost:8080/users/{id}
  ```

- To use the admin operational menu, navigate a browser to:

  ```
  http://localhost:8081
  ```
