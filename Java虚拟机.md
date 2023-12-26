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

### 2.1.3 五种引用

1. 强引用

   只有所有 GC Roots 对象都不通过【强引用】引用该对象，该对象才能被垃圾回收

例如：C对象和B对象强引用了 A1 对象，只有 C 对象和 B 对象都不强引用了 A1 对象，A1 对象才可以被垃圾回收

![image-20231223165023644](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312231650858.png)

2. 软引用（SoftReference）
   - **仅有软引用引用该对象时**，**在垃圾回收后，内存仍不足时会再次触发垃圾回收**，回收软引用对象
   - 可以配合引用队列来释放软引用自身
3. 弱引用（WeakReference）
   - 仅有弱引用引用该对象时，在垃圾回收时，**无论内存是否充足**，都会回收弱引用对象
   - 可以配合引用队列来释放弱引用自身

![image-20231223165530366](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312231655487.png)

4. 虚引用（PhantomReference）
  - 必须配合引用队列使用，主要配合 ByteBuffer 使用，被引用对象回收时，会将虚引用入队，由 Reference Handler 线程调用虚引用相关方法释放直接内存

Cleaner 对象虚引用 ByteBuffer 对象

![image-20231223165915900](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312231659027.png)

当ByteBuffer 对象被回收后，其分配的直接内存还没有被回收。

这时Cleaner对象会被放入引用队列：

![image-20231223170045348](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312231700488.png)

虚引用所在的队列会由一个ReferenceHandler的线程来定时的在队列中寻找是否有新入队的 Cleaner ，如果有就会调用 Cleaner 对象中的 clean 方法。而 clean 方法就会根据前面记录的直接内存的地址，用 Unsafe.freeMemory 方法释放直接内存：

![image-20231223170306689](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312231703042.png)

5. 终结器引用（FinalReference）

- 无需手动编码，但其内部配合引用队列使用，在垃圾回收时，终结器引用入队（被引用对象
  暂时没有被回收），再由 Finalizer 线程通过终结器引用找到被引用对象并调用它的 finalize方法，第二次 GC 时才能回收被引用对象

所有对象都继承自Object类，其中有一个 finalize 方法

如果 finalize 方法被重写了，也就是被对象实现了。jvm 就会将终结器引用对象放入引用队列。就会有一个FinalizeHandler的线程对队列进行检查。找到了终结器引用，就会根据它找到该对象，调用 finalize 方法。调用完后，第二次垃圾回收才可以把该对象回收掉

![image-20231223170935669](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312231709975.png)

### 2.1.4 软引用的示例

内存不足时，会回收软引用对象，可用于对象缓存

可以用 SoftReference 对象保证要软引用的对象：

首先限制内存大小的 vm option ：`-Xmx20m`

```java
public class d1_SoftReference {
    public static void main(String[] args) {
        // list -> SoftReference -> byte[]
        List<SoftReference<byte[]>> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            SoftReference<byte[]> reference = new SoftReference<>(new byte[1024 * 1024 * 4]);
            System.out.println(reference.get());
            list.add(reference);
            System.out.println(list.size());
        }
        System.out.println("循环结束" + list.size());
        for (SoftReference<byte[]> reference : list) {
            System.out.println(reference.get());
        }
    }
}
```

输出：可以看出由于内存空间不足，前三个ref都被回收掉了

```java
[B@10f87f48
1
[B@b4c966a
2
[B@2f4d3709
3
[B@4e50df2e
4
[B@1d81eb93
5
循环结束5
null
null
null
[B@4e50df2e
[B@1d81eb93
```

软引用，引用的对象为 null，那么软引用本身也就没有必要保留了。这里可以用软引用队列来对软引用对象本身进行回收

```java
public class d1_SoftReference {
    public static void main(String[] args) {
        List<SoftReference<byte[]>> list = new ArrayList<>();
        // 引用队列
        ReferenceQueue<byte[]> queue = new ReferenceQueue<>();

        for (int i = 0; i < 5; i++) {
            // 这里关联了软引用队列，当 byte[]被回收时，软引用本身会被加入到 queue中
            SoftReference<byte[]> reference = new SoftReference<>(new byte[1024 * 1024 * 4], queue);
            System.out.println(reference.get());
            list.add(reference);
            System.out.println(list.size());
        }
        // 清除掉没有内容的软引用本身：
        Reference<? extends byte[]> poll = queue.poll();
        while (poll != null){
            list.remove(poll);
            poll = queue.poll();
        }
        System.out.println("循环结束" + list.size());
        for (SoftReference<byte[]> reference : list) {
            System.out.println(reference.get());
        }
    }
}
```

输出：这里的list中只有2个byte数组了

```java
[B@10f87f48
1
[B@b4c966a
2
[B@2f4d3709
3
[B@4e50df2e
4
[B@1d81eb93
5
循环结束2
[B@4e50df2e
[B@1d81eb93
```

### 2.1.5  弱引用示例

不管内存是否充足，如果只有弱引用引用该对象，就会回收该对象

```java
// -Xmx20m
public class d2_WeakReference {
    public static void main(String[] args) {
        // list -> WeakReference -> byte[]
        List<WeakReference<byte[]>> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            WeakReference<byte[]> ref = new WeakReference<>(new byte[1024*1024*4]);
            list.add(ref);
            for (WeakReference<byte[]> weakReference : list) {
                System.out.println(weakReference.get() + " ");
            }
            System.out.println();
        }
        System.out.println("循环结束：" + list.size());
    }
}
```

输出：在垃圾回收时会将弱引用对象回收：

```java
[B@10f87f48 

[B@10f87f48 
[B@1d81eb93 

[B@10f87f48 
[B@1d81eb93 
[B@7291c18f 

null 
null 
null 
[B@34a245ab 

null 
null 
null 
null 
null 

循环结束：5
```

## 2.2 垃圾回收算法

### 2.2.1  标记清除

Mark Sweep

记录垃圾对象的地址

优点：速度快

缺点：造成内存碎片，本可以存下的新对象因为内存分布的太分散而无法存下

![image-20231223190346153](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312231903310.png)

### 2.2.2 标记整理

将所有存活的对象移动到一端，避免了内存碎片的产生，但是由于对象发生了移动，所以算法的速度慢

- 速度慢
- 没内存碎片

![image-20231223190839788](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312231908855.png)

### 2.2.3 复制

 把内存空间分为两部分。一部分内存空间用完了，就把存活的对象复制到另一部分的内存空间上面。然后把使用过的内存空间进行清理。

- 不会产生碎片
- 占用双倍的内存空间

![image-20231223191124239](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312231911352.png)

## 2.3 分代垃圾回收

![image-20231224155037939](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312241551068.png)

