# 1 内存结构

## 1.1 程序计数器

![image-20231222142726484](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221427972.png)

### 1.1.1 定义

Program Counter Register 程序计数器（寄存器）

作用：是记住下一条 `jvm` 指令的执行地址

特点：

- 是线程私有的（每个线程独有自己的一份）
- 不会存在内存溢出

### 1.1.2 作用

记住下一条 `jvm` 指令的执行地址 (`0,3,4,5,...`)

![image-20231222143054213](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221430597.png)

线程私有的：

每个线程都有一个自己的程序计数器，里面存储了自己线程运行到了哪条指令

![image-20231222144028283](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221440501.png)

## 1.2 虚拟机栈

![image-20231222144244229](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221442280.png)

### 1.2.1 定义

Java Virtual Machine Stacks (Java 虚拟机栈)

- 每个线程运行时所需要的内存，称为虚拟机栈
- 每个栈由多个栈帧(Frame)组成，对应着每次方法调用所占用的内存
- 每个线程只有一个活动栈帧，对应当前正在执行的那个方法

栈：线程运行需要的内存空间

栈中存储着多个栈帧，每个栈帧对应着一个调用过的方法，栈顶为活动栈帧，是当前正在运行的函数；当一个方法运行完成，这个方法对应的栈帧就会出栈

<img src="https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221445960.png" alt="image-20231222144505865" style="zoom:50%;" />

问题辨析
1. 垃圾回收是否涉及栈内存？

   不需要，每次方法结束时，栈内存就被回收掉了，不需要等待垃圾回收；垃圾回收是用来回收堆内存中的对象的
2. 栈内存分配越大越好吗？

   - 可以用 `-Xss size` 指令设置栈内存的大小
   - Linux & macOS默认 1024 KB，Windows 依赖虚拟内存的大小
3. 方法内的局部变量是否线程安全？

  - 如果方法内局部变量没有逃离方法的作用访问，是线程私有的，它是线程安全的

    ![image-20231222145641730](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221456902.png)

  - 如果是局部变量引用了对象，对象是共有的，并逃离方法的作用范围，需要考虑线程安全

    ![image-20231222145625818](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221456894.png)

```java
/**
 * 局部变量的线程安全问题
 */

public class d1_local_var_safe {
    // 多个线程执行这个方法
    public static void method1(String[] args) {
        int x = 0;
        for (int i = 0; i < 5000; i++) {
            x++;
        }
        System.out.println(x);
    }
}
```

### 1.2.2  栈内存溢出

- 栈帧过多导致栈内存溢出。例如方法的递归调用

<img src="https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221522112.png" alt="image-20231222152256064" style="zoom:50%;" />

- 栈帧过大导致栈内存溢出

### 1.2.3  线程运行诊断

案例一：CPU 占用过高

- 可以用 top 命令定位哪个进程对 CPU 的占用过高

- 找到进程后，我们想进一步的定位到具体的线程，可以用 `ps` 命令
  - `ps H pid,tid,%cpu` 可以把当前所有线程的 `pid,tid,cpu`的信息展示出来
  - 使用管道过滤出具体的进程号：`ps h -eo pid,tid,%cpu | grep 32655`
- `jstack 进程id` 命令可以列出Java虚拟机中的所有的 Java 线程
  - 根据 `ps` 命令找到的线程号可以在 `jstack` 中找到对应的线程，里面给出该线程的状态和问题代码行号

-----

案例二：程序运行很长时间没有结果

可能发生了死锁等等

使用 `jstack` 命令进行检查

## 1.3 本地方法栈

![image-20231222154054304](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221540433.png)

我们可以通过本地方法调用一些用`C/CPP`写的更底层的方法，例如Object类中的`clone` , `hashCode`... 方法

## 1.4 堆

![image-20231222154417878](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221544005.png)

### 1.4.1  定义

Heap 堆：

- 通过 new 关键词创建对象会使用堆内存

特点：

- 他是线程共享的，堆中对象需要考虑线程安全问题
- 堆中有垃圾回收机制

### 1.4.2 堆内存溢出

不断的向堆中添加对象，且这些对象无法回收，一段时间后会导致堆内存溢出

### 1.4.3 堆内存诊断

```java
package com.rainsun.d1_Java_memory_structure;
/**
 * 堆内存演示
 */
public class d2_heap_memory {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("1.....");
        Thread.sleep(30000);
        byte[] array = new byte[1024 * 1024 * 10]; // 10Mb 空间
        System.out.println("2.....");
        Thread.sleep(30000);
        array = null;
        System.gc();
        System.out.println("3.....");
        Thread.sleep(100000L);
    }
}
```

