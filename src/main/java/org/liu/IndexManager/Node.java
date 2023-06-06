package org.liu.IndexManager;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class Node<T, V extends Comparable<V>> {
    //父节点
    protected Node<T, V> parent;
    //子节点
    protected Node<T, V>[] children;
    //键（子节点）数量
    protected Integer number;
    //键
    protected Object[] keys;

    //构造方法
    public Node(int maxNumber) {
        this.keys = new Object[maxNumber];
        this.children = new Node[maxNumber];
        this.number = 0;
        this.parent = null;
    }

    //查找
    public abstract T find(V key);

    //插入
    public abstract Node<T, V> insert(T value, V key, int maxNumber, int order);

    public abstract LeafNode<T, V> refreshLeft();
}
