package org.liu.BufferManager;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.apache.commons.lang.SerializationUtils;
import org.liu.Common.MyExceptionHandler;
import org.liu.IndexManager.BPlusTree;
import org.liu.IndexManager.IndexInfo;
import org.liu.IndexManager.IndexManager;
import org.liu.Page.Page;
import org.liu.RecordManager.Record.*;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

@Data
public class BufferManager { //数据库实例
    private final int MAXNUM = 50;  //maximum numbers
    //private final int EOF = -1; //none-exist num
    public Page[] buffer = new Page[MAXNUM];
    private String FileName = "";
    private String name = "";

    public BPlusTree CreateIndex(IndexInfo indexInfo) throws MyExceptionHandler {
        for (Page page : buffer) {
            if (page.isUsed() && page.getTablePageHeader().getTableName().equals(indexInfo.getTableName())) {
                indexInfo.setDataBaseName(name);
                BPlusTree bPlusTree = page.CreateIndex(indexInfo);
                String primaryColumnName = page.getTablePageHeader().getSchema().getColumns().get(page.getPrimaryPos()).getName();
                if (indexInfo.getColumnName().equals(primaryColumnName))
                    page.getIndexManager().setPrimaryIndex(bPlusTree);
                return bPlusTree;
            }
        }
        throw new MyExceptionHandler(0, "当前数据库不存在此表");
    }

    public void DropIndex(IndexInfo indexInfo) throws MyExceptionHandler {
        //drop index age_index
        for (Page page : buffer) {
            if (page.isUsed() && page.getTablePageHeader().getTableName().equals(indexInfo.getTableName())) {
                page.DropIndex(indexInfo);
                return;
            }
        }
        throw new MyExceptionHandler(0, "此表不存在");
    }

    public BufferManager(String name) throws MyExceptionHandler {
        this.name = name;
        this.FileName = "D:\\MiniSQL\\" + name + "\\";
        this.InitialBuffer();
    }

    public Page GetPageByName(String tableName) throws MyExceptionHandler {
        for (Page page : buffer) {
            if (page.isUsed() && page.getTablePageHeader().getTableName().equals(tableName))
                return page;
        }
        throw new MyExceptionHandler(0, "该表不存在");
    }

    public void ShowTables() { //展示数据库中的所有表
        System.out.println("Table_in_" + name);
        for (Page page : buffer) {
            if (page.isUsed())
                System.out.println(page.getTablePageHeader().getTableName());
        }
    }

    public void InitialBuffer() throws MyExceptionHandler {
        for (int i = 0; i < MAXNUM; i++) {
            buffer[i] = new Page();  //allocate new memory for blocks
            buffer[i].setLogicalId(i);
        }
        ConstructBufferManager();
    }

    public void DestructBufferManager() throws MyExceptionHandler, IOException { //持久化
        for (int i = 0; i < MAXNUM; i++) {
            WritePageToDisk(i);
        }
    }

    public boolean WritePageToDisk(int i) throws MyExceptionHandler, IOException {
        if (!buffer[i].isUsed())
            return false;
        Path filepath = Paths.get(FileName);
        if (!Files.exists(filepath)) {
            Files.createDirectory(filepath);
            filepath = Paths.get(FileName + "\\page");
            Files.createDirectory(filepath);
            filepath = Paths.get(FileName + "\\index");
            Files.createDirectory(filepath);
        }
        byte[] jsonString = JSON.toJSONBytes(buffer[i]);
        String filename = FileName + "\\" + buffer[i].getLogicalId() + ".txt";
        try (FileWriter fileWriter = new FileWriter(filename)) {
            for (byte b : jsonString) {
                fileWriter.write(b);
            }
            fileWriter.flush();
            return true;
        } catch (IOException e) {
            throw new MyExceptionHandler(0, "数据页写入磁盘时异常");
        }
    }

    public void ConstructBufferManager() throws MyExceptionHandler {
        for (int i = 0; i < MAXNUM; i++) {
            ReadPageFromDisk(i);
        }
    }