查看堆内存信息：

- `jps` 找到当前运行的线程id
- ` jhsdb jmap --pid 线程id --heap` 输出堆占用信息

1:

![image-20231222160549701](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221605790.png)

2:

![image-20231222160605952](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221606014.png)

3:没有G1 Heap了

- jconsole 工具

  图形界面的，多功能的监测工具，可以连续监测

## 1.5 方法区

![image-20231222161551426](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221615556.png)

方法区是线程共享的，存储类相关的信息（方法，构造器，特殊方法，运行时常量池等等）

[Chapter 2. The Structure of the Java Virtual Machine (oracle.com)](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html)

![image-20231222161731756](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221617891.png)

**JDK1.6中的结构：**

使用 `PermGen `永久代实现

![image-20231222161942216](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221619260.png)

**JDK1.8中的结构：**

元空间实现，使用操作系统中的本地内存实现：

![image-20231222162115139](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221621237.png)

### 1.5.4 运行时常量池

- 常量池，就是一张表，虚拟机指令根据这张常量表找到要执行的类名、方法名、参数类型、字面量等信息
- 运行时常量池：
  - 常量池是 *.class 文件中的；
  - 当程序运行的时候，每个被加载的类，它的.class文件中常量池信息就都会放入运行时常量池，运行时常量池保存了所有被加载了的类中的常量池信息
  - 并把里面的符号地址变为真实地址

### 1.5.5 StringTable

```java
public static void main(String[] args) {
    String s1 = "a";
    String s2 = "b";
    String s3 = "a" + "b";
    String s4 = s1 + s2;
}
```

反编译：

- 常量池中的信息，都会被加载到运行时常量池中，这时 a , b 都是常量池中的符号，还没有变成 Java 字符串对象
- ldc #2 会把 a 符号变为 "a" 字符串对象，并把 "a" 放入 StringTable 中，如果没有则加入新的，有则不加入新的

s1，s2，s3 字符串对象的产生是发生在**字符串常量池的 StringTable 中**的

s4 字符串对象的产生是通过调用两次 `makeConcatWithConstants` 方法将常量池中的 "a" ，"b" 进行拼接，在**堆中创建了一个新的字符串变量**，最后存入临时变量 s4中的。

![image-20231222174240646](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312221742827.png)

> 这是 JDK 21 反编译的结果，JDK 8 不一样，可能是 append.append.toString

**StringTable 特性**：

- 常量池中的字符串仅是符号，第一次用到时才变为对象
- 利用串池的机制，来避免重复创建字符串对象
- 字符串**变量**拼接的原理是 StringBuilder （1.8）
- 字符串**常量**拼接的原理是编译期优化
- 可以使用 intern 方法，主动将串池中还没有的字符串对象放入串池

```java
public static main(String[] args){
    String s = new String("a") + new String("b");
}
```

StringTable 中创建了字符串对象 ["a"，"b"]

在堆中产生了字符串对象，因为使用了 new 来创建 "a"，"b" ，"ab"

**intern**:

将字符串对象放入 StringTable，如果table中有则不会放入，没有则放入，并把 table 中的对象返回

```java
String s2 = s.intern(); // 堆中的 "ab" 被放入了 StringTable中了, 并将table中的ab返回
```

```java
public class d3_stringTable {
    // 常量池中的信息，都会被加载到运行时常量池中，这时 a , b 都是常量池中的符号，还没有变成 Java 字符串对象
    // ldc #2 会把 a 符号变为 "a" 字符串对象，并把 "a" 放入 StringTable 中，如果没有则加入新的，有则不加入新的
    public static void main(String[] args) {
        String s1 = "a";
        String s2 = "b";
        String s3 = "a" + "b"; //javac在编译期间的优化，变成了 "ab"，结果在编译期就已经确定了
        String s4 = s1 + s2; // 堆中的ab: new String("ab")

        String s5 = "ab"; // == s3 在table中的ab
        String s6 = s4.intern(); // table中ab已经有了，返回table中的ab

        System.out.println(s3 == s4); //false
        System.out.println(s3 == s5); // true
        System.out.println(s3 == s6); // true

        String x2 = new String("c") + new String("d");
        String x1 = "cd";
        x2.intern();  // table中 cd 已经存在了，x2放不进去table，x2还是堆中的cd
        System.out.println(x1 == x2); // false

        /** 最后两行代码的位置调换：
         * String x2 = new String("c") + new String("d");
         * x2.intern(); // table 中 cd 不存在，x2放入table，x2 变成了table中的cd
         * String x1 = "cd"; // x1 用的是table中的cd
         * System.out.println(x1 == x2); // true
         */
    }
}
```

