package simpledb.eviction;

import simpledb.Page;
import simpledb.PageId;
import simpledb.TransactionId;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * author : lx
 * date : 19-5-1 下午9:03
 */
public class LRUPolicy<k extends PageId, v extends Page> extends EvictionPolicy<PageId, Page>{
    // 参考 https://blog.csdn.net/ShierJun/article/details/51253870
    private LinkedHashMap<k,v> pages;
    private int numPages = 0;
    public LRUPolicy(int numPages){
        super(numPages);

        pages = new LinkedHashMap<k,v>(numPages, 0.75F, true){
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest){
                // 可以看到这个类可以访问到外部类的private变量的
                return size() > LRUPolicy.this.numPages;
            }
        };


    }

    // 是否可以用synchronized 呢
    public Page getPage(PageId key) {
//        System.out.println("xx");

        return pages.get(key);
    }

    public void putPage(PageId key, Page val) {
        pages.put((k) key,(v) val);
//        System.out.println("pp");
    }


    public static void main(String[] args) {
        EvictionPolicy<PageId, Page> p = new LRUPolicy<PageId, Page>(5);

    }
}


