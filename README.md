# digit-triplets-test
Digit Triplets Hearing Test

## Installation

Prerequisites for installation:
1. A server with [Apache Tomcat](https://tomcat.apache.org/) installed.
2. A server with [MySQL](https://www.mysql.com/) installed.
3. A set of digit triplet recordings for your test, with appropriate noise levels

The digit triplets recordings must be named using the covention:  
`{triplet}_{db}{mode}`.mp3

Where:

- `{triplet}` is the digits represented by the recording (3 characters), and
- `{db}` is the decibel level of the voice (3 characters).
- `{mode}` indicates whether the recording is in the left channel - "l" - the right
channel - "r" - or both channels - "".

e.g.
- `123_004l.mp3` is a recording of the digits 1, 2, 3 with a 4 decibel voice, in the left
channel, and
- `126_-22.mp3` is a recording of the digits 1, 2, 6 with a -22 decibel voice, in both
channels.

Furthemore, the recording should be in three separate directories:

- `dttl` for recording in the left channel.
- `dttr` for recording in the right channel.
- `dtt` for recording in both channels.

### Installation steps:

1. Copy digit-triplets-test.war into the *webapps* directory of the Tomcat installation.
2. In your browser, open http://myservername/digit-triplets-test/install  
   (or possibly http://myservername:8080/digit-triplets-test/install depending on your server setup)
3. Fill in the MySQL details the form asks for.
4. Click *Install*
5. Copy the three directories of recordings (`dttl`, `dttr`, and `dtt`) into the `mp3`
directory that has been created in the `digit-triplets-tes` directory on the server that
the web application has been installed into. e.g. on a Linux system, you might run a
command like:
`mv dttl dttr dtt /var/lib/tomcat9/webapps/digit-triplets-test/mp3/`

Once this is done, you will need to check/adjust the settings, by using your browser to
log in to http://myservername:8080/digit-triplets-test/admin

(The first time you log in, use the username `admin` and password `admin`)

The administration interface is where you can:

- add fields for meta-data you want to collect from participants
- specify the text of the different prompts and results pages that will be presented to
   participants,
- specify 'trial sets' - groups of digit triplets that can be used in a given test
   instance,
- fine-tune test parameters (signal level increments, normal/poor hearing thresholds, etc.)
- list the data collected from instances of the test completed by past participants, 
- create other administration users, and
- upgrade the web application to a new version.

For participants, the test app is available at  
http://myservername/digit-triplets-test/

## Build/Develop from source code:

### Server

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

The default build includes the client application builds too, and creates:  
`bin/digit-triplets-test.war`

For development/debugging of the administraion client, you may prefer a version of the
webapp that doesn't include admin user auth (so you can use `ng serve` as below), and
doesn't spend time building both clients every time. In that case, use the following ant
target instead: 

```
ant dev-war
```

This creates:  
`bin/digit-triplets-test-dev.war`

### Client

Clients are [Angular](https://angular.io/) apps.

Prerequisites:

1. Node and npm
2. Angular CLI
   `npm install -g @angular/cli`


#### Admin

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

#### Digit Triplets Test

To run with automatic compilation when files are changed:

```
cd client/dtt
ng serve
```

...and then browse to (http://localhost:4200/) for a live version of the client which
recompiles when you save changes in the client source code.

