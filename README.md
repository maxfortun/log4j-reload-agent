# log4j-reload-agent
Java agent for reloading [log4j 1](https://logging.apache.org/log4j/1.x/) configuration on change without restarting the app.  

Some may say that this project comes a day late and a dollar short.  
I say it is what it is.  

Yes, log4j 1 may be an anachronism, but it is a rather ubiquitous one. It is present in multitude of opensource projects, and just because it is [EOL](https://en.wikipedia.org/wiki/End-of-life_product), doesn't mean it is not relevant. Thus, here we are.  

When running any such project that still uses log4j 1, sometimes, for troubleshooting, all we want is to change the log level w/o restarting the app. One way to achieve this is to invasively change the code and add the logic to apply log4j configuration changes directly to the app. Another, non-invasive method is to use a [java agent](https://www.jrebel.com/blog/how-write-javaagent). This is the very agent that does exactly that.  

## Build
```
./gradlew jar
```

## Use
Place the jar file in a location outside of the classpath. Then add a `-javaagent` parameter to your java call.
```
java ... -javaagent:/path/to/jar/log4j-reload-agent-0.0.7.jar ...
```
The agent will try to determine the location of the log4j.properties file automatically. If it can't do that and fails, you may want to pass the location of the file explicitly, by specifying a `file` argument.
```
java ... -javaagent:/path/to/jar/log4j-reload-agent-0.0.7.jar=file=file:/path/to/log4j.properties ...
```

That's it. Now run your application, and any change to log4j.properties will get automatically picked up.

Know a better way?
Please share!

### Bonus
There is a drop-in replacement for log4j1 that fixes most of the security bugs in the abandoned project. Check out [reload4j](https://reload4j.qos.ch).


