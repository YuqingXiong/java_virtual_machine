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
