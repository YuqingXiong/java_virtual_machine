package com.rainsun.d3_class_structure;

public class d4_lazySingleton {
    public static void main(String[] args) {
        Singleton.test(); // 仅仅输出 test
//        Singleton.getInstance();
    }
}

class Singleton{
    private Singleton(){}

    public static void test(){
        System.out.println("test");
    }

    private static class LazyHolder{
        private static final Singleton SINGLETON = new Singleton();
        static {
            System.out.println("Lazy Holder init");
        }
    }

    public static Singleton getInstance(){
        return LazyHolder.SINGLETON;
    }
}
