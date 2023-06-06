package org.liu.RecordManager.Record;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data //用于表示一个数据表或是一个索引的结构。一个Schema由一个或多个的Column构成；
public class Schema implements Serializable {
    private List<Column> columns = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Column column : columns) {
            s.append(column);
        }
        return s.toString();
    }
}
