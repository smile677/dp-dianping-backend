package com.smile67.代码随想录.链表;

/**
 * @author smile67~
 * @date: 2023/9/14 - 09 - 14 - 21:42
 * @Description: 代码随想录.链表
 * @version: 1.0
 */
public class JavaGuide_Test {
    public static void main(String[] args) {
        int i = -1;
        System.out.println("初始数据：" + i);
        System.out.println("初始数据对应的二进制字符串：" + Integer.toBinaryString(i));
        i <<= 10;
        System.out.println("左移 10 位后的数据 " + i);
        System.out.println("左移 10 位后的数据对应的二进制字符 " + Integer.toBinaryString(i));

    }
}
