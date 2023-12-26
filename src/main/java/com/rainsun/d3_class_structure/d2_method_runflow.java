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
