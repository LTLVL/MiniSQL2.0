package org.liu.BufferManager;

import lombok.Data;

@Data
public class BitmapPageMeta {
    public static final int BITMAP_PAGE_META_SIZE = 4  + 4  + 4; //单位为byte
    private int page_allocated_; //当前已经分配的页的数量
    private int next_free_page_; //下一个空闲的数据页
    public void increment_page_allocated_(){
        this.page_allocated_++;
    }
    public void decrement_page_allocated_(){
        this.page_allocated_--;
    }
    public void increment_next_free_page_(){
        this.next_free_page_++;
    }

    public BitmapPageMeta() {
        this.page_allocated_ = 0;
        this.next_free_page_ = 0;
    }
}