### 1.5.6 StringTable 位置

![image-20231222200203414](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312222002528.png)

1.6 中的StringTable在PermGen永久代中实现，它的回收时机比较晚，容易造成永久代的内存空间不足

在1.8中就将StringTable放入堆Heap中实现，它的垃圾回收时机触发比较早，可以更快回收不用的字符串空间

![image-20231222162115139](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312222006208.png)

### 1.5.7 StringTable 垃圾回收

那些没有用到的字符串常量会被垃圾回收

```java
package com.rainsun.d1_Java_memory_structure;

/**
 * 演示 StringTable垃圾回收
 * -Xmx10m
 * -XX:+PrintStringTableStatistics : 打印 StringTable 信息
 * -XX:+PrintGCDetails -verbose:gc ：打印垃圾回收信息
 */
public class d4_stringTable_GC {
    public static void main(String[] args) {
        int i = 0;
        try {
            for (int j = 0; j < 100000; j++) {
                String.valueOf(j).intern();
                i++;
            }
        }catch (Throwable e){
            e.printStackTrace();
        }finally {
            System.out.println(i);
        }
    }
}
```

![image-20231222202328823](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312222023865.png)

### 1.5.8 StringTable 调优

1. `-XX:StringTableSize=桶个数` 可以设置String Table 中桶的个数，类似于一个哈希表，当表越大，每个桶中链表的长度就越小，查找的速度就越快
2. 考虑将字符串对象是否入池
   - 如果字符串大量重复可以让字符串入池，减少重复字符串存在在heap中

## 1.6 使用操作系统中的直接内存

### 1.6.1 Direct Memory

- 常见于 NIO 操作，用于数据缓冲区
- 分配回收成本较高，但读写性能高
- 不受 JVM 内存回收管理

------

- 传统的阻塞式 IO 读取数据的方式：

CPU从Java的用户态转为系统的内核态，这样可以读取磁盘中的文件到系统的缓冲区，但是**系统的缓冲区 Java 无法获取，所以还需要将系统缓冲区中的数据传给Java缓冲区**

![image-20231222203852847](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312222038920.png)

- 使用直接内存进行文件读取：

会产生一块 direct memory的区域，操作系统和Java都可以读取

![image-20231222204121083](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312222041282.png)

**内存溢出问题**

当direct memory占用太大会抛出内存溢出异常

### 1.6.2 分配和回收原理

- 使用了 Unsafe 对象完成直接内存的分配回收，并且回收需要主动调用 freeMemory 方法

  ```java
  package com.rainsun.d1_Java_memory_structure;
  
  import sun.misc.Unsafe;
  
  import java.io.IOException;
  import java.lang.reflect.Field;
  
  public class d5_direct_memory {
      static int _1Gb = 1024*1024*1024;
      public static void main(String[] args) throws IOException {
          Unsafe unsafe = getUnsafe();
          // 分配内存
          long base = unsafe.allocateMemory(_1Gb);
          unsafe.setMemory(base, _1Gb, (byte) 0);
          System.in.read();
  
          // 释放内存
          unsafe.freeMemory(base);
          System.in.read();
      }
  
      public static Unsafe getUnsafe() {
          try {
              Field f = Unsafe.class.getDeclaredField("theUnsafe");
              f.setAccessible(true);
              Unsafe unsafe = (Unsafe) f.get(null);
              return unsafe;
          } catch (NoSuchFieldException | IllegalAccessException e) {
              throw new RuntimeException(e);
          }
      }
  }
  ```

  

