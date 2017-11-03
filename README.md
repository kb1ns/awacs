# AWACS
[![Build Status](https://travis-ci.org/Archerfeel/awacs.svg?branch=master)](https://travis-ci.org/Archerfeel/awacs)
[![License](https://img.shields.io/badge/license-APACHE2-blue.svg)](https://github.com/ArcherFeel/awacs/blob/master/LICENSE)
[![Version](https://img.shields.io/badge/AWACS-0.2.3-orange.svg)](https://github.com/ArcherFeel/awacs/tree/0.2.3)


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
         0: new           #2                  // class java/util/Random
         3: dup
         4: invokespecial #3                  // Method java/util/Random."<init>":()V
         7: astore_2
         8: aload_2
         9: invokevirtual #19                 // Method java/util/Random.nextInt:()I
        12: iconst_2
        13: irem
        14: ifne          20
        17: ldc           #23                 // String hello, world
        19: areturn
        20: new           #10                 // class java/lang/RuntimeException
        23: dup
        24: invokespecial #21                 // Method java/lang/RuntimeException."<init>":()V
        27: athrow
      LineNumberTable:
        line 76: 0
        line 77: 8
        line 78: 17
        line 79: 20
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      28     0  this   Lio/awacs/demo/TestController;
            0      28     1     s   Ljava/lang/String;
            8      20     2     r   Ljava/util/Random;
      StackMapTable: number_of_entries = 1
        frame_type = 252 /* append */
          offset_delta = 20
          locals = [ class java/util/Random ]
    RuntimeVisibleAnnotations:
      0: #79(#80=[s#95])
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
         5: ldc           #123                // String hello1
         7: invokestatic  #45                 // Method io/awacs/plugin/stacktrace/CallStack.methodEnter:(Ljava/lang/String;Ljava/lang/String;)V
        10: new           #19                 // class java/util/Random
        13: dup
        14: invokespecial #20                 // Method java/util/Random."<init>":()V
        17: astore_2
        18: aload_2
        19: invokevirtual #111                // Method java/util/Random.nextInt:()I
        22: iconst_2
        23: irem
        24: ifne          36
        27: ldc           #125                // String hello, world
        29: invokestatic  #77                 // Method io/awacs/plugin/stacktrace/CallStack.methodQuit:()V
        32: invokestatic  #80                 // Method io/awacs/plugin/stacktrace/CallStack.incrAccess:()V
        35: areturn
        36: new           #33                 // class java/lang/RuntimeException
        39: dup
        40: invokespecial #114                // Method java/lang/RuntimeException."<init>":()V
        43: invokestatic  #77                 // Method io/awacs/plugin/stacktrace/CallStack.methodQuit:()V
        46: invokestatic  #80                 // Method io/awacs/plugin/stacktrace/CallStack.incrAccess:()V
        49: athrow
        50: astore_2
        51: aload_2
        52: invokestatic  #101                // Method io/awacs/plugin/stacktrace/CallStack.incrFailure:(Ljava/lang/Throwable;)V
        55: aload_2
        56: athrow
      Exception table:
         from    to  target type
            10    35    50   Class java/lang/RuntimeException
            36    49    50   Class java/lang/RuntimeException
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
           10      40     0  this   Lio/awacs/demo/TestController;
           10      40     1     s   Ljava/lang/String;
           18      32     2     r   Ljava/util/Random;
      LineNumberTable:
        line 76: 10
        line 77: 18
        line 78: 27
        line 79: 36
      StackMapTable: number_of_entries = 2
        frame_type = 252 /* append */
          offset_delta = 36
          locals = [ class java/util/Random ]
        frame_type = 255 /* full_frame */
          offset_delta = 13
          locals = [ class io/awacs/demo/TestController, class java/lang/String ]
          stack = [ class java/lang/RuntimeException ]
    RuntimeVisibleAnnotations:
      0: #7(#8=[s#122])
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

