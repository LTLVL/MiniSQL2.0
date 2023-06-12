package org.liu.Executor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.liu.CatologManager.CatalogManager;
import org.liu.Common.MyExceptionHandler;
import org.liu.IndexManager.IndexInfo;
import org.liu.Page.Page;
import org.liu.RecordManager.Record.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Executor {

    public static CatalogManager catalogManager;

    static {
        try {
            catalogManager = new CatalogManager();
        } catch (MyExceptionHandler e) {
            throw new RuntimeException(e);
        }
    }

    public static void CreateDataBase(String s) {
        // create database db01;
        String replace = s.replaceFirst(";", "");
        try {
            catalogManager.CreateDataBase(replace);
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
    }

    public static void UseBase(String s) {
        // use database db01;
        String replace = s.replaceFirst(";", "");
        try {
            catalogManager.UseDataBase(replace);
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
    }

    public static void ShowDataBases() {
        catalogManager.ShowDataBases();
    }

    public static void CreateTable(String s) {
        String table = s.trim().replaceFirst("\\(", "").split("\\s+")[2];
        String[] split = s.trim().toLowerCase().split("\\s+");

        Schema schema = new Schema();
        for (int i = 3; i < split.length; i++) {
            if (split[i].equals("primary") && i + 1 < split.length) {
                int leftPos = split[i + 1].indexOf("(");
                int rightPos = split[i + 1].indexOf(")");
                String primary = split[i + 1].substring(leftPos + 1, rightPos);
                for (Column column : schema.getColumns()) {
                    if (column.getName().equals(primary)) {
                        column.setPrimaryKey(true);
                        break;
                    }
                }
                break;
            }
            String name = split[i];
            String type;
            Column column;
            if (split[i + 1].contains(",")) {
                type = split[i + 1].replaceAll(",", "");
                column = new Column(name, type, false, false);

            } else {
                type = split[i + 1].replaceAll(",", "");
                column = new Column(name, type, false, false);
                if (i + 2 < split.length && split[i + 2].contains("unique")) {
                    column.setUnique(true);
                    i++;
                }
            }
            i++;
            if (column.getType().contains("char")) {
                column.setType("string");
            }
            schema.getColumns().add(column);
        }
        try {
            String primaryName = "";
            String type = "";
            for (Column column : schema.getColumns()) {
                if (column.isPrimaryKey()) {
                    primaryName = column.getName();
                    type = column.getType();
                }
            }
            catalogManager.indexInfos.add(new IndexInfo(table + "_primary_index", table, primaryName, type, catalogManager.bufferManager.getName()));
            catalogManager.bufferManager.CreateTable(table, schema);

        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
    }

    public static void InsertTuple(String s) {
        String[] split = s.trim().toLowerCase().split("\\s+");
        String table = split[2];
        if (!split[3].contains("values")) {
            System.out.println("SQL语句格式错误");
        }
        split[3] = split[3].replaceAll("values\\(", "").replaceAll(",", "");
        Page page = null;
        try {
            page = catalogManager.bufferManager.GetPageByName(table);
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
        assert page != null;
        List<Column> columns = page.getTablePageHeader().getSchema().getColumns();
        Row row = new Row();
        for (int i = 3; i < split.length; i++) {
            String value = split[i].replaceAll(",", "").replaceAll("\\)", "").replaceAll("\"", "").replaceAll(";", "");
            String type = columns.get(i - 3).getType();
            switch (type) {
                case "int" -> {
                    if (value.equals("null")) {
                        Field field = new Field(type, true, (Integer) null, i - 3);
                        break;
                    }
                    int parseInt = Integer.parseInt(value);
                    Field field = new Field(type, false, parseInt, i - 3);
                    row.getFields().add(field);
                }
                case "float" -> {
                    if (value.equals("null")) {
                        Field field = new Field(type, true, (Float) null, i - 3);
                        break;
                    }
                    Float parseInt = Float.parseFloat(value);
                    Field field = new Field(type, false, parseInt, i - 3);
                    row.getFields().add(field);
                }
                case "string" -> {
                    if (value.equals("null")) {
                        Field field = new Field(type, true, (String) null, i - 3);
                        break;
                    }
                    Field field = new Field(type, false, value, i - 3);
                    row.getFields().add(field);
                }
            }
        }
        try {
            catalogManager.bufferManager.InsertIntoTable(table, row);
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
    }

    public static void RunSqlFile(String s) {
        try (FileReader fr = new FileReader(new File(s.replaceAll("run", "").trim().toLowerCase().replaceAll(";", "")));
             BufferedReader br = new BufferedReader(fr)) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
                if (line.contains(";")) {
                    Parser.parseSentence(stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void SelectTuple(String s) {
        String[] split = s.replaceAll(";", "").trim().toLowerCase().split("\\s+");
        String table = split[3];
        if (split[1].equals("*")) {
            if (split.length == 4) {
                long l = 0;
                try {
                    l = catalogManager.bufferManager.SelectTable(table);
                } catch (MyExceptionHandler e) {
                    e.printStackTrace();
                }
                System.out.println("查询耗时：" + l + "ms");
                return;
            }
            if (split.length == 8) {
                Page page = null;
                try {
                    page = catalogManager.bufferManager.GetPageByName(table);
                } catch (MyExceptionHandler e) {
                    e.printStackTrace();
                }
                String name = split[5];
                String type = null;
                try {
                    assert page != null;
                    type = page.GetTypeByName(split[5]);
                } catch (MyExceptionHandler e) {
                    e.printStackTrace();
                }
                String operator = split[6];
                String value = split[7].replaceAll("\"", "");
                switch (Objects.requireNonNull(type)) {
                    case "int" -> {
                        Integer integer = Integer.parseInt(value);
                        Condition<Integer> condition = new Condition<>(name, operator, integer);
                        long l = 0;
                        try {
                            l = catalogManager.bufferManager.SelectTable(table, condition);
                        } catch (MyExceptionHandler e) {
                            e.printStackTrace();
                        }
                        System.out.println("查询耗时：" + l + "ms");
                    }
                    case "float" -> {
                        Float aFloat = Float.parseFloat(value);
                        Condition<Float> condition = new Condition<>(name, operator, aFloat);
                        long l = 0;
                        try {
                            l = catalogManager.bufferManager.SelectTable(table, condition);
                        } catch (MyExceptionHandler e) {
                            e.printStackTrace();
                        }
                        System.out.println("查询耗时：" + l + "ms");
                    }
                    case "string" -> {
                        Condition<String> condition = new Condition<>(name, operator, value);
                        long l = 0;
                        try {
                            l = catalogManager.bufferManager.SelectTable(table, condition);
                        } catch (MyExceptionHandler e) {
                            e.printStackTrace();
                        }
                        System.out.println("查询耗时：" + l + "ms");
                    }
                }
                return;
            }
            if (split.length > 8) {
                Page page = null;
                try {
                    page = catalogManager.bufferManager.GetPageByName(table);
                } catch (MyExceptionHandler e) {
                    e.printStackTrace();
                }
                List<Condition> conditions = new ArrayList<>();
                List<String> relations = new ArrayList<>();
                for (int i = 5; i < split.length; i += 4) {
                    String name = split[i];
                    String type = null;
                    try {
                        type = page.GetTypeByName(split[i]);
                    } catch (MyExceptionHandler e) {
                        e.printStackTrace();
                    }
                    String operator = split[i + 1];
                    String value = split[i + 2].replaceAll("\"", "");
                    switch (Objects.requireNonNull(type)) {
                        case "int" -> {
                            Integer integer = Integer.parseInt(value);
                            Condition<Integer> condition = new Condition<>(name, operator, integer);
                            conditions.add(condition);
                        }
                        case "float" -> {
                            Float aFloat = Float.parseFloat(value);
                            Condition<Float> condition = new Condition<>(name, operator, aFloat);
                            conditions.add(condition);
                        }
                        case "string" -> {
                            Condition<String> condition = new Condition<>(name, operator, value);
                            conditions.add(condition);
                        }
                    }
                    if (i + 3 < split.length) {
                        relations.add(split[i + 3]);
                    }
                }
                long l = 0;
                try {
                    l = catalogManager.bufferManager.SelectTable(table, conditions, relations);
                } catch (MyExceptionHandler e) {
                    e.printStackTrace();
                }
                System.out.println("查询耗时：" + l + "ms");
            }
        } else { //投影操作
            List<String> names = new ArrayList<>();
            int pos = 0;
            for (int i = 1; i < split.length; i++) {
                if (split[i].contains("from")) {
                    pos = i + 1;
                    break;
                }
                names.add(split[i].replaceAll(",", ""));
            }
            table = split[pos];
            Page page = null;
            try {
                page = catalogManager.bufferManager.GetPageByName(table);
            } catch (MyExceptionHandler e) {
                e.printStackTrace();
            }
            List<Condition> conditions = new ArrayList<>();
            List<String> relations = new ArrayList<>();
            for (int i = pos + 2; i < split.length; i += 4) {
                String name = split[i];
                String type = null;
                try {
                    assert page != null;
                    type = page.GetTypeByName(split[i]);
                } catch (MyExceptionHandler e) {
                    e.printStackTrace();
                }
                String operator = split[i + 1];
                String value = split[i + 2].replaceAll("\"", "");
                switch (type) {
                    case "int" -> {
                        Integer integer = Integer.parseInt(value);
                        Condition<Integer> condition = new Condition<>(name, operator, integer);
                        conditions.add(condition);
                    }
                    case "float" -> {
                        Float aFloat = Float.parseFloat(value);
                        Condition<Float> condition = new Condition<>(name, operator, aFloat);
                        conditions.add(condition);
                    }
                    case "string" -> {
                        Condition<String> condition = new Condition<>(name, operator, value);
                        conditions.add(condition);
                    }
                }
                if (i + 3 < split.length) {
                    relations.add(split[i + 3]);
                }
            }
            long l = 0;
            try {
                l = catalogManager.bufferManager.SelectTable(table, names, conditions, relations);
            } catch (MyExceptionHandler e) {
                e.printStackTrace();
            }
            System.out.println("查询耗时：" + l + "ms");
        }

    }

    public static void CreateIndex(String s) {
        // create index idx01 on account(name);
        String[] split = s.trim().toLowerCase().replace(";", "").split("\\s+");
        String IndexName = split[2];
        String[] strings = split[4].replaceAll("\\)", "").split("\\(");
        String table = strings[0];
        String field = strings[1];
        Page page = null;
        try {
            page = catalogManager.bufferManager.GetPageByName(table);
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
        String type = null;
        try {
            assert page != null;
            type = page.GetTypeByName(field);
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
        IndexInfo indexInfo = new IndexInfo(IndexName, strings[0], field, type, catalogManager.bufferManager.getName());
        try {
            catalogManager.CreateIndex(indexInfo);
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
    }

    public static void DeleteTuple(String s) {
        //delete from account where name = "name5678";
        String[] split = s.trim().toLowerCase().replaceAll(";", "").replaceAll("\"", "").split("\\s+");
        String table = split[2];
        if (split.length == 3) {
            try {
                catalogManager.bufferManager.DeleteTuple(table);
            } catch (MyExceptionHandler e) {
                e.printStackTrace();
            }
            return;
        }
        Page page = null;
        try {
            page = catalogManager.bufferManager.GetPageByName(table);
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
        String type = null;
        try {
            assert page != null;
            type = page.GetTypeByName(split[4]);
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
        Condition condition = null;
        switch (Objects.requireNonNull(type)) {
            case "int" -> condition = new Condition<>(split[4], split[5], Integer.parseInt(split[6]));
            case "float" -> condition = new Condition<>(split[4], split[5], Float.parseFloat(split[6]));
            case "string" -> condition = new Condition<>(split[4], split[5], split[6]);
        }
        try {
            catalogManager.bufferManager.DeleteTuple(table, condition);
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
    }

    public static void DropTable(String s) {
        //drop table account;
        String[] split = s.trim().toLowerCase().replaceAll(";", "").replaceAll("\"", "").split("\\s+");
        String table = split[2];
        try {
            catalogManager.bufferManager.DropTable(table);
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
    }

    public static void Exit() {
        try {
            byte[] jsonString = JSON.toJSONBytes(catalogManager);
            String filename = "D:\\MiniSQL\\data";
            try (FileWriter fileWriter = new FileWriter(filename)) {
                for (byte b : jsonString) {
                    fileWriter.write(b);
                }
                fileWriter.flush();
            } catch (IOException e) {
                throw new MyExceptionHandler(0, "数据页写入磁盘时异常");
            }
            jsonString = JSON.toJSONBytes(catalogManager.indexInfos);
            String Indexfilename = "D:\\MiniSQL\\index";
            try (FileWriter fileWriter = new FileWriter(Indexfilename)) {
                for (byte b : jsonString) {
                    fileWriter.write(b);
                }
                fileWriter.flush();
            } catch (IOException e) {
                throw new MyExceptionHandler(0, "索引写入磁盘时异常");
            }
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
    }

    public static void Start() {
        try {
            String filename = "D:\\MiniSQL\\data";
            String indexfilename = "D:\\MiniSQL\\index";
            byte[] bytes;
            Path filepath = Paths.get(filename);
            Path indexfilepath = Paths.get(indexfilename);
            if (!Files.exists(filepath) || !Files.exists(indexfilepath)) {
                return;
            }
            try {
                bytes = Files.readAllBytes(filepath);
                CatalogManager object = JSON.parseObject(bytes, CatalogManager.class);
                if (object != null)
                    catalogManager = object;
            } catch (IOException e) {
                throw new MyExceptionHandler(0, "读取磁盘数据页时异常");
            }

            try {
                String s = Files.readString(indexfilepath);
                List<IndexInfo> indexInfos = JSON.parseObject(s, new TypeReference<List<IndexInfo>>() {
                });
                if (indexInfos != null) {
                    catalogManager.indexInfos = indexInfos;
                    for (IndexInfo indexInfo : indexInfos) {
                        catalogManager.ResumeIndex(indexInfo);
                    }
                }
            } catch (IOException e) {
                throw new MyExceptionHandler(0, "读取磁盘数据页时异常");
            }
        } catch (MyExceptionHandler e) {
            e.printStackTrace();
        }
    }

    public static void DropIndex(String s) {
        String[] split = s.trim().toLowerCase().replaceAll(";", "").replaceAll("\"", "").split("\\s+");
        String index = split[2];
        try {
            catalogManager.DropIndex(index);
        } catch (MyExceptionHandler e) {
            e.fillInStackTrace();
        }

    }

    public static void UpdateTuple(String s) {
        //update from account set id = ?, balance = ? where name = "name56789";
        String[] split = s.trim().toLowerCase().replaceAll(",", "").replaceAll(";", "").replaceAll("\"", "").split("\\s+");
        String table = split[2];
        List<Condition> datas = new ArrayList<>();
        int pos = 4;
        for (int i = pos; i < split.length; i += 3) {
            if (split[i].equals("where")) {
                pos = i;
                break;
            }
            String name = split[i];
            String operator = split[i + 1];
            String value = split[i + 2];
            try {
                String type = catalogManager.GetTypeByName(table, name);
                switch (type) {
                    case "int" -> {
                        Condition<Integer> data = new Condition<>(name, operator, Integer.valueOf(value));
                        datas.add(data);
                    }
                    case "float" -> {
                        Condition<Float> data = new Condition<>(name, operator, Float.valueOf(value));
                        datas.add(data);
                    }
                    case "string" -> {
                        Condition<String> data = new Condition<>(name, operator, value);
                        datas.add(data);
                    }
                }
            } catch (MyExceptionHandler e) {
                e.fillInStackTrace();
            }
        }

        //update from account set id = ?, balance = ? where name = "name56789" and id > 1000000;
        List<Condition> conditions = new ArrayList<>();
        List<String> relations = new ArrayList<>();
        Page page = null;
        try {
            page = catalogManager.bufferManager.GetPageByName(table);
        } catch (MyExceptionHandler e) {
            e.fillInStackTrace();
        }
        for (int i = pos + 1; i < split.length; i += 4) {
            String name = split[i];
            String type = null;
            try {
                assert page != null;
                type = page.GetTypeByName(split[i]);
            } catch (MyExceptionHandler e) {
                e.printStackTrace();
            }
            String operator = split[i + 1];
            String value = split[i + 2].replaceAll("\"", "");
            switch (Objects.requireNonNull(type)) {
                case "int" -> {
                    Integer integer = Integer.parseInt(value);
                    Condition<Integer> condition = new Condition<>(name, operator, integer);
                    conditions.add(condition);
                }
                case "float" -> {
                    Float aFloat = Float.parseFloat(value);
                    Condition<Float> condition = new Condition<>(name, operator, aFloat);
                    conditions.add(condition);
                }
                case "string" -> {
                    Condition<String> condition = new Condition<>(name, operator, value);
                    conditions.add(condition);
                }
            }
            if (i + 3 < split.length) {
                relations.add(split[i + 3]);
            }
        }
        try {
            if(relations.size()==0){
                catalogManager.bufferManager.UpdateTuple(table,datas,conditions.get(0));
            }
            catalogManager.bufferManager.UpdateTuple(table, datas, conditions, relations);
        } catch (MyExceptionHandler e) {
            e.fillInStackTrace();
        }

    }
}
