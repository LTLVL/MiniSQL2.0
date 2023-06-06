package org.liu.RecordManager.Record;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data //字段
@AllArgsConstructor
public class Column implements Serializable {
    private String name;
    private String type;
    private boolean isUnique;
    private boolean isPrimaryKey;

    @Override
    public String toString() {
        return name + "\t";
    }
}
