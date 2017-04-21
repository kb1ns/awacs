# AWACS
[![Build Status](https://travis-ci.org/Archerfeel/awacs.svg?branch=master)](https://travis-ci.org/Archerfeel/awacs)
[![License](https://img.shields.io/badge/license-APACHE2-blue.svg)](https://github.com/ArcherFeel/awacs/blob/master/LICENSE)
[![Version](https://img.shields.io/badge/AWACS-0.1.11-orange.svg)](https://github.com/ArcherFeel/awacs/tree/0.1.11)

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
	cd awacs
	git checkout 0.1.11
	mvn clean package -Dmaven.test.skip=true
	cd awacs-server/target && unzip awacs-server.zip
	```

2. AWACS defaults use MongoDB as data storage, so we need start MongoDB before
 
3. Open awacs-server/conf/awacs.properties and modify MongoDB's host and port(default is 127.0.0.1:27017)

4. Start AWACS

	```
	sh /path/to/awacs/awacs-server/bin/start.sh
	```
	
5. AWACS includes a simple springmvc webapp demo, simply start:

	```
	java -javaagent:/path/to/awacs/awacs-agent/target/awacs-agent.jar=http://127.0.0.1:7200 -jar /path/to/awacs/awacs-demo/target/awacs-demo-0.1.11.jar
	```

6. Test webapp and check MongoDB

	```
	curl 127.0.0.1:8080/v1/test/hello
	curl 127.0.0.1:8080/v1/img
	mongo
	db.stacktrace.find().pretty()
	```
	
	method call stack:
	
	```
	{
	"_id" : ObjectId("58fa2096c2aac76c70c1ca6e"),
	"stack" : {
		"caller" : "io.awacs.demo.TestController#test2",
		"subMethods" : [
			{
				"caller" : "io.awacs.demo.TestController#bis1",
				"subMethods" : [
					{
						"caller" : "io.awacs.demo.TestController#bis2",
						"subMethods" : [ ],
						"callCount" : 1,
						"timestamp" : NumberLong("1492787342081"),
						"elapsedTime" : 101
					}
				],
				"callCount" : 1,
				"timestamp" : NumberLong("1492787341581"),
				"elapsedTime" : 601
			}
		],
		"callCount" : 1,
		"timestamp" : NumberLong("1492787341581"),
		"elapsedTime" : 601
	},
	"host" : "127.0.0.1",
	"pid" : 93432,
	"thread" : "qtp1349414238-26",
	"timestamp" : 1492787342
}
```


## Documents

* [wiki]()
* [文档](https://github.com/archerfeel/awacs/wiki/Home_zh_CN)

## Contributors

* Zhang Yu [@archerfeel](https://github.com/archerfeel)
* An Tong [@antong530](https://github.com/antong530)
* Chen Lei [@dalvikchen](https://github.com/dalvikchen)
* Wang Li [@waally](https://github.com/waally)

## License

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

