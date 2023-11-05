package com.smile67.代码随想录.链表;

/**
 * @author smile67~
 * @date: 2023/9/13 - 09 - 13 - 22:37
 * @Description: 代码随想录.链表.单链表
 * @version: 1.0
 */
public class ListNode {
    //结点的值
    int val;
    //下一个结点
    ListNode next;

    //结点的构造函数(无参)
    public ListNode() {
    }

    //结点的构造函数(有一个参数)
    public ListNode(int val) {
        this.val = val;
    }

    //结点的构造函数(有两个参数)
    public ListNode(int val, ListNode next) {
        this.val = val;
        this.next = next;
    }

}
