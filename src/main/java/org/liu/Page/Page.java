package org.liu.Page;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liu.BufferManager.BitmapPageMeta;
import org.liu.Common.MyExceptionHandler;
import org.liu.IndexManager.BPlusTree;
import org.liu.IndexManager.IndexInfo;
import org.liu.IndexManager.IndexManager;
import org.liu.RecordManager.Record.*;
import org.liu.RecordManager.Record.Record;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


// 数据页
@Data
@NoArgsConstructor
public class Page implements Serializable {
    public static final int PAGE_SIZE = 4 * 1024; //4096 byte
    public static final int BITMAP_CONTENT_SIZE = (PAGE_SIZE - BitmapPageMeta.BITMAP_PAGE_META_SIZE); //4084 byte
    private int LogicalId = -1;
    private boolean isDirty = false;
    private boolean isLocked = false;
    private boolean isUsed = false;
    private int LRUCount = 0;
    private int PrimaryPos;
    //private int referenceCount;
    private TablePageHeader tablePageHeader = new TablePageHeader();
    @JSONField(serialize = false)
    private IndexManager indexManager;
    private FreeSpace freeSpace = new FreeSpace();
    private final List<Row> rows = new ArrayList<>();

    public String GetTypeByName(String fieldName) throws MyExceptionHandler {
        for (Column column : tablePageHeader.getSchema().getColumns()) {
            if (column.getName().equals(fieldName))
                return column.getType();
        }
        throw new MyExceptionHandler(0, this.tablePageHeader.getTableName() + "表中不存在" + fieldName + "字段");
    }

    public Page(String tableName, Schema schema) {
//        create table tb_tmp01
//                (
//                        id INT(11),
//                        name VARCHAR(25),
//                        deptId INT(11),
//                        salary FLOAT
//                );
        this.tablePageHeader.setTableName(tableName);
        this.tablePageHeader.setSchema(schema);
    }

