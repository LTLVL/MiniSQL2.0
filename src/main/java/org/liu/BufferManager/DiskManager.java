package org.liu.BufferManager;

import lombok.Data;
import org.liu.Page.Page;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;


@Data
public class DiskManager {
    private final Integer MAX_EXTENT_COUNT = 1; //最大允许分区数
    private DiskMetaPage diskMetaPage = new DiskMetaPage(); //元数据
    private List<Extent> extents = new ArrayList<>(0); //管理的分区
    private List<Integer> FreePages = new ArrayList<>(0); //空闲数据页列表, 使用逻辑id标识
    private Deque<Integer> UnfreePages = new LinkedList<>(); //非空闲数据页链表，用于实现LRU算法
    private List<Integer> PinPages = new ArrayList<>();// 被固定的数据页

    public int AllocatePage(Page page) { //从磁盘中按顺序分配一个空闲页，并返回空闲页的逻辑页号；
        if (FreePages.size() == 0 && extents.size() == MAX_EXTENT_COUNT) { //分区数已达上限
            LRU();
        }
        if (diskMetaPage.getEXTENT_COUNT() == 0 //如果磁盘中分区数为0或者最后一个分区的位图页显示已满
                || FreePages.size() == 0) { // 32672
            Extent extent = new Extent();
            diskMetaPage.incrementCount();
            int i = extent.AllocatePage(page, 0);//分区中分配数据页
            diskMetaPage.incrementPageCount(0);
            extents.add(extent);
            int logicId = i + (extents.size() - 1) * Page.BITMAP_CONTENT_SIZE * 8;
            for (int j = logicId; j < logicId + Page.BITMAP_CONTENT_SIZE * 8; j++) { //新建分区时将所有数据页加入空闲列表
                FreePages.add(j);
            }
            page.setLogicalId(logicId);
            Integer logic = logicId;
            FreePages.remove(logic);
            UnfreePages.add(logic);
            return logicId;
        }
        Extent extent = extents.get(diskMetaPage.getEXTENT_COUNT() - 1);
        int logicalId = FreePages.get(0);
        logicalId %= Page.BITMAP_CONTENT_SIZE * 8;
        extent.AllocatePage(page, logicalId);
        diskMetaPage.incrementPageCount(extents.size() - 1);
        page.setLogicalId(logicalId);
        Integer logic = logicalId;
        FreePages.remove(logic);
        UnfreePages.add(logic);
        return logicalId;
    }

    public boolean DeAllocate(Integer logicalPageId) { // 释放磁盘中逻辑页号对应的物理页
        if (logicalPageId > diskMetaPage.getEXTENT_COUNT() * (Page.BITMAP_CONTENT_SIZE * 8)) {
            return false; // 超出范围
        }
        int extendCount = logicalPageId / (Page.BITMAP_CONTENT_SIZE * 8);// 该数据页位于第几个分区
        int pageCount = logicalPageId % (Page.BITMAP_CONTENT_SIZE * 8);// 该数据页位于分区中的第几个
        diskMetaPage.decrementPageCount(extendCount);
        Extent extent = extents.get(extendCount);
        FreePages.add(logicalPageId);
        UnfreePages.remove(logicalPageId);
        return extent.DeAllocatePage(pageCount);
    }

    public boolean IsPageFree(Integer logicalPageId) {
        if (logicalPageId > diskMetaPage.getEXTENT_COUNT() * (Page.BITMAP_CONTENT_SIZE * 8)) {
            return false; // 超出范围
        }
        int extendCount = logicalPageId / (Page.BITMAP_CONTENT_SIZE * 8);// 该数据页位于第几个分区
        int pageCount = logicalPageId % (Page.BITMAP_CONTENT_SIZE * 8);// 该数据页位于分区中的第几个
        Extent extent = extents.get(extendCount);
        return extent.IsPageFree(pageCount);
    }

    public Page readPage(Integer logicalPageId) {
        if (logicalPageId > diskMetaPage.getEXTENT_COUNT() * (Page.BITMAP_CONTENT_SIZE * 8)) {
            return null; // 超出范围
        }
        int extendCount = logicalPageId / (Page.BITMAP_CONTENT_SIZE * 8);// 该数据页位于第几个分区
        int pageCount = logicalPageId % (Page.BITMAP_CONTENT_SIZE * 8);// 该数据页位于分区中的第几个
        Extent extent = extents.get(extendCount);
        Page[] extentPages = extent.getExtentPages();
        UnfreePages.remove(logicalPageId);
        UnfreePages.add(logicalPageId);
        return extentPages[pageCount];
    }

    public boolean writePage(Page page, Integer logicalPageId) { //按照逻辑id写入page
        boolean flag = IsPageFree(logicalPageId);
        if (!flag)
            return false;
        int extendCount = logicalPageId / (Page.BITMAP_CONTENT_SIZE * 8);// 该数据页位于第几个分区
        int pageCount = logicalPageId % (Page.BITMAP_CONTENT_SIZE * 8);// 该数据页位于分区中的第几个
        Extent extent = extents.get(extendCount);
        Page[] extentPages = extent.getExtentPages();
        extentPages[pageCount] = page;
        FreePages.remove(logicalPageId);
        UnfreePages.add(logicalPageId);
        return true;
    }

    public void LRU() { //磁盘满时替换
        Integer integer = UnfreePages.removeFirst();
        DeAllocate(integer);
    }

    public void Pin(int logicalId) { //固定某个数据页
        if (UnfreePages.contains(logicalId)) {
            UnfreePages.remove(logicalId);
            PinPages.add(logicalId);
        }
    }

    public void UnPin(int logicalId) { //固定某个数据页
        if (UnfreePages.contains(logicalId)) {
            PinPages.remove(logicalId);
            UnfreePages.add(logicalId);
        }
    }

    public int LRUSize() { //此方法返回当前能够被替换的数据页的数量。
        return UnfreePages.size();
    }

}
