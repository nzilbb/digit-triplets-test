# digit-triplets-test

Digit Triplets Hearing Screening Test developed at the
[New Zealand Institute of Language, Brain and Behaviour](https://www.canterbury.ac.nz/nzilbb/)

See:
- [Sharon M. King, *Development and Evaluation of a New Zealand Digit Triplet Test for
Auditory Screening*](https://ir.canterbury.ac.nz/bitstream/handle/10092/8084/thesis_fulltext.pdf),
- [Alice Bowden, *Normalisation, Evaluation and Verification  of the New Zealand Digit Triplet Test.*](https://ir.canterbury.ac.nz/bitstream/handle/10092/8084/thesis_fulltext.pdf),
and 
- [Christa Murray, *Development of a Māori Language Version of the New Zealand Hearing Screening Test*](https://ir.canterbury.ac.nz/bitstream/handle/10092/7132/CMurray_MAudThesis.pdf;sequence=1)

## Installation

Prerequisites for installation:
1. A server with [Apache Tomcat](https://tomcat.apache.org/) installed.
2. A server with [MySQL](https://www.mysql.com/) installed.
3. A set of digit triplet recordings for your test, with appropriate noise levels

The digit triplets recordings must be named using the convention:  
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

Finally, in the `dtt` directory, there should be a file called `sound-check.mp3` which is
played to the participant during the sound check before starting the test.

These three directories of mp3 files must be compressed into a single zip file for
installation on the server.

### Installation steps:

1. Copy digit-triplets-test.war into the *webapps* directory of the Tomcat installation.
2. In your browser, open http://myservername/digit-triplets-test/install  
   (or possibly http://myservername:8080/digit-triplets-test/install depending on your server setup)
3. Fill in the MySQL details the form asks for.
4. Click *Install*
5. Restart Tomcat to complete the installation.
6. Log in to http://myservername:8080/digit-triplets-test/admin using the username `admin`
   and password `admin`.
7. Click the *Media* option on the menu to upload the zip file containing all your triplet
   recordings. 

Once this is done, you will need to check/adjust the settings under
http://myservername:8080/digit-triplets-test/admin

The administration interface is where you can:

- fine-tune test parameters (signal level increments, normal/poor hearing thresholds, etc.)
- add fields for meta-data you want to collect from participants
- specify the text of the different prompts and results pages that will be presented to
   participants,
- specify 'trial sets' - groups of digit triplets that can be used together in a given
   test instance,
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

##### Localizing to other languages

Most of the text that appears in the app is configured using the Admin app. There are a
small number of exceptions, mainly button text and hints. These are defined by
localization resource files in *client/dtt/src/locale*

If you add new *i18n* marked tags, using the following to ensure they appear in the
internationalization resource files:

```
ng extract-i18n --output-path src/locale
```

If you want to translate the digit triplets test to another language, you must:

1. Provide the resource file for the language, in a file named
   *client/dtt/src/locale/messages.ISO-CODE.xlf* where *ISO-CODE* is the ISO-639 code for
   the language you want to add.
2. Register the new language in *client/dtt/angular.json* by adding an entry to the
   "locales" setting indicating the location of the translated resources. 
3. Set the "locale" property in *build.xml* to be the ISO-639 code for the new language.

After installing the .war file on the server, you must then set the texts, prompts,
etc. for the new language.