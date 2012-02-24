# SQLite to XML/JSON exporter



## Introduction
sqlite-exporter is a Java window application that exports a target SQLite database to XML and/or JSON.

The code has been written so that it is easy to create a command line version. I'll leave it to other industrious Open Source contributors to make the appropriate changes



## Licensing
This project is licensed under the BSD license.



## Contributors
- Chris Khoo ([Wappworks Studio](http://www.wappworks.com))



## Using sqlite-exporter

### Software requirements
- Java 6 or higher to be installed on the machine

### Launching the application
From the commandline:

- Go to the bin subdirectory
- Run 'java -jar sqlite-exporter.jar'. The application window will appear.

Through the Windows explorer:

- Double click on 'sqlite-exporter.jar'. The application window will appear.

### Filtering the export
The application's export is filtered by setting up a JSON-encoded export configuration file. The primary sections are as follows and they are all optional:

- common: Common configuration settings that apply to all exports
- xml: XML export specific configuration
- json: JSON export specific configuration

Each primary section may include the following configuration options:

- keys: A JSON-encoded object containing key-value pairs mapping table names to the name of the primary field
- excludes: A JSON-encoded array containing a list of tables and/or fields to exclude from export

The following is a sample configuration file which:

- for all exports,
  - exclude the 'test' table
  - exclude the 'id' field from the 'main' table
- for JSON exports,
  - use 'name' as the primary field for the 'main' table export

> {

>   common: {

>     excludes: [

>       "test",

>       "main.id"

>     ]

>   },

>   xml: {

>     keys: {

>       main: "name"

>     }

>   }

> }

### XML output format
- The root XML node is always 'database'.
- Each child root corresponds to each table in the database and is named accordingly
- Within each XML table root, the child roots represent the table records. The XML node name is the table name suffixed with 'Record'
- Within each XML record node, each child node represent the record fields and are named accordingly  

### JSON output format
- The top most level is a JSON object.
- Each entry in the top JSON object represents a table with the key representing the table name, and the value representing the table record list  
- If the table's primary field is unspecified or missing, the value is a JSON array with each JSON object entry representing a record JSON object. Otherwise, the value is a JSON object with each key-value pair represents a map of the primary field value to a record JSON object.
- Finally, each record JSON object is composed of key-value pairs representing the record fields.


 
## Development Environment

### Third party library dependencies
The application is incorporates the following third party libraries:

- JSON in Java: [official site](http://json.org/java/)
- SQLite JDBC: [official site](http://www.zentus.com/sqlitejdbc/)


### Build environment
The application is built through Eclipse. The build process also relies on Apache Ant.