- 对象首先分配在伊甸园区
- 当新生代空间不足时，触发 minor gc，伊甸园和 from 区域存货的对象复制到 to 区域，存活的对象年龄加 1 ，并交换 from 和 to 两个区域
- minor gc 会引发 stop the world ，暂停其他用户线程，等待垃圾回收结束，用户线程才恢复运行
- 对象年龄的寿命超过阈值 15 时，会晋升到老年代
- 当老年代空间不足时，会首先触发 minor gc，如果之后空间仍然不足，就会触发 full gc，STW 的时间更长

----

对于大对象，新生代无法存入，就会直接存入老年代

如果大对象老年代都无法存下，就会抛出内存溢出的异常

多线程下运行，一个线程的内存溢出不会影响其他线程中断

### 2.3.1 相关 VM 参数

`-Xms` ：堆初始大小

`-Xmx` 或 ` -XX:MaxHeapSize=size` ：堆最大大小

`-Xmn` 或 `-XX:NewSize=size + -XX:MaxNewSize=size` 新生代大小

`-XX:InitialSurvivorRatio=ratio` 和 `-XX:+UseAdaptiveSizePolicy`：幸存区比例（动态）

`-XX:SurvivorRatio=ratio` 幸存区比例

`-XX:MaxTenuringThreshold=threshold` 晋升阈值

`-XX:+PrintTenuringDistribution` 晋升详情

`-XX:+PrintGCDetails -verbose:gc` GC详情

`-XX:+ScavengeBeforeFullGC` FullGC 前 MinorGC 

## 2.4 垃圾回收器

1. 串行

   - 单线程的垃圾回收期，回收时，其他线程暂停
   - 适合个人电脑，其堆内存较小，CPU个数少的
   - 吞吐量优先
2. 多线程运行
   - 堆内存较大，多核 CPU 支持（如果不是多核，就需要争抢时间片）
   - 让单位时间内，STW 的时间最短
     - 垃圾回收时间占比少：0.2 0.2 = 0.4
2. 响应时间优先
   - 多线程运行
   - 堆内存较大，多核 CPU 支持
   - 尽可能让单次的 STW 的时间最短
     - 每次0.1，回收多次：0.1 0.1 0.1 0.1 0.1  = 0.5

### 2.4.1 串行

VM 参数：`-XX:+UseSerialGC=Serial+SerialOld`

Serial : 工作在新生代，回收算法是复制算法

SerialOld：工作在老年代，回收算法是标记整理算法

![image-20231224162304198](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312241623337.png)

所有线程在安全点前阻塞，单线程的垃圾回收器运行；因为可能会更改对象地址，所以线程需要阻塞（STW）

### 2.4.2 吞吐量优先

`-XX:+UseParallelGC` ：新生代采用复制算法

`-XX:+UseParallelOldGC` ：老年代采用标记整理了算法

`Paralle` 表示回收器是并行的

![image-20231224163318445](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312241633512.png)

垃圾回收器的线程数默认是和CPU的核数相同的。

`-XX:+UseAdaptiveSizePolicy` ：自动调整垃圾回收的参数，例如eden，from，to，晋升阈值的大小

`-XX:GCTimeRation=ratio`：$1/(1+ratio)$ ，ratio 默认99，垃圾回收的时间占比为1%；但是难以达到，一般设置19；调整垃圾回收时间与总时间的占比，用于调整吞吐量

`-XX:MaxGCPauseMillis=ms` ：默认为 200 ms；用于调整每次垃圾回收的暂停时间

### 2.4.3 响应时间优先 CMS

`-XX:+UseConcMarkSweepGC` ：**并发**标记清除回收器，用于老年代；

`-XX:+UseParNewGC`：新生代的复制算法

如果并发出现问题，例如标记清除算法会产生很多内存碎片就会并发失败，老年代的垃圾回收器就会退化成 `SerialOld` 的复制串行回收器

![image-20231224164421464](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312241644587.png)

老年代空间不足，达到运行点，进行初始标记，对那些根对象进行遍历

随后进行并发标记遍历其他对象，与此同时其他线程继续运行

当进行重新标记时，需要STW， 随后进行重新标记

`ParallelGCThreads=n` ` -XX:ConcGCThreads=threads` ：可以设置并发标记线程数量；清理垃圾的同时，可能会产生新垃圾，得等到下次垃圾回收时才能释放，称为浮动垃圾。所以需要为这些浮动垃圾预留空间。

`-XX:CMSInitiatingOccupancyFraction=percent` ：执行垃圾回收时的内存占比

`-XX:+CMSScavengeBeforeRemark` ：重新标记时，可能新生代的对象引用老年代的对象，就需要扫描整个堆进行可达性分析，而这些新生代的对象将来也是要被回收掉的，就导致做了无用的查找工作。这个参数可以在重新标记前，对新生代进行一次垃圾回收，这样新生代的对象就少了，查找的工作就少了

### 2.4.4 G1

Garbage First 简称 G1 收集器。

2017 JDK 9默认

适用场景：

- 同时注重吞吐量（Throughput）和低延迟（Low latency），默认的暂停目标是 200 ms
- 超大堆内存，会将堆划分为多个大小相等的 Region
- 整体上是标记+整理算法，两个区域之间是复制算法

相关 JVM 参数：

- `-XX:+UseG1GC`
- `-XX:G1HeapRegionSize=size`
- `-XX:MaxGCPauseMillis=time`

#### 1）垃圾回收阶段

<img src="https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312241709231.png" alt="image-20231224170901118" style="zoom:50%;" />

#### 2）Young Collection

![image-20231224170943839](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312241709998.png)

新生代内存紧张后，就会将新生代的Eden区域的垃圾回收对象复制到幸存区 survivor

![image-20231224171205359](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312241712519.png)

当 survivor 区域的年龄超过晋升阈值就会复制到老年区 Old 

![image-20231224171215817](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312241712021.png)

#### 3) Young Collection +CM（concurrent Mark，并发标记）

- 在Young GC时会进行 GC Root的初始标记

- 老年代占用堆空间达到阈值时，进行并发标记（不会STW），由下面的 VM 参数决定

  `-XX:InitiatingHeapOccupancyPercent=percent `（默认45%）

![image-20231224171525523](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312241715611.png)

#### 4）混合收集

对 E，S，O 进行全面垃圾回收

- 最后标记 (Remark) 会 STW。因为是并发的，其他线程可能产生新的垃圾，所以需要进行最后标记
- 拷贝存活 (Evacuation) 会 STW

Eden 中的对象会复制到 Survivor ，Surivior 区域年龄达到晋升阈值会复制到 Old

Old 区域会根据设置的垃圾回收时间有选择的回收部分老年代的对象，挑选那些回收价值高的也就是**能释放内存更多的对象**进行回收。

垃圾回收暂停时间设置：`-XX:MaxGCPauseMillis=ms`

![image-20231224172217020](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312241722200.png)

#### 5）Full GC

