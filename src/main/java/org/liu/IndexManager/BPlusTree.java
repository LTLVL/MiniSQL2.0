package org.liu.IndexManager;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
public class BPlusTree<T, V extends Comparable<V>> implements Serializable { //K为索引字段的数据，V为对应的rowId
    //B+树的阶
    protected Integer order;
    protected Integer maxNumber;
    protected int IndexPos;
    protected String IndexName;
    private Node<T, V> root;
    private LeafNode<T, V> left;

    //有参构造方法，可以设定B+树的阶
    public BPlusTree(Integer bTreeOrder, int IndexPos) {
        this.order = bTreeOrder;
        //this.minNUmber = (int) Math.ceil(1.0 * bTreeOrder / 2.0);
        //因为插入节点过程中可能出现超过上限的情况,所以这里要加1
        this.maxNumber = bTreeOrder + 1;
        this.root = new LeafNode<T, V>(maxNumber);
        this.left = null;
        this.IndexPos = IndexPos;
    }


    //查询
    public T find(V key) {
        T t = this.root.find(key);
        if (t == null) {
            System.out.println("不存在");
        }
        return t;
    }

    //插入
    public void insert(T value, V key) {
        if (key == null)
            return;
        Node<T, V> t = this.root.insert(value, key, maxNumber, order);
        if (t != null)
            this.root = t;
        this.left = (LeafNode<T, V>) this.root.refreshLeft();

//        System.out.println("插入完成,当前根节点为:");
//        for(int j = 0; j < this.root.number; j++) {
//            System.out.print((V) this.root.keys[j] + " ");
//        }
//        System.out.println();
    }

}