- ByteBuffer 的实现类内部，使用了 Cleaner （虚引用）来监测 ByteBuffer 对象，一旦 ByteBuffer 对象被垃圾回收，那么就会由 ReferenceHandler 线程通过 Cleaner 的 clean 方法调用 freeMemory 来释放直接内存

  测试：

  ```java
  public class d6_direct_byteBuffer {
      static int _1Gb = 1024*1024*1024;
      public static void main(String[] args) throws IOException {
          ByteBuffer byteBuffer = ByteBuffer.allocateDirect(_1Gb);
          System.out.println("分配完毕");
          System.in.read();
          System.out.println("开始释放");
          byteBuffer = null;
          System.gc();
          System.in.read();
      }
  }
  ```

  原理：

  ```java
  // 分配内存后：
  public static ByteBuffer allocateDirect(int capacity) {
      return new DirectByteBuffer(capacity);
  }
  
  DirectByteBuffer(int cap) { 
      super(-1, 0, cap, cap, null);
      boolean pa = VM.isDirectMemoryPageAligned();
      int ps = Bits.pageSize();
      long size = Math.max(1L, (long)cap + (pa ? ps : 0));
      Bits.reserveMemory(size, cap);
  
      long base = 0;
      try {
          base = UNSAFE.allocateMemory(size);
      } catch (OutOfMemoryError x) {
          Bits.unreserveMemory(size, cap);
          throw x;
      }
      UNSAFE.setMemory(base, size, (byte) 0);
      if (pa && (base % ps != 0)) {
          // Round up to page boundary
          address = base + ps - (base & (ps - 1));
      } else {
          address = base;
      }
      try { // Cleaner检测this对象(ByteBuffer)是否被垃圾回收，如果被回收了就会调用clean方法调用 Deallocator 中的 run 方法 释放内存
          cleaner = Cleaner.create(this, new Deallocator(base, size, cap));
      } catch (Throwable t) {
          // Prevent leak if the Deallocator or Cleaner fail for any reason
          UNSAFE.freeMemory(base);
          Bits.unreserveMemory(size, cap);
          throw t;
      }
      att = null;
  }
  // 实现 Runnable 接口
  private static class Deallocator implements Runnable {
      private long address;
      private long size;
      private int capacity;
  
      private Deallocator(long address, long size, int capacity) {
          assert (address != 0);
          this.address = address;
          this.size = size;
          this.capacity = capacity;
      }
      // Deallocator 中的 run 释放了内存
      public void run() {
          if (address == 0) {
              // Paranoia
              return;
          }
          UNSAFE.freeMemory(address); // 释放内存
          address = 0;
          Bits.unreserveMemory(size, capacity);
      }
  
  }
  
  // clean 方法调用 run 方法：
  public void clean() {
      if (!remove(this))
          return;
      try {
          thunk.run(); // Deallocator 中的 run 被调用
      } catch (final Throwable x) {
          AccessController.doPrivileged(new PrivilegedAction<>() {
                  public Void run() {
                      if (System.err != null)
                          new Error("Cleaner terminated abnormally", x)
                              .printStackTrace();
                      System.exit(1);
                      return null;
                  }});
      }
  }
  ```

### 1.6.3 禁用显示垃圾回收

`-XX:+DisableExplicitGC` 禁用显式的垃圾回收

显示垃圾回收写法：

```java
System.gc();
```

这种垃圾回收都是 Full GC的，回收比较满，常用于性能调优

这可能会对Direct memory 的内存回收有影响，因为这里的Direct memory的内存回收是需要Java中的对象被回收时才会被触发的；

例如这里是ByteBuffer对象被回收，触发了分配的直接内存的回收。

所以还是用Unsafe对象手动的调用 freeMemory 进行回收比较好

# 2 垃圾回收

## 2.1 判断一个对象是否可回收

### 2.1.1 引用计数法

如果一个对象被另一个对象引用，那么它的引用计数加一，如果那个对象不再引用它了，那么引用计数减一。当引用计数为 0 时，该对象就应该被垃圾回收了。

但是下面这种互相引用的情况就无法回收了：

两个对象的计数都为1，导致两个对象都无法被释放	

![image-20231222213023143](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312222130211.png)

### 2.1.2 可达性分析算法

垃圾回收之前，扫描所有对象，判断每个对象是否被根对象引用，如果没有被根对象引用，那么在以后垃圾回收时就将那些没有与根相连的对象回收

- Java 虚拟机中的垃圾回收器采用可达性分析来探索所有存活的对象
- 扫描堆中的对象，看是否能够沿着 GC Root对象 为起点的引用链找到该对象，找不到，表示可以回收
- 哪些对象可以作为 GC Root ?

**查找可以作为GCRoot的对象：**

运行下面程序：

```java
public static void main(String[] args) throws IOException {

        ArrayList<Object> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add(1);
        System.out.println(1);
        System.in.read();

        list = null;
        System.out.println(2);
        System.in.read();
        System.out.println("end");
    }


```

使用 ` jmap -dump:format=b,live,file=1.bin 进程id` 将堆内存中的信息存储到文件1.bin中

使用Eclipse Memory Analyzer 打开1.bin文件，选择 GC Root 选项进行分析：

![image-20231222214435898](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312222144954.png)

可以看出 GC Root 分为四类：

- System Class ：系统类，启动类加载器加载的类，例如Object，HashMap等核心类
- Native Stack：Java 有时候需要调用系统中的一些方法
- Busy Monitor ：被加锁的对象也需要被保留
- Thread：活动线程的栈帧内中使用的对象

















