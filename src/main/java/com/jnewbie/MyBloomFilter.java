package com.jnewbie;

import java.util.BitSet;

/**
 * @program: jnewbie
 * @description: 布隆去重
 * @author: pingc
 * @create: 2021-11-05 18:27
 **/
public class MyBloomFilter {
    //定义BitSet默认大小,2 << 25没有超过int范围
    private final static int DEFAULT_SIZE = 2 << 25;
    //创建BitSet
    private static BitSet bitSet = new BitSet(DEFAULT_SIZE);
    //定义哈希种子,类型为质数、个数决定哈希函数个数
    private static int[] seeds = new int[]{5, 7, 11, 13, 31, 37, 61};
    //定义哈希函数数组
    private static MyHash[] func = new MyHash[seeds.length];

    public MyBloomFilter() {
        //....构造所需的哈希函数
        for (int i = 0; i < seeds.length; i++) {
            func[i] = new MyHash(DEFAULT_SIZE, seeds[i]);
        }
    }

    /**
     * 将字符串标记到bits中
     */
    public  void add(String url) {
        if (url != null) {
            for (MyHash f : func
            ) {
                //true代表这个位置被标记了
                bitSet.set(f.hash(url), true);
            }
        }
    }

    /**
     * 判断url是否已经被BitSet标记过
     */
    public  boolean contain(String url) {
        if (url == null) {
            return false;
        }
        boolean flag = true;
        //循环判断bitset中是否包含该url
        for (MyHash f : func
        ) {
            flag = flag && bitSet.get(f.hash(url));
        }
        return flag;
    }

    /**
     * 定义一个静态内部类MyHash，实现哈希函数功能
     */
    public static class MyHash {
        /**
         * cap = DEFAULT_SIZE
         */
        private int cap;
        private int seed;

        public MyHash(int cap, int seed) {
            this.cap = cap;
            this.seed = seed;
        }

        public int hash(String url) {
            int no = 0;
            for (int i = 0; i < url.length(); i++) {
                no = no * seed + url.charAt(i);
            }
            return (cap - 1) & no;
        }
    }
}