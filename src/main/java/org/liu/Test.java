package org.liu;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.liu.BufferManager.BufferManager;
import org.liu.CatologManager.CatalogManager;
import org.liu.Common.MyExceptionHandler;
import org.liu.Page.Page;
import org.liu.RecordManager.Record.Field;
import org.liu.RecordManager.Record.Row;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) throws MyExceptionHandler {
        Row row = new Row();
        Field ID = new Field("int", false, 0, 0);
        Field NAME = new Field("string", false, "zhangsan", 1);
        Field AGE = new Field("int", false, 11, 2);
        row.getFields().add(ID);
        row.getFields().add(NAME);
        row.getFields().add(AGE);
        Row row1 = new Row();
        Field ID1 = new Field("int", false, 1, 0);
        Field NAME1 = new Field("string", false, "lisi", 1);
        Field AGE1 = new Field("int", false, 12, 2);
        row1.getFields().add(ID1);
        row1.getFields().add(NAME1);
        row1.getFields().add(AGE1);
        Page page = new Page();
        List<Row> rows1 = page.getRows();
        rows1.add(row);
        rows1.add(row1);
        BufferManager bufferManager = new BufferManager("db01");
        bufferManager.buffer[0] = page;
        CatalogManager catalogManager = new CatalogManager();
        List<BufferManager> bufferManagers = catalogManager.getBufferManagers();
        bufferManagers.add(bufferManager);
        WriteBM(catalogManager);
        ReadBM();
    }

    public static void Read() throws MyExceptionHandler {
        String filename = "D:\\MiniSQL\\data";
        Path filepath = Paths.get(filename);
        byte[] bytes;
        try {
            if (!Files.exists(filepath))
                return;
            String s = Files.readString(filepath);
            List<Row> rows = JSON.parseObject(s, new TypeReference<List<Row>>(){});
            System.out.println(rows);
            //List<Row> rows = JSON.parseArray(s, BufferManager.class);
        } catch (IOException e) {
            throw new MyExceptionHandler(0, "读取磁盘数据页时异常");
        }
    }


    public static void Write(List<Row> rows) throws MyExceptionHandler {
        byte[] jsonString = JSON.toJSONBytes(rows);
        String filename = "D:\\MiniSQL\\data";
        try (FileWriter fileWriter = new FileWriter(filename)) {
            for (byte b : jsonString) {
                fileWriter.write(b);
            }
            fileWriter.flush();
        } catch (IOException e) {
            throw new MyExceptionHandler(0, "数据页写入磁盘时异常");
        }
    }

    public static void ReadRow() throws MyExceptionHandler {
        String filename = "D:\\MiniSQL\\data";
        Path filepath = Paths.get(filename);
        byte[] bytes;
        try {
            if (!Files.exists(filepath))
                return;
            String s = Files.readString(filepath);
            Row row = JSON.parseObject(s, new TypeReference<Row>(){});
            System.out.println(row);
            //List<Row> rows = JSON.parseArray(s, BufferManager.class);
        } catch (IOException e) {
            throw new MyExceptionHandler(0, "读取磁盘数据页时异常");
        }
    }

    public static void WriteRow(Row row) throws MyExceptionHandler {
        byte[] jsonString = JSON.toJSONBytes(row);
        String filename = "D:\\MiniSQL\\data";
        try (FileWriter fileWriter = new FileWriter(filename)) {
            for (byte b : jsonString) {
                fileWriter.write(b);
            }
            fileWriter.flush();
        } catch (IOException e) {
            throw new MyExceptionHandler(0, "数据页写入磁盘时异常");
        }
    }

    public static void ReadPage() throws MyExceptionHandler {
        String filename = "D:\\MiniSQL\\data";
        Path filepath = Paths.get(filename);
        byte[] bytes;
        try {
            if (!Files.exists(filepath))
                return;
            String s = Files.readString(filepath);
            Page page = JSON.parseObject(s, new TypeReference<Page>(){});
            System.out.println(page);
            //List<Row> rows = JSON.parseArray(s, BufferManager.class);
        } catch (IOException e) {
            throw new MyExceptionHandler(0, "读取磁盘数据页时异常");
        }
    }

    public static void WritePage(Page page) throws MyExceptionHandler {
        byte[] jsonString = JSON.toJSONBytes(page);
        String filename = "D:\\MiniSQL\\data";
        try (FileWriter fileWriter = new FileWriter(filename)) {
            for (byte b : jsonString) {
                fileWriter.write(b);
            }
            fileWriter.flush();
        } catch (IOException e) {
            throw new MyExceptionHandler(0, "数据页写入磁盘时异常");
        }
    }

    public static void ReadBM() throws MyExceptionHandler {
        String filename = "D:\\MiniSQL\\data";
        Path filepath = Paths.get(filename);
        byte[] bytes;
        try {
            if (!Files.exists(filepath))
                return;
            String s = Files.readString(filepath);
            CatalogManager catalogManager = JSON.parseObject(s, new TypeReference<CatalogManager>(){});
            System.out.println(catalogManager);
            //List<Row> rows = JSON.parseArray(s, BufferManager.class);
        } catch (IOException e) {
            throw new MyExceptionHandler(0, "读取磁盘数据页时异常");
        }
    }

    public static void WriteBM(CatalogManager catalogManager) throws MyExceptionHandler {
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
    }

}
