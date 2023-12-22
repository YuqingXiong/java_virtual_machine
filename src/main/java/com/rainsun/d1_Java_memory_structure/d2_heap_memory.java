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