- SerialGC
  新生代内存不足发生的垃圾收集 - minor gc
  老年代内存不足发生的垃圾收集 - full gc
- ParallelGC
  新生代内存不足发生的垃圾收集 - minor gc
  老年代内存不足发生的垃圾收集 - full gc
- CMS
  新生代内存不足发生的垃圾收集 - minor gc
  老年代内存不足
- G1
  新生代内存不足发生的垃圾收集 - minor gc
  老年代内存不足

#### 6) Young Collection 跨代引用
新生代回收的跨代引用（老年代引用新生代）问题

新生代回收时需要查找有哪些 GC Root，再进行可达性分析和回收

查找 GC Root 需要查找整个老年代花费时间多。

可以把老年代区域分割成多个 card，如果这个 card 引用了新生代，那么这个 card 就是脏 card。

![image-20231225151030711](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312251510557.png)

- 老年代里有卡表，新生代里有 Remembered Set 记录有那些脏 card 引用自己
  - 垃圾回收时就会通过 Remembered Set 找到脏card里的GC Root
- 在引用变更时通过 post-write barrier + dirty card queue
- concurrent refinement threads 更新 Remembered Set

![image-20231225151153020](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312251511115.png)

#### 7) Remark

pre-write barrier + satb_mark_queue

![image-20231225151551342](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312251515476.png)

C的引用发生改变则会添加写屏障，并放入队列，将来的Remark阶段就会通过队列对C对象进行处理决定是否垃圾回收

![image-20231225151849670](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312251518736.png)

#### 8）JDK 8u20 字符串去重

- 优点：节省大量内存

- 缺点：略微多占用了 cpu 时间，新生代回收时间略微增加

  开启这个开关（默认打开）：`-XX:+UseStringDeduplication`

```java
String s1 = new String("hello"); // char[]{'h','e','l','l','o'}
String s2 = new String("hello"); // char[]{'h','e','l','l','o'}
```

- 将所有新分配的字符串放入一个队列
- 当新生代回收时，G1并发检查是否有字符串重复
- 如果它们值一样，让它们引用同一个 char[]
- 注意，与 String.intern() 不一样
  - String.intern() 关注的是字符串对象
  - 而字符串去重关注的是 char[]
  - 在 JVM 内部，使用了不同的字符串表

#### 9) JDK 8u40 并发标记类卸载

所有对象都经过并发标记后，就能知道哪些类不再被使用，当一个类加载器的所有类都不再使用，则卸载它所加载的所有类
`-XX:+ClassUnloadingWithConcurrentMark` 默认启用

#### 10）JDK 8u60 回收巨型对象

- 一个对象大于 region 的一半时，称之为巨型对象
- **G1 不会对巨型对象进行拷贝**
- **回收时被优先考虑**
- G1 会跟**踪老年代所有 incoming 引用**，这样老年代 incoming 引用为0 的巨型对象就可以在新生代垃圾回收时处理掉

![image-20231225152524545](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312251525627.png)

![image-20231225152704858](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312251527046.png)

#### 11）JDK 9 并发标记起始时间的调整

- 并发标记必须在堆空间占满前完成，否则退化为 FullGC
- JDK 9 之前需要使用 `-XX:InitiatingHeapOccupancyPercent`
- JDK 9 可以动态调整
  - `-XX:InitiatingHeapOccupancyPercent` 用来设置初始值
  - 进行数据采样并动态调整
  - 总会添加一个安全的空档空间

## 2.5 垃圾回收调优

### 2.5.1 调优领域

- 内存
- 锁竞争
- CPU 占用
- IO 占用

### 2.5.2 确定目标

- 低延迟还是高吞吐量，选择合适的回收器

- 响应时间优先：CMS，G1，ZGC

- 吞吐量优先：ParallelGC

### 2.5.3 最快的 GC 是不 GC

- 查看 FullGC 前后的内存占用，考虑下面的问题：
  - 数据是不是太多？
    - resultSet = statement.executeQuery("select *from large_table") 查出的数据太多，可以加个 ` limit n` 限制一下查出数据的数量
  - 数据表示是否太臃肿？
    - 对象图，只查出只用的数据，例如查找年龄，不要也把姓名其他信息查出来
    - 对象大小设置，一个Object 最小占16字节，包装类型占24字节，基本类型int只占4字节
  - 是否存在内存泄漏？
    - 创建一个static Map map = xxx，长时间存货对象
    - 可以用软引用，弱引用，进行回收
    - 使用第三方缓存实现

### 2.5.4 新生代调优

新生代特点：

- 所有的 new 操作的内存分配非常廉价
  - 首先会分配在TLAB中（thread-local allocation buffer）
- 死亡对象的回收代价是零
- 大部分对象用过即死
- Minor GC 的时间远远低于 Full GC

新生代内存是否越大越好？

`-Xmn` ：设置新生代内存的vm命令
Sets the initial and maximum size (in bytes) of the heap for the young generation (nursery). GC is performed in this region more often than in other regions. If the size for the young generation is too small, then a lot of minor garbage collections are performed. If the size is too large, then only full garbage collections are performed, which can take a long time to complete. Oracle recommends that you keep the size for the young generation greater than 25% and less than 50% of the overall heap size.

- 新生代能容纳所有【并发量 * (请求-响应)】的数据
- 幸存区大到能保留【当前活跃对象+需要晋升对象】
- 晋升阈值配置得当，让长时间存活对象尽快晋升

	-XX:MaxTenuringThreshold=threshold
	-XX:+PrintTenuringDistribution

### 2.5.5 老年代调优

以 CMS 为例，具有浮动垃圾的问题，如果浮动垃圾存不下就会并发失败退化为Serial

- CMS 的老年代内存越大越好
- 先尝试不做调优，如果没有 Full GC 那么已经...，否则先尝试调优新生代
- 观察发生 Full GC 时老年代内存占用，将老年代内存预设调大 1/4 ~ 1/3
  - `-XX:CMSInitiatingOccupancyFraction=percent`

### 2.5.6 案例

- 案例1 Full GC 和 Minor GC频繁
  - 新生代内存太小，创建的对象生成周期短，但因新生代内存下而晋升到老年代
  - 可以调大新生代内存，同时调大晋升阈值
- 案例2 请求高峰期发生 Full GC，单次暂停时间特别长 （CMS）
  - 重新标记时会扫描所有对象，耗时太多
  - 设置`-XX:+CMSScavengeBeforeRemark` ：重新标记前回收一遍新生代的垃圾
- 案例3 老年代充裕情况下，发生 Full GC （CMS jdk1.7）
  - 1.7 永久代内存空间不足也会导致Full GC
  - 1.7 方法区存放在永久代，可以增大方法区的内存

# 3 类加载与字节码技术

## 3.1 类文件结构

1. 类文件结构
2. 字节码指令
3. 编译期处理
4. 类加载阶段
5. 类加载器
6. 运行期优化

