# AWACS


## Overview

AWACS is a non-intrusive APM for Java platform.

## Features

* Real-time monitoring and alarm
* No code intrusion(official plugin)
* Plugin based architecture
* Little performance impact
* Support custom monitor plugin and data storage

## Quickstart

1. Compile & Extract

	```
	cd awacs && mvn clean package
	cd awacs-server/target && unzip awacs-server.zip
	```

2. AWACS defaults use MongoDB as data storage, so we need start MongoDB before
 
3. Open conf/awacs.properties and modify MongoDB's host and port(default is 127.0.0.1:27017)

4. Start AWACS

	```
	sh /path/to/awacs/awacs-server/bin/start.sh
	```
	
5. AWACS includes a simple springmvc webapp demo, simply start:

	```
	java -javaagent:/path/to/awacs/awacs-agent/target/awacs-agent.jar=http://127.0.0.1:7200 -jar /path/to/awacs/awacs-demo/target/awacs-demo-0.1.0.jar
	```

6. Test webapp and check MongoDB

	```
	curl 127.0.0.1:8080/v1/test1?name=test
	curl 127.0.0.1:8080/v1/test2/hello
	```

## Plugins

So far, we provide springmvc plugin(for Spring webapp QPS) and mxbean plugin(for JVM index).

## Documents

* [wiki]()
* [user guide]()
* [contributor guide]()

## Deploy

Coming soon...

## Contributors

* Zhang Yu [@archerfeel](https://github.com/archerfeel)
* An Tong [@antong530](https://github.com/antong530)
* Chen Lei [@dalvikchen](https://github.com/dalvikchen)
* Wang Li [@waally](https://github.com/waally)

## License

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

