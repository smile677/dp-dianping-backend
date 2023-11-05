package com.smile67.代码随想录.数组;


public class removeElement_3 {
    //暴力解法
    // 时间复杂度：O(n^2)
    // 空间复杂度：O(1)
     static int removeElement(int[] nums, int val) {
         int size = nums.length;
         for (int i = 0; i < size; i++) {
             if (nums[i] == val) {//发现需要移动的元素，就将后面的素组集体向前移动一位
                 for (int j = i + 1; j < size; j++) {
                     nums[j-1] = nums[j];
                 }
                 i--;//因为删除了一个元素，所以需要将当前索引-1，
                 size--;//此时数组大小-1;
             }
         }
         return size;
     }


    public static void main(String[] args) {
        int[] nums = {0, 1, 2, 2, 3, 0, 4, 2};
        int val = 2;
        int size = removeElement(nums, val);
        System.out.println(size);
    }
}
