package org.liu.IndexManager;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.liu.Common.MyExceptionHandler;
import org.liu.RecordManager.Record.Record;
import org.liu.RecordManager.Record.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


@Data
@Slf4j
@NoArgsConstructor
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

    public List<Row> select(Condition cond) throws IllegalArgumentException {
        // select * from account where name = "name56789"
        String indexName = cond.getName();
        List<Row> res = new ArrayList<>();
        if (PrimaryIndex.getColumnName().equals(indexName)) {
            List<Row> query = new ArrayList<>();
            Integer first = PrimaryIndex.searchFirst();
            Integer last = PrimaryIndex.searchLast() + 1;
            switch (cond.getOperator()) {
                case "=" -> query = PrimaryIndex.query((Integer) cond.getValue());
                case "<>" -> {
                    query = PrimaryIndex.rangeQuery(first, (Integer) cond.getValue() - 1);
                    query.addAll(PrimaryIndex.rangeQuery((Integer) cond.getValue() + 1, last));
                }
                case ">" -> query.addAll(PrimaryIndex.rangeQuery((Integer) cond.getValue() + 1, last));
                case "<" -> query.addAll(PrimaryIndex.rangeQuery(first, (Integer) cond.getValue() - 1));
                case ">=" -> query.addAll(PrimaryIndex.rangeQuery((Integer) cond.getValue(), last));
                case "<=" -> query.addAll(PrimaryIndex.rangeQuery(first, (Integer) cond.getValue()));
            }
            return query;
        }
        intTreeMap.forEach((s, Tree) -> {
            if (Tree.getColumnName().equals(cond.getName())) {
                List<Integer> query = new ArrayList<>();
                Integer first = Tree.searchFirst();
                Integer last = Tree.searchLast() + 1;
                switch (cond.getOperator()) {
                    case "=" -> query = Tree.query((Integer) cond.getValue());
                    case "<>" -> {
                        query = Tree.rangeQuery(first, (Integer) cond.getValue() - 1);
                        query.addAll(Tree.rangeQuery((Integer) cond.getValue() + 1, last));
                    }
                    case ">" -> query.addAll(Tree.rangeQuery((Integer) cond.getValue() + 1, last));
                    case "<" -> query.addAll(Tree.rangeQuery(first, (Integer) cond.getValue() - 1));
                    case ">=" -> query.addAll(Tree.rangeQuery((Integer) cond.getValue(), last));
                    case "<=" -> query.addAll(Tree.rangeQuery(first, (Integer) cond.getValue()));
                }
                List<Row> rows = new ArrayList<>();
                for (Integer integer : query) {
                    List<Row> query1 = PrimaryIndex.query(integer);
                    rows.addAll(query1);
                }
                res.addAll(rows);
                try {
                    throw new MyExceptionHandler(0, "退出循环");
                } catch (MyExceptionHandler e) {

                }
            }
        });
        if (res.size() != 0) {
            return res;
        }
        charTreeMap.forEach((s, Tree) -> {
            if (Tree.getColumnName().equals(cond.getName())) {
                List<Integer> query = new ArrayList<>();
                String first = Tree.searchFirst();
                String last = Tree.searchLast();
                switch (cond.getOperator()) {
                    case "=" -> query = Tree.query((String) cond.getValue());
                    case "<>" -> {
                        query = Tree.rangeQuery(first, last);
                        query.removeAll(Tree.query((String) cond.getValue()));
                    }
                    case ">" -> {
                        query.addAll(Tree.rangeQuery((String) cond.getValue(), last));
                        query.removeAll(Tree.query((String) cond.getValue()));
                        query.addAll(Tree.query(last));
                    }
                    case "<" -> query.addAll(Tree.rangeQuery(first, (String) cond.getValue()));
                    case ">=" -> {
                        query.addAll(Tree.rangeQuery((String) cond.getValue(), last));
                        query.addAll(Tree.query(last));
                    }
                    case "<=" -> {
                        query.addAll(Tree.rangeQuery(first, (String) cond.getValue()));
                        query.addAll(Tree.query((String) cond.getValue()));
                    }
                }
                List<Row> rows = new ArrayList<>();
                for (Integer integer : query) {
                    List<Row> query1 = PrimaryIndex.query(integer);
                    rows.addAll(query1);
                }
                res.addAll(rows);
                try {
                    throw new MyExceptionHandler(0, "退出循环");
                } catch (MyExceptionHandler e) {

                }
            }
        });
        if (res.size() != 0) {
            return res;
        }
        floatTreeMap.forEach((s, Tree) -> {
            if (Tree.getColumnName().equals(cond.getName())) {
                List<Integer> query = new ArrayList<>();
                Float first = Tree.searchFirst();
                Float last = Tree.searchLast() + 1;
                switch (cond.getOperator()) {
                    case "=" -> query = Tree.query((Float) cond.getValue());
                    case "<>" -> {
                        query = Tree.rangeQuery(first, last);
                        query.removeAll(Tree.query((Float) cond.getValue()));
                    }
                    case ">" -> {
                        query.addAll(Tree.rangeQuery((Float) cond.getValue(), last));
                        query.removeAll(Tree.query((Float) cond.getValue()));
                        query.addAll(Tree.query(last));
                    }
                    case "<" -> query.addAll(Tree.rangeQuery(first, (Float) cond.getValue()));
                    case ">=" -> {
                        query.addAll(Tree.rangeQuery((Float) cond.getValue(), last));
                        query.addAll(Tree.query(last));
                    }
                    case "<=" -> {
                        query.addAll(Tree.rangeQuery(first, (Float) cond.getValue()));
                        query.addAll(Tree.query((Float) cond.getValue()));
                    }
                }
                List<Row> rows = new ArrayList<>();
                for (Integer integer : query) {
                    List<Row> query1 = PrimaryIndex.query(integer);
                    rows.addAll(query1);
                }
                res.addAll(rows);
            }
        });
        return res;
    }

    public List<Row> select(List<Condition> conditions, List<String> relations) throws IllegalArgumentException {
        //select * from account where id < 12508200 and name < "name00100"
        List<Row> res = new ArrayList<>(select(conditions.get(0)));
        for (int i = 1; i < conditions.size(); i++) {
            if(relations.get(i-1).equals("and")){
                List<Row> B = select(conditions.get(i));
                res = (List<Row>) CollectionUtils.intersection(B,res);
            }else if(relations.get(i-1).equals("or")){
                List<Row> B = select(conditions.get(i));
                res = (List<Row>) CollectionUtils.union(B,res);
            }
        }
        return res;
    }

    public void insert(Row row) throws IllegalArgumentException {
        // 插入键值
        int primaryPos = this.PrimaryIndex.getIndexPos();
        PrimaryIndex.insert((Integer) row.getFields().get(primaryPos).getValue(), row);
        intTreeMap.forEach((s, IntegerBPlusTree) -> IntegerBPlusTree.insert((Integer) row.getFields().get(IntegerBPlusTree.getIndexPos()).getValue(), (Integer) row.getFields().get(primaryPos).getValue()));
        charTreeMap.forEach((s, StringBPlusTree) -> StringBPlusTree.insert((String) row.getFields().get(StringBPlusTree.getIndexPos()).getValue(), (Integer) row.getFields().get(primaryPos).getValue()));
        floatTreeMap.forEach((s, FloatBPlusTree) -> FloatBPlusTree.insert((Float) row.getFields().get(FloatBPlusTree.getIndexPos()).getValue(), (Integer) row.getFields().get(primaryPos).getValue()));
    }

    public void Update(Row old, Row row) {
        PrimaryIndex.remove((Integer) old.getFields().get(getPrimaryIndex().IndexPos).getValue());
        PrimaryIndex.insert((Integer) row.getFields().get(getPrimaryIndex().IndexPos).getValue(), row);
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
