# digit-triplets-test
Digit Triplets Hearing Test

## Server

The server is a Java webapp.

Prerequisites for building:
1. JDK
2. Apache ant
3. Apache Tomcat
4. The prerequisites for building the client (see below)

To build the web application archive (war):

```
ant
```

The default build includes the client application build too.

## Client

Clients are [Angular](https://angular.io/) apps.

Prerequisites:

1. Node and npm
2. Angular CLI
   `npm install -g @angular/cli`


### Admin

To run for debugging:

```
cd client/admin
ng serve --open
```

