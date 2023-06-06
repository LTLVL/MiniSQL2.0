package org.liu.Page;

import lombok.Data;
import org.liu.RecordManager.Record.Schema;

import java.util.ArrayList;
import java.util.List;

@Data
public class TablePageHeader {
    private int PrevPageId = -1;
    private int NextPageId = -1;
    private int FreeSpacePointer = 4096; //初始为数据页末尾
    private Schema schema = new Schema(); //表的元数据信息
    private String tableName = "";
    private int headerSize = 4 * 4; //初始为16byte
    private List<RecordInfo> recordInfos = new ArrayList<>();
}
