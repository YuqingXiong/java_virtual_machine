package com.rainsun.d3_class_structure;

public class d5_classLoad {
    public static void main(String[] args) throws ClassNotFoundException {
        System.out.println(d5_classLoad.class.getClassLoader());
        Class<?> aClass = d5_classLoad.class.getClassLoader().loadClass("com.rainsun.d3_class_structure.H");
        System.out.println(aClass.getClassLoader());
    }
}
