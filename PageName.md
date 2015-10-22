# Getting Started #

More details to come.

Check out and build the project:

  * Check out the source in Eclipse
  * Build the project for the first time using mvn:
```
mvn -P CONVERTER install
```
  * If needed, add the M2\_REPO eclipe variable per the instructions here: http://www.mkyong.com/maven/how-to-configure-m2_repo-variable-in-eclipse-ide/

The message validator module is in a class called "Converter". A simple demonstration of the converter in action is in "ConverterTest#testCanonicalProvidersAreReadCorrectly()".

Most actual unit tests for the Converter require a configured instance of CouchDB to be running, so these are not enabled (yet).