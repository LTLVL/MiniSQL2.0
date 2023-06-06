package org.liu.IndexManager;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.liu.RecordManager.Record.Record;
import org.liu.RecordManager.Record.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;


@Data
@Slf4j
public class IndexManager {
    // 索引名与对应的B+树集合
    private String DataBaseName;
    private String TableName;
    public List<String> ColumnNames = new ArrayList<>();
    public BPlusTree<Integer, Row> PrimaryIndex;// 主键索引直接插到数据
    public LinkedHashMap<String, BPlusTree<Integer, Integer>> intTreeMap = new LinkedHashMap<>(); // 非主键索引回表查询
    public LinkedHashMap<String, BPlusTree<String, Integer>> charTreeMap = new LinkedHashMap<>();
    public LinkedHashMap<String, BPlusTree<Float, Integer>> floatTreeMap = new LinkedHashMap<>();

    public IndexManager(Schema schema, String dbName, String TableName) {
        for (int i = 0; i < schema.getColumns().size(); i++) {
            Column column = schema.getColumns().get(i);
            if (column.isPrimaryKey()) {
                PrimaryIndex = new BPlusTree<>(171, i);
                PrimaryIndex.setIndexName(TableName + "_primary_index");
                PrimaryIndex.setColumnName(column.getName());
                ColumnNames.add(column.getName());
                this.DataBaseName = dbName;
                this.TableName = TableName;
            }
        }
    }

    public boolean createIndex(IndexInfo indexInfo) throws RuntimeException {
        return true;
    }

    public void select(Condition cond) throws IllegalArgumentException {
        // select * from account where name = "name56789"
        String indexName = cond.getName();
        if(PrimaryIndex.getColumnName().equals(indexName)){
            List<Row> query = PrimaryIndex.query((Integer) cond.getValue());
            for (Row row : query) {
                System.out.println(row);
            }
        }
        intTreeMap.forEach((s, Tree) -> {
            if(Tree.getColumnName().equals(cond.getName())){
                List<Integer> list = Tree.query((Integer) cond.getValue());
                List<Row> query = new ArrayList<>();
                for (Integer integer : list) {
                    List<Row> query1 = PrimaryIndex.query(integer);
                    query.addAll(query1);
                }
                for (Row row : query) {
                    System.out.println(row);
                }
            }
        });
        charTreeMap.forEach((s, Tree) -> {
            if(Tree.getColumnName().equals(cond.getName())){
                List<Integer> list = Tree.query((String) cond.getValue());
                List<Row> query = new ArrayList<>();
                for (Integer integer : list) {
                    List<Row> query1 = PrimaryIndex.query(integer);
                    query.addAll(query1);
                }
                for (Row row : query) {
                    System.out.println(row);
                }
            }
        });
        floatTreeMap.forEach((s, Tree) -> {
            if(Tree.getColumnName().equals(cond.getName())){
                List<Integer> list = Tree.query((Float) cond.getValue());
                List<Row> query = new ArrayList<>();
                for (Integer integer : list) {
                    List<Row> query1 = PrimaryIndex.query(integer);
                    query.addAll(query1);
                }
                for (Row row : query) {
                    System.out.println(row);
                }
            }
        });
    }

    public void insert(Row row) throws IllegalArgumentException {
        // 插入键值
        int primaryPos = this.PrimaryIndex.getIndexPos();
        PrimaryIndex.insert((Integer) row.getFields().get(primaryPos).getValue(), row);
        intTreeMap.forEach((s, IntegerBPlusTree) -> IntegerBPlusTree.insert((Integer) row.getFields().get(IntegerBPlusTree.getIndexPos()).getValue(), (Integer) row.getFields().get(primaryPos).getValue()));
        charTreeMap.forEach((s, StringBPlusTree) -> StringBPlusTree.insert((String) row.getFields().get(StringBPlusTree.getIndexPos()).getValue(), (Integer) row.getFields().get(primaryPos).getValue()));
        floatTreeMap.forEach((s, FloatBPlusTree) -> FloatBPlusTree.insert((Float) row.getFields().get(FloatBPlusTree.getIndexPos()).getValue(), (Integer) row.getFields().get(primaryPos).getValue()));
    }



    public boolean dropIndex(String name) {
        // 删除索引
        // DROP INDEX index_name (Att_name) ON table_name

        return true;
    }

//    private <K extends Comparable<? super K>> ArrayList<Object> satisfiesCond(BPlusTree<K, Integer> tree, String operator, K key) throws IllegalArgumentException {
//        return null;
//    }

    private int getStoreLength(String tableName) {
        return 0;
    }

    private Record getTuple(String tableName, int offset) {
        return null;
    }


    public void Update(int i, Row old, Row row) {
        PrimaryIndex.remove((Integer) old.getFields().get(getPrimaryIndex().IndexPos).getValue());
        PrimaryIndex.insert((Integer) row.getFields().get(getPrimaryIndex().IndexPos).getValue(),row);
        intTreeMap.forEach((s, Tree) -> {
            Tree.remove((Integer) old.getFields().get(Tree.getIndexPos()).getValue());
            Tree.insert((Integer) row.getFields().get(Tree.getIndexPos()).getValue(), (Integer) row.getFields().get(getPrimaryIndex().IndexPos).getValue());
        });
        charTreeMap.forEach((s, Tree) -> {
            Tree.remove((String) old.getFields().get(Tree.getIndexPos()).getValue());
            Tree.insert((String) row.getFields().get(Tree.getIndexPos()).getValue(), (Integer) row.getFields().get(getPrimaryIndex().IndexPos).getValue());
        });
        floatTreeMap.forEach((s, Tree) -> {
            Tree.remove((Float) old.getFields().get(Tree.getIndexPos()).getValue());
            Tree.insert((Float) row.getFields().get(Tree.getIndexPos()).getValue(), (Integer) row.getFields().get(getPrimaryIndex().IndexPos).getValue());
        });
    }


    public void Clear(List<Row> rows) {
        for (Row row : rows) {
            Delete(row);
        }
    }

    public void Delete(Row row) {
        PrimaryIndex.remove((Integer) row.getFields().get(PrimaryIndex.IndexPos).getValue());
        intTreeMap.forEach((s, Tree) -> Tree.remove((Integer) row.getFields().get(Tree.IndexPos).getValue()));
        charTreeMap.forEach((s, Tree) -> Tree.remove((String) row.getFields().get(Tree.IndexPos).getValue()));
        floatTreeMap.forEach((s, Tree) -> Tree.remove((Float) row.getFields().get(Tree.IndexPos).getValue()));
    }

    public boolean contains(String name) {
        return ColumnNames.contains(name);
    }
}
