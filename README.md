# Introduction
This sample application was developed to provide an example for implementing authentication and authorization for
Dropwizard. Included in this sample are TLS Client Authentication service auth and OpenID Connect user auth.

The commit history should give an idea of what is needed for each step of authentication / authorization implemented
here. The remainder of this README will also change as the git history changes and authentication features are added.

# Overview
[![Build Status](https://travis-ci.org/wdawson/dropwizard-auth-example.svg?branch=master)](https://travis-ci.org/wdawson/dropwizard-auth-example)

## What the app does
The functionality of the application is fairly simple; The application serves information about users. As the backend
data store is not the focus of this application, a simple, static, text file is used to store information with no update
functionality.

- The `UserInfo` class is the representation of the user.
- The endpoints for getting the user info data from the service are in the `UserInfoResource` class.
- `GET /users` is the REST endpoint to call for a list of all the users.
- `GET /users/{id}` is the REST endpoint to call for a specific user, where `{id}` is the line number in the text file.
- `UserInfoApplication` contains the `main` method for the application and is the Application for Dropwizard to execute.
- The configuration object, `UserInfoConfiguration`, contains various config data needed for the application.

The application runs on port 8443, with the Dropwizard admin on port 8081.

## Auth
This application authenticates clients in two ways:

1. It authenticates services via TLS Client Authentication and performs further authentication on the client's
certificate using the `TLSCertificateAuthorizationFilter`. The authorization is a simple regex match on the certificate
subject. This could be extended to check additional claims passed in the certificate. Hostname verification is not
included in this example, but is another option for providing more assurance.
2. It authorizes users via OAuth 2.0 implemented with JWTs. This example does not authenticate users and instead relies
on the delegated authorization of the OAuth 2.0 spec to federate the user's identity back to the signer of the JWT (the
OAuth token).

The application uses certificates issued by an example CA. In order for the application to start, the JVM must trust the
root CA. In order to accomplish this, the application overrides Java's trustStore at runtime with a custom keystore
file. The password is "notsecret" should you need to edit it. WARNING: DO NOT MAKE JAVA TRUST THIS STORE BY DEFAULT! The
CA that's backing this example is not secure for public communication and is meant for demonstration only.

The CA supports both OCSP and CRL and access \[root|intermediate\].example.ca.wilsdawson.com to perform revocation
checks.

# Running the application
NOTE: You must install the Unlimited JCE to run the tests!

To test the application, run the following commands.

- To package the application, run:

  ```
  mvn package
  ```
  
- To run the integration tests, run:
  ```
  mvn verify
  ```

- To run the server, run:

  ```
  java -jar target/auth-example-0.1.0-SNAPSHOT.jar server conf.yml
  ```

- To get all the users once the server is up, run:

  ```
  curl http://localhost:8443/users
  ```

- To get the user with an id of `{id}` when the server is up, run:

  ```
  curl http://localhost:8443/users/{id}
  ```

- To use the admin operational menu, navigate a browser to:

  ```
  http://localhost:8081
  ```
