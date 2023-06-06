package org.liu.BufferManager;

import lombok.Data;
import org.liu.Page.Page;

// 分区，一个位图页加一段连续的数据页
@Data
public class Extent {
    private BitmapPage bitmapPage = new BitmapPage(); //位图页
    private Page[] ExtentPages = new Page[4084 * 8]; //一个分区32672个数据页
    private int nextFreePage = 0;
    private int PageSize = 0;

    public int AllocatePage(Page page, int pos) { //为数据页分配空间
        this.PageSize++;
        this.ExtentPages[pos] = page;
        this.nextFreePage++;
        return this.bitmapPage.AllocatePage(pos);
    }

    public boolean DeAllocatePage(int PageNum) {
        if (this.ExtentPages[PageNum] == null)
            return false;
        this.PageSize--;
        this.ExtentPages[PageNum] = null;
        return true;
    }

    public boolean IsPageFree(int pos) {
        return ExtentPages[pos] == null;
    }
}
