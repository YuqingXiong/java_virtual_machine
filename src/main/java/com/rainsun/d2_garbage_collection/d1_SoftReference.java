package com.rainsun.d2_garbage_collection;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * -Xmx20m
 */
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
