package org.liu.BufferManager;

import lombok.Data;
import org.liu.Page.Page;

import java.util.ArrayList;
import java.util.List;

// 分区元数据
@Data
public class DiskMetaPage extends Page {
    private int EXTENT_COUNT = 0; //管理的分区数量
    private List<Integer> EXTENT_PAGE_COUNT = new ArrayList<>(0); //每个分区中已经分配的页的数量

    public void incrementCount() {
        EXTENT_COUNT++;
        EXTENT_PAGE_COUNT.add(0);
    }

    public void decrementCount(int pos) {
        EXTENT_COUNT--;
        EXTENT_PAGE_COUNT.remove(pos);
    }

    public void incrementPageCount(int pos) {
        Integer integer = EXTENT_PAGE_COUNT.get(pos) + 1;
        EXTENT_PAGE_COUNT.set(pos, integer);
    }

    public void decrementPageCount(int pos) {
        Integer integer = EXTENT_PAGE_COUNT.get(pos) - 1;
        EXTENT_PAGE_COUNT.set(pos, integer);
    }
}
