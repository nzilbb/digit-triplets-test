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

To run during development you can compile the client directly into the webapp location, e.g.:

```
cd client/admin
ng build --output-path /var/lib/tomcat9/webapps/digit-triplets-test/admin/
```

If instead you want to run with automatic compilation when files are changed, you must:

1. Have a debug build of the server installed locally.  This can be built using:
   `ant dev-war`  
   (If you already have a production build of the server running, change its `web.xml` file
   to match `server/WEB-INF/web-debug.xml`)
2. Ensure the *baseUrl* setting in `client/admin/src/environments/environment.ts` to
   matches the locally-running server - e.g.
   `baseUrl: "http://localhost:8080/digit-triplets-test/admin/"`

Once these prerequisites are met, you can run:

```
cd client/admin
ng serve
```

...and then browse to (http://localhost:4200/) for a live version of the client which
recompiles when you save changes in the client source code.

