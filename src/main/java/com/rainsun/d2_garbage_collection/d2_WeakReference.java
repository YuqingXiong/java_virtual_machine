package com.rainsun.d2_garbage_collection;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
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