![image-20231225163130177](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312251631270.png)

根据 JVM 规范，类文件结构如下

```java
ClassFile {
    u4 magic;
    u2 minor_version; // 小版本号
    u2 major_version; // 主版本号
    u2 constant_pool_count; // 常量池
    cp_info constant_pool[constant_pool_count-1];
    u2 access_flags; // 访问修饰 public project private 
    u2 this_class; // 包名 类名
    u2 super_class; // 父类信息
    u2 interfaces_count; // 接口信息
    u2 interfaces[interfaces_count];
    u2 fields_count; //类中的成员变量，静态变量
    field_info fields[fields_count];
    u2 methods_count; // 类中成员方法，静态方法
    method_info methods[methods_count];
    u2 attributes_count; // 附加的属性信息
    attribute_info attributes[attributes_count];
}
```

### 3.1.1 魔数

 0~3 字节，表示它是否是【class】类型的文件

0000000 **ca fe ba be** 00 00 00 34 00 23 0a 00 06 00 15 09

（咖啡宝贝？:）

### 3.1.2 版本

![image-20231225170801251](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312251708374.png)

4~7 字节，表示类的版本 00 34（52） 表示是 Java 8
0000000 ca fe ba be 00 00 00 34 00 23 0a 00 06 00 15 09

### 3.1.3 常量池

8-9字节表示常量池的长度（constant_pool_count）

