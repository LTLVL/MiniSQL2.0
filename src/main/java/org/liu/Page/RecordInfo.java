package org.liu.Page;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data //每条记录在TablePage中的偏移和长度
public class RecordInfo {
    private int offset;
    private int length;
}