    public Page ReadPageFromDisk(int logicId) throws MyExceptionHandler {
        if (buffer[logicId].isUsed())
            return buffer[logicId];
        String filename = FileName + logicId + ".txt";
        Path filepath = Paths.get(filename);
        byte[] bytes;
        try {
            if (!Files.exists(filepath))
                return null;
            bytes = Files.readAllBytes(filepath);
            Object object = JSON.parseObject(bytes, (Type) Page.class);
            buffer[logicId] = (Page) object;
            return (Page) object;
        } catch (IOException e) {
            throw new MyExceptionHandler(0, "读取磁盘数据页时异常");
        }
    }

    public Page ReadPageFromDisk(String tableName) throws MyExceptionHandler {
        for (int i = 0; i < MAXNUM; i++) { //find the target block
            if (buffer[i].getTablePageHeader().getTableName().equals(tableName))
                return buffer[i];
        }
        String filename = FileName + tableName + ".txt";
        Path filepath = Paths.get(filename);
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(filepath);
            return JSON.parseObject(bytes, (Type) Page.class);
        } catch (IOException e) {
            throw new MyExceptionHandler(0, "读取磁盘数据页时异常");
        }
    }

    public int CreateTable(String tableName, Schema schema) throws MyExceptionHandler {
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i].isUsed() && buffer[i].getTablePageHeader().getTableName().equals(tableName)) {
                throw new MyExceptionHandler(0, "无法重复建表");
            }
            if (!buffer[i].isUsed()) {
                buffer[i].getTablePageHeader().setTableName(tableName);
                buffer[i].getTablePageHeader().setSchema(schema);
                buffer[i].setUsed(true);
                buffer[i].setDirty(true);
                for (int i1 = 0; i1 < schema.getColumns().size(); i1++) {
                    Column column = schema.getColumns().get(i1);
                    if (column.isPrimaryKey()) {
                        buffer[i].setPrimaryPos(i);
                    }
                }
                buffer[i].setIndexManager(new IndexManager(schema, this.name, tableName));
                System.out.println("创建表" + tableName + "成功！");
                return i;
            }
        }
        throw new MyExceptionHandler(0, "没有足够的数据页");
    }

    public long SelectTable(String tableName) throws MyExceptionHandler {
        long startTime = System.currentTimeMillis();
        Page page = GetPageByName(tableName);
        if (!page.isUsed()) {
            throw new MyExceptionHandler(0, "该表不存在");
        }
        System.out.println("----------------------------------------------");
        System.out.println(page.getTablePageHeader().getSchema());
        List<Row> rows = page.getRows();
        for (Row row : rows) {
            System.out.println(row);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("----------------------------------------------");
        return endTime - startTime;
    }

    public long SelectTable(String tableName, Condition condition) throws MyExceptionHandler {
        // select * from account where name = "name56789"
        long startTime = System.currentTimeMillis();
        Page page = GetPageByName(tableName);
        if (!page.isUsed()) {
            throw new MyExceptionHandler(0, "该表不存在");
        }
        if (!page.getIndexManager().ColumnNames.contains(condition.getName())) {
            System.out.println("----------------------------------------------");
            System.out.println(page.getTablePageHeader().getSchema());
            List<Row> rows = page.getRows();
            for (Row row : rows) {
                Field field1 = row.getFields().get(page.getFieldPos(condition.getName()));
                if (condition.satisfy(field1)) {
                    System.out.println(row);
                }
            }
            System.out.println("----------------------------------------------");
            long endTime = System.currentTimeMillis();
            return endTime - startTime;
        }
        startTime = System.currentTimeMillis();
        //查索引
        System.out.println("----------------------------------------------");
        System.out.println(page.getTablePageHeader().getSchema());
        List<Row> rows = page.getIndexManager().select(condition);
        for (Row row : rows) {
            System.out.println(row);
        }
        System.out.println("----------------------------------------------");
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public long SelectTable(String tableName, List<Condition> conditions, List<String> relations) throws MyExceptionHandler {
        Page page = GetPageByName(tableName);
        if (!page.isUsed()) {
            throw new MyExceptionHandler(0, "该表不存在");
        }
        boolean GoIndex = true; //是否可以走索引
        for (Condition condition : conditions) {
            GoIndex = GoIndex && page.getIndexManager().ColumnNames.contains(condition.getName());
        }
        if (!GoIndex) {
            long startTime = System.currentTimeMillis();
            System.out.println("----------------------------------------------");
            System.out.println(page.getTablePageHeader().getSchema());
            List<Row> rows = page.getRows();
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                Field field0 = row.getFields().get(page.getFieldPos(conditions.get(0).getName()));
                boolean flag = conditions.get(0).satisfy(field0);
                for (int i1 = 1; i1 < conditions.size(); i1++) {
                    field0 = row.getFields().get(page.getFieldPos(conditions.get(i1).getName()));
                    if (relations.get(i1 - 1).equals("and")) {
                        flag = flag && conditions.get(i1).satisfy(field0);
                    } else if (relations.get(i1 - 1).equals("or")) {
                        flag = flag || conditions.get(i1).satisfy(field0);
                    } else {
                        throw new MyExceptionHandler(0, "表达式错误");
                    }
                }
                if (flag) {
                    System.out.println(row);
                }
            }
            System.out.println("----------------------------------------------");
            long endTime = System.currentTimeMillis();
            return endTime - startTime;
        }
        long startTime = System.currentTimeMillis();
        List<Row> rows = page.getIndexManager().select(conditions, relations);//索引查询
        System.out.println("----------------------------------------------");
        for (Row row : rows) {
            System.out.println(row);
        }
        System.out.println("----------------------------------------------");
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public long SelectTable(String tableName, List<String> names, List<Condition> conditions, List<String> relations) throws MyExceptionHandler {
        long startTime = System.currentTimeMillis();
        Page page = GetPageByName(tableName);
        if (!page.isUsed()) {
            throw new MyExceptionHandler(0, "该表不存在");
        }
        System.out.println("----------------------------------------------");
        for (Column column : page.getTablePageHeader().getSchema().getColumns()) {
            if (names.contains(column.getName())) {
                System.out.print(column);
            }
        }
        System.out.println();
        List<Row> rows = page.getRows();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            Row res = new Row();
            Field field0 = row.getFields().get(page.getFieldPos(conditions.get(0).getName()));
            boolean flag = conditions.get(0).satisfy(field0);
            for (int i1 = 1; i1 < conditions.size(); i1++) {
                field0 = row.getFields().get(page.getFieldPos(conditions.get(i1).getName()));
                if (relations.get(i1 - 1).equals("and")) {
                    flag &= conditions.get(i1).satisfy(field0);
                } else if (relations.get(i1 - 1).equals("or")) {
                    flag |= conditions.get(i1).satisfy(field0);
                } else {
                    throw new MyExceptionHandler(0, "表达式错误");
                }
            }
            if (flag) {
                for (String s : names) {
                    int fieldPos = page.getFieldPos(s);
                    res.getFields().add(row.getFields().get(fieldPos));
                }
                System.out.println(res);
            }
        }
        System.out.println("----------------------------------------------");
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public long InsertIntoTable(String tableName, Row row) throws MyExceptionHandler {
        long startTime = System.currentTimeMillis();
        Page page = GetPageByName(tableName);
        if (!page.isUsed()) {
            throw new MyExceptionHandler(0, "该表不存在");
        }
        try {
            page.InsertRecord(row);
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
        //page.getIndexManager().PrimaryIndex.insert();
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public void DropTable(String tableName) throws MyExceptionHandler {
        Page page = GetPageByName(tableName);
        page.setUsed(false);
        System.out.println(page.getTablePageHeader().getTableName() + "表删除成功！");
    }

    public long UpdateTuple(String tableName, List<Condition> datas, Condition condition) throws MyExceptionHandler {
        long startTime = System.currentTimeMillis();
        Page page = GetPageByName(tableName);
        if (!page.isUsed()) {
            throw new MyExceptionHandler(0, "该表不存在");
        }
        List<Row> rows = page.getRows();
        IndexManager indexManager = page.getIndexManager();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            Row old = (Row) SerializationUtils.clone(row);
            Field field1 = row.getFields().get(page.getFieldPos(condition.getName()));
            if (condition.satisfy(field1)) {
                List<Field> fields = row.getFields();
                for (Condition data : datas) {
                    Field field = row.getFields().get(page.getFieldPos(data.getName()));
                    field.setValue(data.getValue().toString());
                }
                indexManager.Update(old, row);
            }
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public long UpdateTuple(String tableName, List<Condition> datas, List<Condition> conditions, List<String> relations) throws MyExceptionHandler {
        long startTime = System.currentTimeMillis();
        Page page = GetPageByName(tableName);
        if (!page.isUsed()) {
            throw new MyExceptionHandler(0, "该表不存在");
        }
        IndexManager indexManager = page.getIndexManager();
        List<Row> rows = page.getRows();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            Row old = (Row) SerializationUtils.clone(row);
            Row res = new Row();
            Field field0 = row.getFields().get(page.getFieldPos(conditions.get(0).getName()));
            boolean flag = conditions.get(0).satisfy(field0);
            for (int i1 = 1; i1 < conditions.size(); i1++) {
                field0 = row.getFields().get(page.getFieldPos(conditions.get(i1).getName()));
                if (relations.get(i1 - 1).equals("and")) {
                    String type = page.GetTypeByName(conditions.get(i1).getName());
                    if(field0.getType().equals(type)){
                        flag &= conditions.get(i1).satisfy(field0);
                    }

                } else if (relations.get(i1 - 1).equals("or")) {
                    String type = page.GetTypeByName(conditions.get(i1).getName());
                    if(field0.getType().equals(type)){
                        flag |= conditions.get(i1).satisfy(field0);
                    }
                } else {
                    throw new MyExceptionHandler(0, "表达式错误");
                }
            }
            if (flag) {
                List<Field> fields = row.getFields();
                for (Condition data : datas) {
                    Field field = row.getFields().get(page.getFieldPos(data.getName()));
                    field.setValue(data.getValue().toString());
                }
                indexManager.Update(old, row);
            }
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public void DeleteTuple(String tableName) throws MyExceptionHandler {
        Page page = GetPageByName(tableName);
        List<Row> rows = page.getRows();
        page.getIndexManager().Clear(rows);
        rows.clear();
        System.out.println(page.getTablePageHeader().getTableName() + "表所有数据删除成功！");
    }

    public void DeleteTuple(String tableName, Condition condition) throws MyExceptionHandler {
        Page page = GetPageByName(tableName);
        List<Row> rows = page.getRows();
        Iterator<Row> iterator = rows.iterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            Field field1 = row.getFields().get(page.getFieldPos(condition.getName()));
            if (condition.satisfy(field1)) {
                iterator.remove();
                page.getIndexManager().Delete(row);
            }
        }
    }


    public long DeleteTuple(String tableName, List<Condition> conditions, List<String> relations) throws MyExceptionHandler {
        long startTime = System.currentTimeMillis();
        Page page = GetPageByName(tableName);
        if (!page.isUsed()) {
            throw new MyExceptionHandler(0, "该表不存在");
        }
        List<Row> rows = page.getRows();
        Iterator<Row> iterator = rows.iterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            Field field0 = row.getFields().get(page.getFieldPos(conditions.get(0).getName()));
            boolean flag = conditions.get(0).satisfy(field0);
            for (int i1 = 1; i1 < conditions.size(); i1++) {
                field0 = row.getFields().get(page.getFieldPos(conditions.get(i1).getName()));
                if (relations.get(i1 - 1).equals("and")) {
                    flag &= conditions.get(i1).satisfy(field0);
                } else if (relations.get(i1 - 1).equals("or")) {
                    flag |= conditions.get(i1).satisfy(field0);
                } else {
                    throw new MyExceptionHandler(0, "表达式错误");
                }
            }
            if (flag) {
                iterator.remove();
                page.getIndexManager().Delete(row);
            }
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }


}
