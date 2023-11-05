package com.smile67.代码随想录.链表;

/**
 * 删除链表元素
 * @author smile67~
 * @Description: 代码随想录.链表
 * @version: 1.0
 */
public class Solution_203 {
    /**
     * 添加虚拟结点的方式
     * 时间复杂度 O(n)
     * 空间复杂读 O(1)
     *
     * @param head
     * @param val
     * @return
     */
    public ListNode removeElements1(ListNode head, int val) {
        //链表为空
        if (head == null) {
            return head;
        }
        // 因为删除可能涉及到头结点，所以设置dummy结点，统一操作
        ListNode dummy = new ListNode(-1, head);
        ListNode pre = dummy;
        ListNode cur = head;

        while (cur != null) {
            if (cur.val == val) {
                pre.next = cur.next;
            } else {
                pre = cur;
            }
            cur = cur.next;
        }

        //返回头节点
        return dummy.next;//不返回head是因为head可能被删
    }

    /**
     * 不添加虚拟结点的方式
     * 时间复杂度 O(n)
     * 空间复杂读 O(1)
     *
     * @param head
     * @param val
     * @return
     */
    public ListNode removeElements2(ListNode head, int val) {
        // 删除的是头结点
        while (head != null && head.val == val) {
            head = head.next;
        }
        // 链表已经为空
        if (head == null) {
            return head;
        }
        // 已经确定当前head.val != val
        ListNode pre = head;
        ListNode cur = head.next;
        while (cur != null) {
            if (head.val == val) {
                pre.next = cur.next;
            } else {
                pre = cur;
            }
            cur = cur.next;
        }
        return head;
    }

    /**
     * 不添加虚拟头结点和 pre 节点的方式
     * 时间复杂度 O(n)
     * 空间复杂读 O(1)
     *
     * @param head
     * @param val
     * @return
     */
    public ListNode removeElements(ListNode head, int val) {
        // 删除头结点
        while (head != null && head.val == val) {
            head = head.next;
        }
        // 不使用pre节点
        ListNode curr = head;
        while (curr != null) {
            if (curr.next != null && curr.next.val == val) {
                curr.next = curr.next.next;
            }
            curr = curr.next;
        }
        return head;//返回头结点
    }
}