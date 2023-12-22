package com.rainsun.d1_Java_memory_structure;

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
