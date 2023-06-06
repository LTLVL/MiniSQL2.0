package org.liu.RecordManager.Record;


import lombok.Data;
import org.openjdk.jol.info.ClassLayout;

import java.util.ArrayList;
import java.util.List;

@Data
public class Record {
    private final List<Row> rows = new ArrayList<>();
    private int aliveRowNum = 0; //存活的row数量
    public Long getRecordSize() { //该记录的大小，单位为byte
        return ClassLayout.parseInstance(this).instanceSize();
    }
}
