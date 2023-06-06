package org.liu;

import com.alibaba.fastjson2.JSON;
import org.liu.BufferManager.BufferManager;
import org.liu.CatologManager.CatalogManager;
import org.liu.Common.MyExceptionHandler;
import org.liu.Executor.Interface;
import org.liu.Executor.Parser;
import org.liu.IndexManager.IndexInfo;
import org.liu.Page.Page;
import org.liu.RecordManager.Record.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) throws MyExceptionHandler {
        Interface.StartMiniSQL();
    }
//    public static void main(String[] args) throws MyExceptionHandler, IOException {
//        CatalogManager catalogManager = new CatalogManager();
//        CatalogManager start = CatalogManager.Start();
//        catalogManager.CreateDataBase("db01");
//        catalogManager.UseDataBase("db01");
//        Schema schema = new Schema();
//        Column id = new Column("id", "int", true, true);
//        Column name = new Column("name", "char", true, false);
//        Column age = new Column("age", "int", true, false);
//        schema.getColumns().add(id);
//        schema.getColumns().add(name);
//        schema.getColumns().add(age);
//        catalogManager.CreateTable("user", schema);
//
//        //加索引 create index age_index on user(age);
//        String type = catalogManager.GetTypeByName("user","age");
//        IndexInfo indexInfo = new IndexInfo("age_index", "user", "age", type);
//        catalogManager.CreateIndex(indexInfo);
//
//        Row row = new Row();
//        Field ID = new Field("int", false, 0, 0);
//        Field NAME = new Field("string", false, "zhangsan", 1);
//        Field AGE = new Field("int", false, 11, 2);
//        row.getFields().add(ID);
//        row.getFields().add(NAME);
//        row.getFields().add(AGE);
//        Row row1 = new Row();
//        Field ID1 = new Field("int", false, 1, 0);
//        Field NAME1 = new Field("string", false, "lisi", 1);
//        Field AGE1 = new Field("int", false, 12, 2);
//        row1.getFields().add(ID1);
//        row1.getFields().add(NAME1);
//        row1.getFields().add(AGE1);
//        catalogManager.bufferManager.InsertIntoTable("user", row);
//        catalogManager.bufferManager.InsertIntoTable("user", row1);
//        Condition<Integer> condition = new Condition<>("id", ">=", 0);
//        Condition<Integer> condition2 = new Condition<>("age", "<", 12);
//        List<Condition> conditions = new ArrayList<>();
//        conditions.add(condition);
//        conditions.add(condition2);
//        catalogManager.bufferManager.SelectTable("user",condition2);
//        catalogManager.bufferManager.SelectTable("user", conditions, Collections.singletonList("and"));
//        catalogManager.bufferManager.DeleteTuple("user", conditions, Collections.singletonList("and"));
//        //System.out.println("删除后");
//        Condition<String> condition3 = new Condition<>("name", "=", "xiugai");
//        Condition<Integer> condition4 = new Condition<>("age", "=", 122);
//        List<Condition> datas = new ArrayList<>();
//        datas.add(condition3);
//        datas.add(condition4);
//        catalogManager.bufferManager.UpdateTuple("user",datas,conditions,Collections.singletonList("and"));
//        catalogManager.bufferManager.SelectTable("user");
//        List<String> strings = new ArrayList<>();
//        strings.add("id");
//        strings.add("name");
//        catalogManager.bufferManager.SelectTable("user", strings, conditions, Collections.singletonList("and"));
//        catalogManager.bufferManager.ShowTables();
//        catalogManager.bufferManager.DeleteTuple("user");
//        //catalogManager.bufferManager.DropTable("user");
//         //drop index age_index
//        //catalogManager.DropIndex("age_index");
//
//        catalogManager.Exit();
//    }
}