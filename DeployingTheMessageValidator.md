# Introduction #

The following steps are followed to deploye the message validator:

  * Ensure that you are up to date from SVN
  * Build the project with Maven

```
mvn -P LISTENER install
```

  * Log into: http://uhnvesb02d.uhn.on.ca:17448/
  * Click on web applications
  * Check off "cGTA Input Listner" and click undeploy
  * wait a bit
  * Click Deploy
  * Find "cGTA\_Input\_Listener.war"
  * Click "OK"