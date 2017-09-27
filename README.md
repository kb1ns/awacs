# AWACS
[![Build Status](https://travis-ci.org/Archerfeel/awacs.svg?branch=master)](https://travis-ci.org/Archerfeel/awacs)
[![License](https://img.shields.io/badge/license-APACHE2-blue.svg)](https://github.com/ArcherFeel/awacs/blob/master/LICENSE)
[![Version](https://img.shields.io/badge/AWACS-0.2.2-orange.svg)](https://github.com/ArcherFeel/awacs/tree/0.2.2)


## Overview

AWACS is a non-intrusive Java APM.

## Features

* Realtime monitoring
* No SDK dependence for users
* Plugin based architecture
* Little performance effect
* Easy to deploy

Before transformed: 

```
public java.lang.String hello1(java.lang.String);
    descriptor: (Ljava/lang/String;)Ljava/lang/String;
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=3, args_size=2
         0: getstatic     #15                 // Field java/lang/System.out:Ljava/io/PrintStream;
         3: aload_1
         4: invokevirtual #16                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
         7: new           #2                  // class java/util/Random
        10: dup
        11: invokespecial #3                  // Method java/util/Random."<init>":()V
        14: astore_2
        15: aload_2
        16: invokevirtual #17                 // Method java/util/Random.nextInt:()I
        19: iconst_2
        20: irem
        21: ifne          27
        24: ldc           #18                 // String hello, world
        26: areturn
        27: ldc           #19                 // String hoho
        29: areturn
      LineNumberTable:
        line 68: 0
        line 69: 7
        line 70: 15
        line 71: 24
        line 72: 27
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      30     0  this   Lio/awacs/demo/TestController;
            0      30     1     s   Ljava/lang/String;
           15      15     2     r   Ljava/util/Random;
    RuntimeVisibleAnnotations:
      0: #71(#72=[s#85])
```

After transformed:

```
public java.lang.String hello1(java.lang.String);
    descriptor: (Ljava/lang/String;)Ljava/lang/String;
    flags: ACC_PUBLIC
    Code:
      stack=4, locals=4, args_size=2
         0: invokestatic  #38                 // Method io/awacs/plugin/stacktrace/CallStack.initStack:()V
         3: ldc           #40                 // String io.awacs.demo.TestController
         5: ldc           #117                // String hello1
         7: invokestatic  #45                 // Method io/awacs/plugin/stacktrace/CallStack.methodEnter:(Ljava/lang/String;Ljava/lang/String;)V
        10: getstatic     #94                 // Field java/lang/System.out:Ljava/io/PrintStream;
        13: aload_1
        14: invokevirtual #99                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        17: new           #19                 // class java/util/Random
        20: dup
        21: invokespecial #20                 // Method java/util/Random."<init>":()V
        24: astore_2
        25: aload_2
        26: invokevirtual #103                // Method java/util/Random.nextInt:()I
        29: iconst_2
        30: irem
        31: ifne          43
        34: ldc           #105                // String hello, world
        36: invokestatic  #77                 // Method io/awacs/plugin/stacktrace/CallStack.methodQuit:()V
        39: invokestatic  #82                 // Method io/awacs/plugin/stacktrace/StackTracePlugin.incrAccess:()V
        42: areturn
        43: ldc           #107                // String hoho
        45: invokestatic  #77                 // Method io/awacs/plugin/stacktrace/CallStack.methodQuit:()V
        48: invokestatic  #82                 // Method io/awacs/plugin/stacktrace/StackTracePlugin.incrAccess:()V
        51: areturn
        52: astore_2
        53: aload_2
        54: invokestatic  #86                 // Method io/awacs/plugin/stacktrace/StackTracePlugin.incrFailure:(Ljava/lang/Throwable;)V
        57: aload_2
        58: athrow
      Exception table:
         from    to  target type
            10    42    52   Class java/lang/Exception
            43    51    52   Class java/lang/Exception
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
           10      42     0  this   Lio/awacs/demo/TestController;
           10      42     1     s   Ljava/lang/String;
           25      27     2     r   Ljava/util/Random;
      LineNumberTable:
        line 68: 10
        line 69: 17
        line 70: 25
        line 71: 34
        line 72: 43
Error: java.lang.reflect.InvocationTargetException
        StackMap: length = 0x6
         00 01 74 07 00 21
    RuntimeVisibleAnnotations:
      0: #7(#8=[s#116])
```

## Documents

* [wiki]()
* [文档,慢慢更新中]()

## Contributors

* Zhang Yu [@archerfeel](https://github.com/archerfeel)
* An Tong [@antong530](https://github.com/antong530)
* Chen Lei [@dalvikchen](https://github.com/dalvikchen)
* Wang Li [@waally](https://github.com/waally)
* Zhang Zun [@jcbay](https://github.com/jcbay)

## License

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

