package org.liu.IndexManager;

class BPlusNode<T, V extends Comparable<V>> extends Node<T, V> {

    public BPlusNode(int max) {
        super(max);
    }

    @Override
    public T find(V key) {
        int i = 0;
        while (i < this.number) {
            if (key.compareTo((V) this.keys[i]) <= 0)
                break;
            i++;
        }
        if (this.number == i)
            return null;
        return this.children[i].find(key);
    }


    @Override
    public Node<T, V> insert(T value, V key, int max, int order) {
        int i = 0;
        while (i < this.number) {
            if (key.compareTo((V) this.keys[i]) < 0)
                break;
            i++;
        }
        if (key.compareTo((V) this.keys[this.number - 1]) >= 0) {
            i--;
        }
        return this.children[i].insert(value, key, max, order);
    }

    @Override
    public LeafNode<T, V> refreshLeft() {
        return this.children[0].refreshLeft();
    }

    public Node<T, V> insertNode(Node<T, V> node1, Node<T, V> node2, V key, int maxNumber, int order) {

//            System.out.println("非叶子节点,插入key: " + node1.keys[node1.number - 1] + " " + node2.keys[node2.number - 1]);

        V oldKey = null;
        if (this.number > 0)
            oldKey = (V) this.keys[this.number - 1];
        //如果原有key为null,说明这个非节点是空的,直接放入两个节点即可
        if (key == null || this.number <= 0) {
//                System.out.println("非叶子节点,插入key: " + node1.keys[node1.number - 1] + " " + node2.keys[node2.number - 1] + "直接插入");
            this.keys[0] = node1.keys[node1.number - 1];
            this.keys[1] = node2.keys[node2.number - 1];
            this.children[0] = node1;
            this.children[1] = node2;
            this.number += 2;
            return this;
        }
        //原有节点不为空,则应该先寻找原有节点的位置,然后将新的节点插入到原有节点中
        int i = 0;
        while (key.compareTo((V) this.keys[i]) != 0) {
            i++;
        }
        //左边节点的最大值可以直接插入,右边的要挪一挪再进行插入
        this.keys[i] = node1.keys[node1.number - 1];
        this.children[i] = node1;

        Object tempKeys[] = new Object[maxNumber];
        Object tempChilds[] = new Node[maxNumber];

        System.arraycopy(this.keys, 0, tempKeys, 0, i + 1);
        System.arraycopy(this.children, 0, tempChilds, 0, i + 1);
        System.arraycopy(this.keys, i + 1, tempKeys, i + 2, this.number - i - 1);
        System.arraycopy(this.children, i + 1, tempChilds, i + 2, this.number - i - 1);
        tempKeys[i + 1] = node2.keys[node2.number - 1];
        tempChilds[i + 1] = node2;

        this.number++;

        //判断是否需要拆分
        //如果不需要拆分,把数组复制回去,直接返回
        if (this.number <= order) {
            System.arraycopy(tempKeys, 0, this.keys, 0, this.number);
            System.arraycopy(tempChilds, 0, this.children, 0, this.number);

//                System.out.println("非叶子节点,插入key: " + node1.keys[node1.number - 1] + " " + node2.keys[node2.number - 1] + ", 不需要拆分");

            return null;
        }

//            System.out.println("非叶子节点,插入key: " + node1.keys[node1.number - 1] + " " + node2.keys[node2.number - 1] + ",需要拆分");

        //如果需要拆分,和拆叶子节点时类似,从中间拆开
        Integer middle = this.number / 2;

        //新建非叶子节点,作为拆分的右半部分
        BPlusNode<T, V> tempNode = new BPlusNode<T, V>(maxNumber);
        //非叶节点拆分后应该将其子节点的父节点指针更新为正确的指针
        tempNode.number = this.number - middle;
        tempNode.parent = this.parent;
        //如果父节点为空,则新建一个非叶子节点作为父节点,并且让拆分成功的两个非叶子节点的指针指向父节点
        if (this.parent == null) {

//                System.out.println("非叶子节点,插入key: " + node1.keys[node1.number - 1] + " " + node2.keys[node2.number - 1] + ",新建父节点");

            BPlusNode<T, V> tempBPlusNode = new BPlusNode<>(maxNumber);
            tempNode.parent = tempBPlusNode;
            this.parent = tempBPlusNode;
            oldKey = null;
        }
        System.arraycopy(tempKeys, middle, tempNode.keys, 0, tempNode.number);
        System.arraycopy(tempChilds, middle, tempNode.children, 0, tempNode.number);
        for (int j = 0; j < tempNode.number; j++) {
            tempNode.children[j].parent = tempNode;
        }
        //让原有非叶子节点作为左边节点
        this.number = middle;
        this.keys = new Object[maxNumber];
        this.children = new Node[maxNumber];
        System.arraycopy(tempKeys, 0, this.keys, 0, middle);
        System.arraycopy(tempChilds, 0, this.children, 0, middle);

        //叶子节点拆分成功后,需要把新生成的节点插入父节点
        BPlusNode<T, V> parentNode = (BPlusNode<T, V>) this.parent;
        return parentNode.insertNode(this, tempNode, oldKey,maxNumber,order);
    }

}