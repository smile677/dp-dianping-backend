package com.smile67.代码随想录.数组;

/**
 * 二分查找
 *
 * @author 刘棋~
 * @version 1.0
 * @projectName sky-take-out
 * @package 代码随想录.数组.
 * @className test2
 * @date 2023/7/18 23:47
 */
public class Search_2 {
    //左闭右闭
    public static int search(int[] nums, int target) {
        //避免当target小于nums[0] 或者 大于nums[nums.length - 1]时多次循环运算
        if (target < nums[0] || target > nums[nums.length -1]){
            return -1;
        }
        int left = 0, right = nums.length - 1;
        while (left <= right){
            //避免越界
            int mid = left + ((right-left)>>1);
            if (nums[mid] < target){
                left = mid + 1;
            }else if (nums[mid] > target){
                right = mid - 1;
            }
            if (nums[mid] == target)
                return mid;
        }
        return -1;
    }


    public static void main(String[] args) {
        int[] a = {-1, 0, 3, 5, 9, 12};
        int b = 9;
        int c  = search(a,b);
        System.out.println(c);
    }
}