    public List<Row> GetRowsByCondition(Condition condition) {
        Iterator<Row> iterator = rows.iterator();
        List<Row> res = new ArrayList<>();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            Field field = row.getFields().get(getFieldPos(condition.getName()));
            boolean flag = condition.satisfy(field);
            if (flag) {
                res.add(row);
            }
        }
        return res;
    }

    //
    public boolean InsertRecord(Row row) throws MyExceptionHandler {
        Long rowSize = row.getRowSize();
        if (this.freeSpace.getFreeSize() < rowSize)
            return false;
        List<Column> columns = this.getTablePageHeader().getSchema().getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).isPrimaryKey() || columns.get(i).isUnique()) {
                for (int j = 0; j < rows.size(); j++) {
                    if (rows.get(j).getFields().get(i).getValue().equals(row.getFields().get(i).getValue())) {
                        throw new MyExceptionHandler(0, "唯一约束冲突");
                    }
                }
            }
        }
        Long rowId = ((long) LogicalId >>> 32 + rows.size());
        row.setRowId(rowId);
        rows.add(row);
        freeSpace.setEnd((int) (freeSpace.getEnd() - rowSize));
        tablePageHeader.setFreeSpacePointer(freeSpace.getEnd());
        tablePageHeader.getRecordInfos().add(new RecordInfo(freeSpace.getEnd() - freeSpace.getStart(), Math.toIntExact(rowSize)));
        tablePageHeader.setHeaderSize(tablePageHeader.getHeaderSize() + 8);
        freeSpace.setStart(freeSpace.getStart() + 8);
        freeSpace.setFreeSize(freeSpace.getEnd() - freeSpace.getStart());
        this.indexManager.insert(row);
        return true;
    }


    public int getFieldPos(String fieldName) {
        List<Column> columns = getTablePageHeader().getSchema().getColumns();
        int pos = 0;
        for (int i = 0; i < columns.size(); i++) {
            if (fieldName.equals(columns.get(i).getName())) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    public boolean DeleteRows(Condition condition) {
        // DELETE FROM t_student WHERE id = 2 and name = '张三';
        Iterator<Row> iterator = rows.iterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            Field field = row.getFields().get(getFieldPos(condition.getName()));
            boolean flag = condition.satisfy(field);
            if (flag) {
                iterator.remove();
            }
        }
        return true;
    }

    public boolean DeleteRows(List<Condition> conditions, List<String> relations) {
        // DELETE FROM t_student WHERE id = 2 and name = '张三';
        Iterator<Row> iterator = rows.iterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            Field field = row.getFields().get(getFieldPos(conditions.get(0).getName()));
            boolean flag = conditions.get(0).satisfy(field);
            for (int i = 1; i < conditions.size(); i++) {
                boolean flag2 = conditions.get(i).satisfy(field);
                if (relations.get(i - 1).equals("and")) {
                    flag = flag & flag2;
                } else {
                    flag = flag | flag2;
                }
            }
            if (flag) {
                iterator.remove();
            }
        }
        return true;
    }

    public int UpdateRecord(Row row, Long rowId) { //将RowId为rid的记录old_row替换成新的记录new_row
        Long newSize = row.getRowSize();
        for (int i = 0; i < rows.size(); i++) {
            Long id = rows.get(i).getRowId();
            if (id.equals(rowId)) {
                Long oldSize = row.getRowSize();
                if (this.freeSpace.getFreeSize() < newSize - oldSize)
                    return 1; //内存不够
                rows.set(i, row);
                row.setRowId(rowId);
                freeSpace.setEnd((int) (freeSpace.getEnd() + oldSize - newSize));
                freeSpace.setFreeSize(freeSpace.getEnd() - freeSpace.getStart());
                tablePageHeader.setFreeSpacePointer((int) (tablePageHeader.getFreeSpacePointer() + oldSize - newSize));
                tablePageHeader.getRecordInfos().set(i, new RecordInfo(freeSpace.getEnd() - freeSpace.getStart(), Math.toIntExact(newSize)));
                return 0;
            }
        }
        return 2; //没有此rowId
    }

    public Row GetTuple(Long rowId) { //获取RowId为row->rid_的记录
        for (Row row : rows) {
            if (row.getRowId().equals(rowId)) {
                return row;
            }
        }
        return null;
    }

    public void CreateIndex(IndexInfo indexInfo) {
        switch (indexInfo.getType()) {
            case "int" -> {
                BPlusTree<Integer, Integer> tree = new BPlusTree<>(171, getFieldPos(indexInfo.getColumnName()));
                for (Row row : rows) {
                    int PrimaryValue = row.getFields().get(PrimaryPos).getIntValue();
                    int indexValue = (int) row.getFields().get(tree.getIndexPos()).getValue();
                    tree.insert(PrimaryValue, indexValue);
                }
                tree.setIndexName(indexInfo.getIndexName());
                indexManager.intTreeMap.put(indexInfo.getIndexName(), tree);
            }
            case "string" -> {
                BPlusTree<Integer, String> tree = new BPlusTree<>(171, getFieldPos(indexInfo.getColumnName()));
                for (Row row : rows) {
                    int PrimaryValue = row.getFields().get(PrimaryPos).getIntValue();
                    String indexValue = (String) row.getFields().get(tree.getIndexPos()).getValue();
                    tree.insert(PrimaryValue, indexValue);
                }
                tree.setIndexName(indexInfo.getIndexName());
                indexManager.charTreeMap.put(indexInfo.getIndexName(), tree);
            }
            case "float" -> {
                BPlusTree<Integer, Float> tree = new BPlusTree<>(171, getFieldPos(indexInfo.getColumnName()));
                for (Row row : rows) {
                    int PrimaryValue = row.getFields().get(PrimaryPos).getIntValue();
                    Float indexValue = (Float) row.getFields().get(tree.getIndexPos()).getValue();
                    tree.insert(PrimaryValue, indexValue);
                }
                tree.setIndexName(indexInfo.getIndexName());
                indexManager.floatTreeMap.put(indexInfo.getIndexName(), tree);
            }
        }
    }


    public void DropIndex(IndexInfo indexInfo) throws MyExceptionHandler {
        if(indexInfo.getIndexName().equals(indexManager.PrimaryIndex.getIndexName())){
            throw new MyExceptionHandler(0,"不能删除主键索引");
        }
        switch (indexInfo.getType()){
            case "int"->{
                indexManager.intTreeMap.remove(indexInfo.getIndexName());
            }
            case "string"->{
                indexManager.charTreeMap.remove(indexInfo.getIndexName());
            }
            case "float"->{
                indexManager.floatTreeMap.remove(indexInfo.getIndexName());
            }
        }
    }
}

