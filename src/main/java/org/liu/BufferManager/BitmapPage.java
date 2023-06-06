package org.liu.BufferManager;

import lombok.Data;
import org.liu.Page.Page;


// 标记一段连续页的分配情况
@Data
public class BitmapPage extends Page {
    private BitmapPageMeta bitmapPageMeta;
    private byte[] bitmapContent = new byte[BITMAP_CONTENT_SIZE * 8];// 管理的数据页个数：32672
    public static final int EOF = -1; //位图页已满

    public BitmapPage() {
        this.bitmapPageMeta = new BitmapPageMeta();
    }

    public int AllocatePage(int pos) {
        if(this.bitmapPageMeta.getPage_allocated_()==BITMAP_CONTENT_SIZE * 8){
            return EOF; // 空间已满
        }
        this.bitmapContent[pos] = 1; //分配空间
        this.bitmapPageMeta.increment_page_allocated_(); //更新位图大小
        this.bitmapPageMeta.increment_next_free_page_(); //更新空闲指针
        return pos;
    }

    public boolean DeAllocatePage(int page_offset) {
        if(this.bitmapContent[page_offset]==0){
            return false; //该页未被分配
        }
        this.bitmapPageMeta.decrement_page_allocated_();
        this.bitmapContent[page_offset] = 0;
        return true;
    }

    public boolean IsPageFree(int page_offset) { //true表示未分配
        return this.bitmapContent[page_offset]==0;
    }

}
