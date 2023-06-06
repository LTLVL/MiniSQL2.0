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

    /**
     * 进行查找,经典二分查找,不多加注释
     *
     * @param key
     * @return
     */
    @Override
    public T find(V key) {
        if (this.number <= 0)
            return null;

//            System.out.println("叶子节点查找");

        Integer left = 0;
        Integer right = this.number;

        Integer middle = (left + right) / 2;

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

    /**
     * @param value
     * @param key
     */
    @Override
    public Node<T, V> insert(T value, V key, int maxNumber, int order) {

//            System.out.println("叶子节点,插入key: " + key);

        //保存原始存在父节点的key值
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

        //复制数组,完成添加
        Object tempKeys[] = new Object[maxNumber];
        Object tempValues[] = new Object[maxNumber];
        System.arraycopy(this.keys, 0, tempKeys, 0, i);
        System.arraycopy(this.values, 0, tempValues, 0, i);
        System.arraycopy(this.keys, i, tempKeys, i + 1, this.number - i);
        System.arraycopy(this.values, i, tempValues, i + 1, this.number - i);
        tempKeys[i] = key;
        tempValues[i] = value;

        this.number++;

//            System.out.println("插入完成,当前节点key为:");
//            for(int j = 0; j < this.number; j++)
//                System.out.print(tempKeys[j] + " ");
//            System.out.println();

        //判断是否需要拆分
        //如果不需要拆分完成复制后直接返回
        if (this.number <= order) {
            System.arraycopy(tempKeys, 0, this.keys, 0, this.number);
            System.arraycopy(tempValues, 0, this.values, 0, this.number);

            //有可能虽然没有节点分裂，但是实际上插入的值大于了原来的最大值，所以所有父节点的边界值都要进行更新
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
//                System.out.println("叶子节点,插入key: " + key + ",不需要拆分");

            return null;
        }

//            System.out.println("叶子节点,插入key: " + key + ",需要拆分");

        //如果需要拆分,则从中间把节点拆分差不多的两部分
        Integer middle = this.number / 2;

        //新建叶子节点,作为拆分的右半部分
        LeafNode<T, V> tempNode = new LeafNode<T, V>(maxNumber);
        tempNode.number = this.number - middle;
        tempNode.parent = this.parent;
        //如果父节点为空,则新建一个非叶子节点作为父节点,并且让拆分成功的两个叶子节点的指针指向父节点
        if (this.parent == null) {

//                System.out.println("叶子节点,插入key: " + key + ",父节点为空 新建父节点");

            BPlusNode<T, V> tempBPlusNode = new BPlusNode<>(maxNumber);
            tempNode.parent = tempBPlusNode;
            this.parent = tempBPlusNode;
            oldKey = null;
        }
        System.arraycopy(tempKeys, middle, tempNode.keys, 0, tempNode.number);
        System.arraycopy(tempValues, middle, tempNode.values, 0, tempNode.number);

        //让原有叶子节点作为拆分的左半部分
        this.number = middle;
        this.keys = new Object[maxNumber];
        this.values = new Object[maxNumber];
        System.arraycopy(tempKeys, 0, this.keys, 0, middle);
        System.arraycopy(tempValues, 0, this.values, 0, middle);

        this.right = tempNode;
        tempNode.left = this;

        //叶子节点拆分成功后,需要把新生成的节点插入父节点
        BPlusNode<T, V> parentNode = (BPlusNode<T, V>) this.parent;
        return null;
        //return parentNode.insertNode(this, tempNode, oldKey, maxNumber, order);
    }

    @Override
    public LeafNode<T, V> refreshLeft() {
        if (this.number <= 0)
            return null;
        return this;
    }
}
