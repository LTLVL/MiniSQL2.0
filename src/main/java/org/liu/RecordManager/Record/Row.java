package org.liu.RecordManager.Record;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.openjdk.jol.info.ClassLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data //与元组的概念等价，用于存储记录或索引键，一个Row由一个或多个Field构成
public class Row implements Serializable {
    private List<Field> fields = new ArrayList<>();
    // 高32位存储的是该RowId对应记录所在数据页的page_id
    // 低32位(slot_num)存储的是该RowId在page_id对应的数据页中对应的是第几条记录
//    private List<Integer> integers = new ArrayList<>(){{
//        add(1);
//        add(2);
//    }};
    private Long RowId;
    private boolean DeleteMask = false; //标记记录是否被删除

    public Long getRowSize() {
        return ClassLayout.parseInstance(this).instanceSize();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Field field : fields) {
            s.append(field.toString());
        }
        return s.toString();
    }

}
