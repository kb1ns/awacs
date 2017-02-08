# AWACS
[![Build Status](https://travis-ci.org/ArcherFeel/AWACS.svg?branch=master)](https://travis-ci.org/ArcherFeel/awacs)
[![License](https://img.shields.io/badge/license-APACHE2-blue.svg)](https://github.com/ArcherFeel/awacs/blob/master/LICENSE)
[![Version](https://img.shields.io/badge/AWACS-0.1.9-orange.svg)](https://github.com/ArcherFeel/awacs/tree/0.1.9)

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
 
3. Open awacs-server/conf/awacs.properties and modify MongoDB's host and port(default is 127.0.0.1:27017)

4. Start AWACS

	```
	sh /path/to/awacs/awacs-server/bin/start.sh
	```
	
5. AWACS includes a simple springmvc webapp demo, simply start:

	```
	java -javaagent:/path/to/awacs/awacs-agent/target/awacs-agent.jar=http://127.0.0.1:7200 -jar /path/to/awacs/awacs-demo/target/awacs-demo-0.1.8.jar
	```

6. Test webapp and check MongoDB

	```
	curl 127.0.0.1:8080/v1/test/hello
	curl 127.0.0.1:8080/v1/img
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

