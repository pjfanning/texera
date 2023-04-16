package edu.uci.ics.amber.engine.common.lbmq;

public class MultiQueueNode<E> {

    E item;

    /*
     * One of:
     * - the real successor Node
     * - this Node, meaning the successor is head.next
     * - null, meaning there is no successor (this is the last node)
     */
    MultiQueueNode<E> next = null;

    MultiQueueNode(E item) {
        this.item = item;
    }
}
