package com.rainsun.d1_Java_memory_structure;

public class d3_stringTable {
    // 常量池中的信息，都会被加载到运行时常量池中，这时 a , b 都是常量池中的符号，还没有变成 Java 字符串对象
    // ldc #2 会把 a 符号变为 "a" 字符串对象，并把 "a" 放入 StringTable 中，如果没有则加入新的，有则不加入新的
    public static void main(String[] args) {
        String s1 = "a";
        String s2 = "b";
        String s3 = "a" + "b"; //javac在编译期间的优化，变成了 "ab"，结果在编译期就已经确定了
        String s4 = s1 + s2;

        String s5 = "ab";
        String s6 = s4.intern();

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
