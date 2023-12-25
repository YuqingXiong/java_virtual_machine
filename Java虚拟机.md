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
- 案例2 请求高峰期发生 Full GC，单次暂停时间特别长 （CMS）
- 案例3 老年代充裕情况下，发生 Full GC （CMS jdk1.7）