00 23 (35）表示常量池有 #1~#34项，#0项不计入，也没有值；

第一个字节表示常量类型，一共有 17 种类型：每种类型有着完全独立的数据结构

![image-20231226100637687](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261006940.png)



第#1项 0a 表示一个 Method 信息，00 06 和 00 15（21） 表示它引用了常量池中 #6 和 #21 项来获得这个方法的【所属类】和【方法名】

0000000 ca fe ba be 00 00 00 34 00 23 **0a 00 06 00 15** 09



第#2项 09 表示一个 Field 信息，00 16（22）和 00 17（23） 表示它引用了常量池中 #22 和 # 23 项来获得这个成员变量的【所属类】和【成员变量名】

0000000 ca fe ba be 00 00 00 34 00 23 0a 00 06 00 15 09

0000020 **00 16 00 17** 08 00 18 0a 00 19 00 1a 07 00 1b 07



第#6项 07 表示一个 Class 信息，00 1c（28） 表示它引用了常量池中 #28 项

0000020 00 16 00 17 08 00 18 0a 00 19 00 1a 07 00 1b **07**

0000040 **00 1c** 01 00 06 3c 69 6e 69 74 3e 01 00 03 28 29



第#7项 01 表示一个 utf8 串，00 06 表示长度，3c 69 6e 69 74 3e 是【`<init> `】表示构造方法

0000040 00 1c **01 00 06 3c 69 6e 69 74 3e** 01 00 03 28 29



第#8项 01 表示一个 utf8 串，00 03 表示长度，28 29 56 是【()V】其实就是表示无参、无返回值

0000040 00 1c 01 00 06 3c 69 6e 69 74 3e **01 00 03 28 29**

0000060 **56** 01 00 04 43 6f 64 65 01 00 0f 4c 69 6e 65 4e



第#21项 0c 表示一个 【名+类型】，00 07 00 08 引用了常量池中 #7 #8 两项

0000360 2e 6a 61 76 61 **0c 00 07 00 08** 07 00 1d 0c 00 1e



第#22项 07 表示一个 Class 信息，00 1d（29） 引用了常量池中 #29 项

0000360 2e 6a 61 76 61 0c 00 07 00 08 **07 00 1d** 0c 00 1e



第#23项 0c 表示一个 【名+类型】，00 1e（30） 00 1f （31）引用了常量池中 #30 #31 两项
0000360 2e 6a 61 76 61 0c 00 07 00 08 07 00 1d **0c 00 1e**
0000400 **00 1f** 01 00 0b 68 65 6c 6c 6f 20 77 6f 72 6c 64



第#28项 01 表示一个 utf8 串，00 10（16） 表示长度，是【java/lang/Object】

0000460 6f 57 6f 72 6c 64 **01** **00 10 6a 61 76 61 2f 6c 61**

0000500 **6e 67 2f 4f 62 6a 65 63 74** 01 00 10 6a 61 76 61



第#29项 01 表示一个 utf8 串，00 10（16） 表示长度，是【java/lang/System】

0000500 6e 67 2f 4f 62 6a 65 63 74 **01 00 10 6a 61 76 61**

0000520 **2f 6c 61 6e 67 2f 53 79 73 74 65 6d** 01 00 03 6f



第#30项 01 表示一个 utf8 串，00 03 表示长度，是【out】
0000520 2f 6c 61 6e 67 2f 53 79 73 74 65 6d **01 00 03 6f**
0000540 75 74 01 00 15 4c 6a 61 76 61 2f 69 6f 2f 50 72



第#31项 01 表示一个 utf8 串，00 15（21） 表示长度，是【Ljava/io/PrintStream;】
0000540 75 74 **01 00 15 4c 6a 61 76 61 2f 69 6f 2f 50 72**
0000560 **69 6e 74 53 74 72 65 61 6d 3b** 01 00 13 6a 61 76

### 3.1.4 访问标识与继承信息

![image-20231226101652491](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261016557.png)

21 表示该 class 是一个类，公共的
0000660 29 56 **00 21** 00 05 00 06 00 00 00 00 00 02 00 01
05 表示根据常量池中 #5 找到本类全限定名
0000660 29 56 00 21 **00 05** 00 06 00 00 00 00 00 02 00 01
06 表示根据常量池中 #6 找到父类全限定名
0000660 29 56 00 21 00 05 **00 06** 00 00 00 00 00 02 00 01
表示接口的数量，本类为 0
0000660 29 56 00 21 00 05 00 06 **00 00** 00 00 00 02 00 01

### 3.1.5 Field 信息用于表示成员变量

表示成员变量数量，本类为 0
0000660 29 56 00 21 00 05 00 06 00 00 **00 00** 00 02 00 01

字节码中表示类型信息的方法

![image-20231226101827740](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261018948.png)

### 3.1.6 Method 信息

表示方法数量，本类为 2
0000660 29 56 00 21 00 05 00 06 00 00 00 00 **00 02** 00 01

一个方法由 访问修饰符，名称，参数描述，方法属性数量，方法属性组成

- 红色代表访问修饰符（本类中是 public）
- 蓝色代表引用了常量池 #07 项作为方法名称
- 绿色代表引用了常量池 #08 项作为方法参数描述
- 黄色代表方法属性数量，本方法是 1
- 红色代表方法属性
  - 00 09 表示引用了常量池 #09 项，发现是【Code】属性
  - 00 00 00 2f 表示此属性的长度是 47
  - 00 01 表示【操作数栈】最大深度
  - 00 01 表示【局部变量表】最大槽（slot）数
  - 00 00 00 05 表示字节码长度，本例是 5
  - 2a b7 00 01 b1 是字节码指令
  - 00 00 00 02 表示方法细节属性数量，本例是 2
  - 00 0a 表示引用了常量池 #10 项，发现是【LineNumberTable】属性
    - 00 00 00 06 表示此属性的总长度，本例是 6
    - 00 01 表示【LineNumberTable】长度
    - 00 00 表示【字节码】行号 00 04 表示【java 源码】行号
  - 00 0b 表示引用了常量池 #11 项，发现是【LocalVariableTable】属性
    - 00 00 00 0c 表示此属性的总长度，本例是 12
    - 00 01 表示【LocalVariableTable】长度
    - 00 00 表示局部变量生命周期开始，相对于字节码的偏移量
    - 00 05 表示局部变量覆盖的范围长度
    - 00 0c 表示局部变量名称，本例引用了常量池 #12 项，是【this】
    - 00 0d 表示局部变量的类型，本例引用了常量池 #13 项，是【Lcn/itcast/jvm/t5/HelloWorld;】
    - 00 00 表示局部变量占有的槽位（slot）编号，本例是 0

![image-20231226102243959](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261022103.png)

### 3.1.7 附加属性

00 01 表示附加属性数量
00 13 表示引用了常量池 #19 项，即【SourceFile】
00 00 00 02 表示此属性的长度
00 14 表示引用了常量池 #20 项，即【HelloWorld.java】

0001100 00 12 00 00 00 05 01 00 10 00 00 **00 01 00 13 00**
0001120 **00 00 02 00 14**

### 参考文献
https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html

## 3.2 字节码指令

### 3.2.1 入门

public cn.itcast.jvm.t5.HelloWorld(); 构造方法的字节码指令

```java
2a b7 00 01 b1
```

1. 2a => aload_0 加载 slot 0 的局部变量，即 this，做为下面的 invokespecial 构造方法调用的参数
2. b7 => invokespecial 预备调用构造方法，哪个方法呢？
3. 00 01 引用常量池中 #1 项，即【Method java/lang/Object."<init>":()V 】
4. b1 表示返回



另一个是 public static void main(java.lang.String[]); 主方法的字节码指令

```java
b2 00 02 12 03 b6 00 04 b1
```

1. b2 => getstatic 用来加载静态变量，哪个静态变量呢？
2. 00 02 引用常量池中 #2 项，即【Field java/lang/System.out:Ljava/io/PrintStream;】
3. 12 => ldc 加载参数，哪个参数呢？
4. 03 引用常量池中 #3 项，即 【String hello world】
5. b6 => invokevirtual 预备调用成员方法，哪个方法呢？
6. 00 04 引用常量池中 #4 项，即【Method java/io/PrintStream.println:(Ljava/lang/String;)V】
7. b1 表示返回

### 3.2.2 javap 工具反编译字节码文件

自己分析类文件结构太麻烦了，Oracle 提供了 javap 工具来反编译 class 文件：

`javap -v class文件路径`

```java
D:\CodeProject\Java\java_virtual_machine\target\classes\com\rainsun\d3_class_structure> javap -v .\d1_HelloWorld.class
Classfile /D:/CodeProject/Java/java_virtual_machine/target/classes/com/rainsun/d3_class_structure/d1_HelloWorld.class
  Last modified 2023年12月25日; size 604 bytes
  SHA-256 checksum f4c26de1e0291f2f0984d894624592a6287a89c87805cc57f0b2e658b5a796c7
  Compiled from "d1_HelloWorld.java"
public class com.rainsun.d3_class_structure.d1_HelloWorld
  minor version: 0
  major version: 65
  flags: (0x0021) ACC_PUBLIC, ACC_SUPER
  this_class: #21                         // com/rainsun/d3_class_structure/d1_HelloWorld
  super_class: #2                         // java/lang/Object
  interfaces: 0, fields: 0, methods: 2, attributes: 1
Constant pool:
   #1 = Methodref          #2.#3          // java/lang/Object."<init>":()V
   #2 = Class              #4             // java/lang/Object
   #3 = NameAndType        #5:#6          // "<init>":()V
   #4 = Utf8               java/lang/Object
   #5 = Utf8               <init>
   #6 = Utf8               ()V
   #7 = Fieldref           #8.#9          // java/lang/System.out:Ljava/io/PrintStream;
   #8 = Class              #10            // java/lang/System
   #9 = NameAndType        #11:#12        // out:Ljava/io/PrintStream;
  #10 = Utf8               java/lang/System
  #11 = Utf8               out
  #12 = Utf8               Ljava/io/PrintStream;
  #13 = String             #14            // hello world
  #14 = Utf8               hello world
  #15 = Methodref          #16.#17        // java/io/PrintStream.println:(Ljava/lang/String;)V
  #16 = Class              #18            // java/io/PrintStream
  #17 = NameAndType        #19:#20        // println:(Ljava/lang/String;)V
  #18 = Utf8               java/io/PrintStream
  #19 = Utf8               println
  #20 = Utf8               (Ljava/lang/String;)V
  #21 = Class              #22            // com/rainsun/d3_class_structure/d1_HelloWorld
  #22 = Utf8               com/rainsun/d3_class_structure/d1_HelloWorld
  #23 = Utf8               Code
  #24 = Utf8               LineNumberTable
  #25 = Utf8               LocalVariableTable
  #26 = Utf8               this
  #27 = Utf8               Lcom/rainsun/d3_class_structure/d1_HelloWorld;
  #28 = Utf8               main
  #29 = Utf8               ([Ljava/lang/String;)V
  #30 = Utf8               args
  #31 = Utf8               [Ljava/lang/String;
  #32 = Utf8               SourceFile
  #33 = Utf8               d1_HelloWorld.java
{
  public com.rainsun.d3_class_structure.d1_HelloWorld();
    descriptor: ()V
      LineNumberTable:
        line 5: 0
        line 6: 8
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       9     0  args   [Ljava/lang/String;
}
SourceFile: "d1_HelloWorld.java"
```

### 3.2.3 图解方法执行流程

#### 1）原始的 Java 代码

```java
package com.rainsun.d3_class_structure;
/**
 * 演示 字节码指令 和 操作数栈、常量池的关系
 */
public class d2_method_runflow {
    public static void main(String[] args) {
        int a = 10;
        int b = Short.MAX_VALUE + 1;
        int c = a + b;
        System.out.println(c);
    }
}
```

#### 2）编译后的字节码文件

```java
 D:\CodeProject\Java\java_virtual_machine\target\classes\com\rainsun\d3_class_structure> javap -v .\d2_method_runflow.class
Classfile /D:/CodeProject/Java/java_virtual_machine/target/classes/com/rainsun/d3_class_structure/d2_method_runflow.class
  Last modified 2023年12月26日; size 675 bytes
  SHA-256 checksum 090cee1c8efae1a0d3b54af310b799f33efc90463f218ea25bdb4cea5aef56ef
  Compiled from "d2_method_runflow.java"
public class com.rainsun.d3_class_structure.d2_method_runflow
  minor version: 0
  major version: 65
  flags: (0x0021) ACC_PUBLIC, ACC_SUPER
  this_class: #22                         // com/rainsun/d3_class_structure/d2_method_runflow
  super_class: #2                         // java/lang/Object
  interfaces: 0, fields: 0, methods: 2, attributes: 1
Constant pool:
   #1 = Methodref          #2.#3          // java/lang/Object."<init>":()V
   #2 = Class              #4             // java/lang/Object
   #3 = NameAndType        #5:#6          // "<init>":()V
   #4 = Utf8               java/lang/Object
   #5 = Utf8               <init>
   #6 = Utf8               ()V
   #7 = Class              #8             // java/lang/Short
   #8 = Utf8               java/lang/Short
   #9 = Integer            32768
  #10 = Fieldref           #11.#12        // java/lang/System.out:Ljava/io/PrintStream;       
  #11 = Class              #13            // java/lang/System
  #12 = NameAndType        #14:#15        // out:Ljava/io/PrintStream;
  #13 = Utf8               java/lang/System
  #14 = Utf8               out
  #15 = Utf8               Ljava/io/PrintStream;
  #16 = Methodref          #17.#18        // java/io/PrintStream.println:(I)V
  #17 = Class              #19            // java/io/PrintStream
  #18 = NameAndType        #20:#21        // println:(I)V
  #19 = Utf8               java/io/PrintStream
  #20 = Utf8               println
  #21 = Utf8               (I)V
  #22 = Class              #23            // com/rainsun/d3_class_structure/d2_method_runflow 
  #23 = Utf8               com/rainsun/d3_class_structure/d2_method_runflow
  #24 = Utf8               Code
  #25 = Utf8               LineNumberTable
  #26 = Utf8               LocalVariableTable
  #27 = Utf8               this
  #28 = Utf8               Lcom/rainsun/d3_class_structure/d2_method_runflow;
  #29 = Utf8               main
  #30 = Utf8               ([Ljava/lang/String;)V
  #31 = Utf8               args
  #32 = Utf8               [Ljava/lang/String;
  #33 = Utf8               a
  #34 = Utf8               I
  #35 = Utf8               b
  #36 = Utf8               c
  #37 = Utf8               SourceFile
  #38 = Utf8               d2_method_runflow.java
{
  public com.rainsun.d3_class_structure.d2_method_runflow();
    descriptor: ()V
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 6: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   Lcom/rainsun/d3_class_structure/d2_method_runflow;        

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=4, args_size=1
         0: bipush        10
         2: istore_1
         3: ldc           #9                  // int 32768
         5: istore_2
         6: iload_1
         7: iload_2
         8: iadd
         9: istore_3
        10: getstatic     #10                 // Field java/lang/System.out:Ljava/io/PrintStream;
        13: iload_3
        14: invokevirtual #16                 // Method java/io/PrintStream.println:(I)V      
        17: return
      LineNumberTable:
        line 8: 0
        line 9: 3
        line 10: 6
        line 11: 10
        line 12: 17
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      18     0  args   [Ljava/lang/String;
            3      15     1     a   I
            6      12     2     b   I
           10       8     3     c   I
}
SourceFile: "d2_method_runflow.java"
```

#### 3）常量池放入运行时常量池

首先加载main方法所在的类，加载类需要将类中的常量池放入加载到运行时常量池

![image-20231226104547181](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261045258.png)

#### 4）方法字节码载入方法区

![image-20231226105131299](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261051375.png)

#### 5）main 线程开始运行，分配栈帧内存

（stack=2，locals=4）

绿色：局部变量表，有 4 个槽

蓝绿色：操作数栈，深度为 2 

![image-20231226105154146](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261051211.png)

#### 6）执行引擎开始执行字节码

##### bipush 10

- 将一个 byte 压入操作数栈（其长度会补齐 4 个字节），类似的指令还有
- sipush 将一个 short 压入操作数栈（其长度会补齐 4 个字节）
- ldc 将一个 int 压入操作数栈
- ldc2_w 将一个 long 压入操作数栈（分两次压入，因为 long 是 8 个字节）
- 这里小的数字都是和字节码指令存在一起，超过 short 范围的数字存入了常量池

![image-20231226105240461](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261052587.png)

##### istore_1

将操作数栈顶数据弹出，存入局部变量表的 slot 1

![image-20231226105711881](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261057021.png)

![image-20231226105725011](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261057149.png)

##### ldc #3

- 从常量池加载 #3 数据到操作数栈
- 注意 Short.MAX_VALUE 是 32767，所以 32768 = Short.MAX_VALUE + 1 实际是在编译期间计算好的

![image-20231226105800441](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261058587.png)

##### istore_2

将操作数栈顶数据弹出，存入局部变量表的 slot 2

![image-20231226105832397](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261058472.png)

##### iload_1

加载 slot 1 中的数据到操作数栈

![image-20231226105921951](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261059114.png)

##### iload_2

![image-20231226105957795](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261059937.png)

##### iadd

执行加法

![image-20231226110031605](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261100707.png)

##### istore_3

将栈顶的执行结果存入局部变量表的 slot 3

![image-20231226110050072](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261100276.png)

##### getstatic #10

getstatic获取一个成员变量的引用，将该对象加载到堆中，并将堆中的引用放入操作数栈

![image-20231226110141704](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261101844.png)

![image-20231226110415448](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261104579.png)

##### iload_3

将操作数变量表中的 slot 3 位置的变量放入操作数栈，传递给out对象

![image-20231226110434514](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261104965.png)

##### invokevirtual #16

调用println函数

- 找到常量池 #5 项
- 定位到方法区 java/io/PrintStream.println:(I)V 方法
- 生成新的栈帧（分配 locals、stack等）
- 传递参数，执行新栈帧中的字节码

![image-20231226110707787](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261107935.png)

- 执行完毕，弹出栈帧
- 清除 main 操作数栈内容

![image-20231226111530302](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261115367.png)

##### return

- 完成 main 方法调用，弹出 main 栈帧
- 程序结束

### 3.2.9 方法调用

```java
public class Demo3_9 {
    public Demo3_9() { } 			// 构造方法
    private void test1() { }		// 私有方法
    private final void test2() { }	// final 方法
    public void test3() { }			// 普通 public 成员方法
    public static void test4() { }	// 静态方法
    public static void main(String[] args) {
    	Demo3_9 d = new Demo3_9();
        d.test1();
        d.test2();
        d.test3();
        d.test4();
        Demo3_9.test4();
    }
}
```

字节码：

```java
0: new #2 // class cn/itcast/jvm/t3/bytecode/Demo3_9
3: dup
4: invokespecial #3 // Method "<init>":()V
7: astore_1
8: aload_1
9: invokespecial #4 // Method test1:()V
12: aload_1
13: invokespecial #5 // Method test2:()V
16: aload_1
17: invokevirtual #6 // Method test3:()V
20: aload_1
21: pop
22: invokestatic #7 // Method test4:()V
25: invokestatic #7 // Method test4:()V
28: return
```

`Demo3_9 d = new Demo3_9();`

- new 是创建【对象】，给对象分配堆内存，执行成功会将【对象引用】压入操作数栈
- dup 是赋值操作数栈栈顶的内容，本例即为【对象引用】，为什么需要两份引用呢，一个是要配合 invokespecial 调用该对象的构造方法 "`<init>`":()V （会消耗掉栈顶一个引用），另一个要配合 astore_1 赋值给局部变量

`test1` `test2`

最终方法（final），私有方法（private），构造方法都是由 invokespecial 指令来调用，属于静态绑定

`test3`:

普通成员方法是由 invokevirtual 调用，**属于动态绑定，即支持多态**

成员方法与静态方法调用的另一个区别是，执行方法前是否需要【对象引用】

比较有意思的是 d.test4(); 是通过【对象引用】调用一个静态方法，可以看到在调用 invokestatic 之前执行了 pop 指令，把【对象引用】从操作数栈弹掉了

还有一个执行 invokespecial 的情况是通过 super 调用父类方法

### 3.2.10 多态的实现原理

（HSDB工具的使用）

对象的内存结构是由基础的16字节加上存储属性所花费的字节组成的

16字节中，前8字节为 MarkWord 用于计算类的 hashcode，后 8 字节为对象的Class指针

查看该对象的 class 指针指向的内存地址，可以找到其中关联一个 vtable（虚函数表），里面存储着虚方法。从 Class 的起始地址开始算，偏移 0x1b8 就是 vtable 的起始地址

通过 Tools -> Class Browser 查看每个类的方法定义，比较可知

```java
Dog - public void eat() @0x000000001b7d3fa8
Animal - public java.lang.String toString() @0x000000001b7d35e8;
Object - protected void finalize() @0x000000001b3d1b10;
Object - public boolean equals(java.lang.Object) @0x000000001b3d15e8;
Object - public native int hashCode() @0x000000001b3d1540;
Object - protected native java.lang.Object clone() @0x000000001b3d1678;
```

- eat() 方法是 Dog 类自己的
- toString() 方法是继承 String 类的
- finalize() ，equals()，hashCode()，clone() 都是继承 Object 类的

当执行 invokevirtual 指令时，
1. 先通过栈帧中的对象引用找到对象
2. 分析对象头，找到对象的实际 Class
3. Class 结构中有 vtable，**它在类加载的链接阶段就已经根据方法的重写规则生成好了**
4. 查表得到方法的具体地址
5. 执行方法的字节码

### 3.2.11 异常处理

**try catch 原理：**

![image-20231226151814103](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261518254.png)

- 可以看到多出来一个 Exception table 的结构，[from, to) 是前闭后开的检测范围，一旦这个范围内的字节码执行出现异常，则通过 type 匹配异常类型，如果一致，进入 target 所指示行号
- 8 行的字节码指令 astore_2 是将异常对象引用存入局部变量表的 slot 2 位置

**多个 single-catch 块的情况：**

因为异常出现时，只能进入 Exception table 中一个分支，所以局部变量表 slot 2 位置被共用

监听[2, 5)之间的字节码，如果出现了异常就转到对应的 target 行进行处理

![image-20231226151915151](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261519319.png)

**multi-catch 的情况**

![image-20231226152116152](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261521199.png)

catch 多个不同类型的异常，那么 target 跳转的行就会相同

![image-20231226152100958](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261521175.png)

**finally原理：**

finally 中的代码被复制了 3 份，分别放入 try 流程，catch 流程以及 catch 剩余的异常类型流程

- 在没有异常会在try后执行finally中的代码
- 捕获了异常的时候就catch执行后执行finally中的代码
- 在出现了异常但是和异常类型不匹配时，也就是没有捕获成功时
  - 由于JVM增加了一个异常检测，还会检测 catch 是否出现了异常
  - 这时如果 catch中没有捕获异常或者出现了新的异常就会跳转到 finally字节码的地方执行。

![image-20231226151640515](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261516605.png)

#### finally 的面试题

finally 出现了 return

```java
public class Demo3_12_2 {
    public static void main(String[] args) {
        int result = test();
        System.out.println(result);
    }
    public static int test() {
        try {
            return 10;
        } finally {
            return 20;
        }
    }
}
```

字节码的角度分析：

```java
public static int test();
    descriptor: ()I
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
        stack=1, locals=2, args_size=0
        0: bipush 10 // <- 10 放入栈顶
        2: istore_0 // 10 -> slot 0 (从栈顶移除了)
        3: bipush 20 // <- 20 放入栈顶
        5: ireturn // 返回栈顶 int(20)
        6: astore_1 // catch any -> slot 1
        7: bipush 20 // <- 20 放入栈顶
        9: ireturn // 返回栈顶 int(20)
    Exception table:
        from to target type
           0  3     6   any
    LineNumberTable: ...
    StackMapTable: ...
```

因为 finally 块中的代码被插入了所有可能的流程（当然包括 try 流程）。所以在try 中代码执行后，执行了 finally 中的代码，由于最后执行的 finally 代码，finally 中的 20 被放入栈顶了，最后 return 就是栈顶的 20。

这也说明了 finally 的代码是在 return 前插入的

- 由于 finally 中的 ireturn 被插入了所有可能的流程，因此返回结果肯定以 finally 的为准
- 至于字节码中第 2 行，似乎没啥用，且留个伏笔，看下个例子
- 跟上例中的 finally 相比，发现没有 athrow 了，这告诉我们：
  - 如果在 finally 中出现了 return，会吞掉异常😱😱😱，可以试一下下面的代码。1/0不会抛出异常，只返回了一个 20

```java
public class Demo3_12_1 {
    public static void main(String[] args) {
        int result = test();
        System.out.println(result);//20
    }
    public static int test() {
        try {
            int i = 1/0; 
        	return 10;
        } finally {
        	return 20;
        }
    }
}
```

**finally 对返回值影响**

```java
public class Demo3_12_2 {
    public static void main(String[] args) {
        int result = test();
        System.out.println(result); // 10
    }
    public static int test() {
        int i = 10;
        try {
            return i;
        } finally {
            i = 20;
        }
    }
}
```

字节码：

```java
 public static int test();
    descriptor: ()I
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=1, locals=3, args_size=0
         0: bipush        10 // <-10放入栈顶
         2: istore_0		// 10-> slot 0
         3: iload_0			// <- slot 0 的10加载到栈顶
         4: istore_1		// 栈顶10暂存到 solt 1，目的是为了固定返回值
         5: bipush        20  // 20 放入栈顶
         7: istore_0		// 20 放入solt 0
         8: iload_1			// solt 1 里的 10	加载到栈顶
         9: ireturn			// 返回栈顶的 10
        10: astore_2
        11: bipush        20
        13: istore_0
        14: aload_2
        15: athrow
      Exception table:
         from    to  target type
             3     5    10   any
      LineNumberTable:
        line 9: 0
        line 11: 3
        line 13: 5
        line 11: 8
        line 13: 10
        line 14: 14
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            3      13     0     i   I
      StackMapTable: number_of_entries = 1
        frame_type = 255 /* full_frame */
          offset_delta = 10
          locals = [ int ]
          stack = [ class java/lang/Throwable ]
}
```

如果 finally 中没有 return，对返回值的修改是没有影响的，因为在return前会对返回值进行暂存

同时由于 finally 中没有 return 抛出异常的 athrow 也不会被吞掉

#### synchronized

```java
public class Demo3_13 {
    public static void main(String[] args) {
        Object lock = new Object();
        synchronized (lock) {
        	System.out.println("ok");
        }
    }
}
```

monitorenter 指令对对象进行加锁

monitorexit 指令对对象进行解锁，在正常执行和异常处理的部分都会放置一份，确保即使出现异常也可以解锁成功

![image-20231226155242490](https://xiongyuqing-img.oss-cn-qingdao.aliyuncs.com/img/202312261552696.png)

## 3.3 编译期处理

所谓的 语法糖，其实就是指 java 编译器把 *.java 源码编译为 *.class 字节码的过程中，自动生成和转换的一些代码

编译器转换的结果直接就是 class 字节码，只是为了便于阅读，这里给出了 几乎等价 的 java 源码方式，

原本的源码->优化->源码优化后生成的字节码->反编译生成优化后的源码

### 3.3.1 默认构造器

```java
public class Candy1 {
}

//编译成class后的代码：
public class Candy1 {
    // 这个无参构造是编译器帮助我们加上的
    public Candy1() {
    	super(); // 即调用父类 Object 的无参构造方法，即调用 java/lang/Object." <init>":()V
    }
}
```

### 3.3.2 自动拆装箱

从JDK 5开始：

之前版本的代码太麻烦了，需要在基本类型和包装类型之间来回转换（尤其是集合类中操作的都是包装类型），因此这些转换的事情在 JDK 5 以后都由编译器在编译阶段完成。即 代码片段1 都会在编译阶段被转换为 代码片段2

```java
//1:
public class Candy2 {
    public static void main(String[] args) {
        Integer x = 1;
        int y = x;
    }
}

// 2:
public class Candy2 {
    public static void main(String[] args) {
        Integer x = Integer.valueOf(1);
        int y = x.intValue;
    }
}
```

### 3.3.3 泛型集合取值

泛型也是在 JDK 5 开始加入的特性，但 java 在编译泛型代码后会执行 泛型擦除 的动作，即泛型信息在编译为字节码之后就丢失了，实际的类型都当做了 Object 类型来处理：

```java
public class Candy3 {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(10); // 实际调用的是 List.add(Object e)
        Integer x = list.get(0); // 实际调用的是 Object obj = List.get(int index);
    }
}
```

擦除的是字节码上的泛型信息，可以看到 LocalVariableTypeTable 仍然保留了方法参数泛型的信息

使用反射，仍然能够获得这些信息：

```java
public Set<Integer> test(List<String> list, Map<Integer, Object> map) {
}
```

```java
Method test = Candy3.class.getMethod("test", List.class, Map.class);
Type[] types = test.getGenericParameterTypes();
for (Type type : types) {
    if (type instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        System.out.println("原始类型 - " + parameterizedType.getRawType());
        Type[] arguments = parameterizedType.getActualTypeArguments();
        for (int i = 0; i < arguments.length; i++) {
        	System.out.printf("泛型参数[%d] - %s\n", i, arguments[i]);
        }
    }
}
```

输出：

```java
原始类型 - interface java.util.List
泛型参数[0] - class java.lang.String
原始类型 - interface java.util.Map
泛型参数[0] - class java.lang.Integer
泛型参数[1] - class java.lang.Object
```

#### 可变参数，foreach,switch,枚举...

#### 方法重写时的桥接方法

方法重写时对返回值分两种情况：

- 父子类的返回值完全一致
- 子类返回值可以是父类返回值的子类（比较绕口，见下面的例子）

```java
class A {
    public Number m() {
    	return 1;
    }
}
class B extends A {
    @Override
    // 子类 m 方法的返回值是 Integer 是父类 m 方法返回值 Number 的子类
    public Integer m() {
        return 2;
    }
}
```

对于子类，java 编译器会做如下处理：

```java
class B extends A {
    public Integer m() {
        return 2;
    }
    // 此方法才是真正重写了父类 public Number m() 方法
    public synthetic bridge Number m() {
        // 调用 public Integer m()
        return m();
    }
}
```

其中桥接方法比较特殊，仅对 java 虚拟机可见，并且与原来的 public Integer m() 没有命名冲突，

#### 匿名内部类

```java
// 源代码：
public class Candy11 {
    public static void main(String[] args) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
            	System.out.println("ok");
        	}
        };
    }
}

// 转换后：额外生成的类
final class Candy11$1 implements Runnable {
    Candy11$1() {
    }
    public void run() {
        System.out.println("ok");
    }
}
public class Candy11 {
    public static void main(String[] args) {
        Runnable runnable = new Candy11$1();
    }
}
```

引用局部变量的匿名内部类，源代码：

```java
public class Candy11 {
    public static void test(final int x) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("ok:" + x);
            }
        };
    }
}
```

转换后代码：

```java
// 额外生成的类
final class Candy11$1 implements Runnable {
    int val$x; // 外部的局部变量变成了类的成员变量，无法感知外部的变化，所以引用的外部变量必须是 final 的
    Candy11$1(int x) {
        this.val$x = x;
    }
    public void run() {
        System.out.println("ok:" + this.val$x);
    }
}

public class Candy11 {
    public static void test(final int x) {
        Runnable runnable = new Candy11$1(x);
    }
}
```

这同时解释了为什么匿名内部类引用局部变量时，局部变量必须是 final 的：因为在创建
`Candy11$1` 对象时，将 x 的值赋值给了 `Candy11$1` 对象的 `val$x` 属性，所以 x 不应该再发生变化了，如果变化，那么 `val$x` 属性没有机会再跟着一起变化







