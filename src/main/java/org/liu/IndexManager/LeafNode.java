package org.liu.IndexManager;

import lombok.NoArgsConstructor;

@NoArgsConstructor
class LeafNode<T, V extends Comparable<V>> extends Node<T, V> {

    protected Object values[];
    protected LeafNode left;
    protected LeafNode right;

    public LeafNode(int maxNumber) {
        super(maxNumber);
        this.values = new Object[maxNumber];
        this.left = null;
        this.right = null;
    }


    @Override
    public T find(V key) {
        if (this.number <= 0)
            return null;
        Integer left = 0;
        Integer right = this.number;
        int middle = (left + right) / 2;
        while (left < right) {
            V middleKey = (V) this.keys[middle];
            if (key.compareTo(middleKey) == 0)
                return (T) this.values[middle];
            else if (key.compareTo(middleKey) < 0)
                right = middle;
            else
                left = middle;
            middle = (left + right) / 2;
        }
        return null;
    }

    @Override
    public Node<T, V> insert(T value, V key, int maxNumber, int order) {
        V oldKey = null;
        if (this.number > 0)
            oldKey = (V) this.keys[this.number - 1];
        //先插入数据
        int i = 0;
        while (i < this.number) {
            if (key.compareTo((V) this.keys[i]) < 0)
                break;
            i++;
        }

        Object tempKeys[] = new Object[maxNumber];
        Object tempValues[] = new Object[maxNumber];
        System.arraycopy(this.keys, 0, tempKeys, 0, i);
        System.arraycopy(this.values, 0, tempValues, 0, i);
        System.arraycopy(this.keys, i, tempKeys, i + 1, this.number - i);
        System.arraycopy(this.values, i, tempValues, i + 1, this.number - i);
        tempKeys[i] = key;
        tempValues[i] = value;

        this.number++;
        if (this.number <= order) {
            System.arraycopy(tempKeys, 0, this.keys, 0, this.number);
            System.arraycopy(tempValues, 0, this.values, 0, this.number);
            Node node = this;
            while (node.parent != null) {
                V tempkey = (V) node.keys[node.number - 1];
                if (tempkey.compareTo((V) node.parent.keys[node.parent.number - 1]) > 0) {
                    node.parent.keys[node.parent.number - 1] = tempkey;
                    node = node.parent;
                } else {
                    break;
                }
            }
            return null;
        }

        Integer middle = this.number / 2;
        LeafNode<T, V> tempNode = new LeafNode<T, V>(maxNumber);
        tempNode.number = this.number - middle;
        tempNode.parent = this.parent;
        if (this.parent == null) {
            BPlusNode<T, V> tempBPlusNode = new BPlusNode<>(maxNumber);
            tempNode.parent = tempBPlusNode;
            this.parent = tempBPlusNode;
            oldKey = null;
        }
        System.arraycopy(tempKeys, middle, tempNode.keys, 0, tempNode.number);
        System.arraycopy(tempValues, middle, tempNode.values, 0, tempNode.number);
        this.number = middle;
        this.keys = new Object[maxNumber];
        this.values = new Object[maxNumber];
        System.arraycopy(tempKeys, 0, this.keys, 0, middle);
        System.arraycopy(tempValues, 0, this.values, 0, middle);

        this.right = tempNode;
        tempNode.left = this;
        BPlusNode<T, V> parentNode = (BPlusNode<T, V>) this.parent;
        return parentNode.insertNode(this, tempNode, oldKey, maxNumber, order);
    }

    @Override
    public LeafNode<T, V> refreshLeft() {
        if (this.number <= 0)
            return null;
        return this;
    }
}
