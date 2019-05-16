package simpledb;

/**
 * author : lx
 * date : 19-4-20 上午10:45
 */
import java.math.BigInteger;
import java.util.ArrayList;

public class Solution {
    public static void main(String[] args){
       String s =  new Solution().PrintMinNumber(new int[]{1,2,3});
       System.out.println(s);
    }
    public String PrintMinNumber(int [] numbers) {
        //考虑是否可以用动规做 即a[i] 是以i为结尾下标的 所有的数所能排列成的最小的数、
        // 让后新的数利用这个数，加载前面或者后面，计算两者最小值，那么没有放在中间的情况吗
        // 考虑 2 3 1 a[2] = 23 + 1* 100 不是最小，
        // 考虑 2 1 3 a[2] = 12 * 10 + 3  最小，
        // 考虑 3 1 2 a[2] = 13 * 10 + 2  不是最小 此动规状态转移失败。

        // 考虑暴力组合，组合出所有的数 仍然用动规，即a[i] 是以i为结尾下标的所有全排列的可能
        // a[i] 的元素可以使一个alist<blist>，blist中存储的是数值，或者是下标值
        // 不论如何都很费劲
        int n = numbers.length;
        if( n == 0 ){
            return "";
        }
        Integer[] num = new Integer[n];
        for(int i =0;i < n;i ++){
            num[i] = new Integer(numbers[i]);
        }
        ArrayList<ArrayList<Integer>>[] ans = new ArrayList[n];
        ans[0] = new ArrayList<ArrayList<Integer>>();
        ArrayList tmp = new ArrayList();
        tmp.add(num[0]);
        ans[0].add(tmp);
        for(int i = 1 ; i < n ;i ++){
            int last_2 = ans[i-1].size();
            ans[i] = new ArrayList<ArrayList<Integer>>();
            for(int j = 0; j < last_2; j ++){
                for(int k = 0; k <= i; k++){
                    tmp = (ArrayList<Integer>)ans[i-1].get(j).clone();
                    tmp.add(k,num[i]);
                    ans[i].add(tmp);
                }

            }
        }
//        for(int i = 0 ; i < n; i ++){
//            for(ArrayList<Integer> a: ans[i]){
//                for( Integer b : a){
//
//                    System.out.print(b);
//                    System.out.print(" ");
//
//                }
//                    System.out.println();
//                }
//            }
        long minimum = Long.MAX_VALUE;
        for(int i = 0 ; i < ans[n-1].size(); i++){
            long res = 0;
            for(Integer t:ans[n-1].get(i)){

                System.out.print(t);
                res = plus2(res,t);

            }
            System.out.println();
            minimum = Math.min(res , minimum);

        }

        return String.valueOf(minimum);
    }

    public long plus2(long i, long j ){
        long tmp = j;
        int wei = 1;
        while((tmp /= 10) != 0){
            wei ++;
        }
//        System.out.println(i * (10 ^ wei) + j);
        for(int x = 0 ; x < wei ; x ++){
            i *= 10;
        }
        return  i + j;
    }
}