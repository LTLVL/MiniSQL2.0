package org.liu.IndexManager;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liu.RecordManager.Record.Record;
import org.liu.RecordManager.Record.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;


@Data
@Slf4j
public class IndexManager {
    // 索引名与对应的B+树集合
    private String DataBaseName;
    private String TableName;
    public BPlusTree<Row, Integer> PrimaryIndex;// 主键索引直接插到数据
    public LinkedHashMap<String, BPlusTree<Integer, Integer>> intTreeMap = new LinkedHashMap<>(); // 非主键索引回表查询
    public LinkedHashMap<String, BPlusTree<Integer, String>> charTreeMap = new LinkedHashMap<>();
    public LinkedHashMap<String, BPlusTree<Integer, Float>> floatTreeMap = new LinkedHashMap<>();

    public IndexManager(Schema schema, String dbName, String TableName) {
        for (int i = 0; i < schema.getColumns().size(); i++) {
            Column column = schema.getColumns().get(i);
            if (column.isPrimaryKey()) {
                PrimaryIndex = new BPlusTree<>(171, i);
                PrimaryIndex.setIndexName(TableName + "_primary_index");
                this.DataBaseName = dbName;
                this.TableName = TableName;
            }
        }
    }

    public boolean createIndex(IndexInfo indexInfo) throws RuntimeException {
        return true;
    }

    public ArrayList<Object> select(IndexInfo idx, Condition cond) throws IllegalArgumentException {
        return null;
    }

    public void delete(IndexInfo idx, String key) throws IllegalArgumentException {
        // 删除键值

    }

    public void insert(Row row) throws IllegalArgumentException {
        // 插入键值
        int primaryPos = this.PrimaryIndex.getIndexPos();
        PrimaryIndex.insert(row, (Integer) row.getFields().get(primaryPos).getValue());
        intTreeMap.forEach((s, IntegerBPlusTree) -> IntegerBPlusTree.insert((Integer) row.getFields().get(primaryPos).getValue(), (Integer) row.getFields().get(IntegerBPlusTree.getIndexPos()).getValue()));
        charTreeMap.forEach((s, StringBPlusTree) -> StringBPlusTree.insert((Integer) row.getFields().get(primaryPos).getValue(), (String) row.getFields().get(StringBPlusTree.getIndexPos()).getValue()));
        floatTreeMap.forEach((s, FloatBPlusTree) -> FloatBPlusTree.insert((Integer) row.getFields().get(primaryPos).getValue(), (Float) row.getFields().get(FloatBPlusTree.getIndexPos()).getValue()));
    }

    public void update(IndexInfo idx, String key, Object value) throws IllegalArgumentException {
        // 更新键值

    }


    public boolean dropIndex(IndexInfo idx) {
        // 删除索引
        // DROP INDEX index_name (Att_name) ON table_name
        return true;
    }

    private <K extends Comparable<? super K>> ArrayList<Object> satisfiesCond(BPlusTree<K, Integer> tree, String operator, K key) throws IllegalArgumentException {
        return null;
    }

    private int getStoreLength(String tableName) {
        return 0;
    }

    private Record getTuple(String tableName, int offset) {
        return null;
    }


    public void Update(int i,Row row) {
        PrimaryIndex.
        intTreeMap.forEach((s, IntegerBPlusTree) -> IntegerBPlusTree.insert((Integer) row.getFields().get(primaryPos).getValue(), (Integer) row.getFields().get(IntegerBPlusTree.getIndexPos()).getValue()));
        charTreeMap.forEach((s, StringBPlusTree) -> StringBPlusTree.insert((Integer) row.getFields().get(primaryPos).getValue(), (String) row.getFields().get(StringBPlusTree.getIndexPos()).getValue()));
        floatTreeMap.forEach((s, FloatBPlusTree) -> FloatBPlusTree.insert((Integer) row.getFields().get(primaryPos).getValue(), (Float) row.getFields().get(FloatBPlusTree.getIndexPos()).getValue()));

    }
}
