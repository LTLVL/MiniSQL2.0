package org.liu.Page;

import lombok.Data;

@Data
public class FreeSpace {
    private int start = 16; // 空闲空间开始处，初始为16，单位为byte
    private int end = 4096; // 空闲空间结束处，单位为byte
    private int FreeSize = end - start; // 空闲空间大小，单位为byte
}
