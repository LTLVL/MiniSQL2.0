package org.liu.CatologManager;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.annotation.JSONAutowired;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.liu.BufferManager.BufferManager;
import org.liu.Common.MyExceptionHandler;
import org.liu.IndexManager.IndexInfo;
import org.liu.Page.Page;
import org.liu.RecordManager.Record.Column;
import org.liu.RecordManager.Record.Schema;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@JSONAutowired
@Data
public class CatalogManager {
    public List<BufferManager> bufferManagers = new ArrayList<>(); // 数据库实例，所有与数据库相关的操作
    public BufferManager bufferManager; // 当前使用的数据库实例
    public List<IndexInfo> indexInfos = new ArrayList<>(); // 所有索引信息

    public CatalogManager() throws MyExceptionHandler {
    }


    @JSONField(serialize = false)
    public static CatalogManager Start() throws MyExceptionHandler {
        String filename = "D:\\MiniSQL\\data";
        Path filepath = Paths.get(filename);
        byte[] bytes;
        try {
            if (!Files.exists(filepath))
                return null;
            String s = Files.readString(filepath);
            return JSON.parseObject(s, new TypeReference<CatalogManager>() {
            });
            //this.bufferManagers = JSON.parseArray(s, BufferManager.class);
        } catch (IOException e) {
            throw new MyExceptionHandler(0, "读取磁盘数据页时异常");
        }
    }

    @JSONField(serialize = false)
    public void Exit() throws MyExceptionHandler {
        byte[] jsonString = JSON.toJSONBytes(this);
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


    public boolean UseDataBase(String name) throws MyExceptionHandler {
        for (BufferManager bufferManager : bufferManagers) {
            if (bufferManager.getName().equals(name)) {
                this.bufferManager = bufferManager;
                return true;
            }
        }
        throw new MyExceptionHandler(0, "此数据库不存在");
    }

    public boolean CreateDataBase(String DBName) throws MyExceptionHandler {
        for (BufferManager manager : bufferManagers) {
            if (manager.getName().equals(DBName)) {
                throw new MyExceptionHandler(0, "该数据库已存在");
            }
        }
        BufferManager manager = new BufferManager(DBName);
        manager.setName(DBName);
        bufferManagers.add(manager);
        return true;
    }

    //通过表名和字段名获取类型
    public String GetTypeByName(String table, String field) throws MyExceptionHandler {
        for (Page page : bufferManager.buffer) {
            if (page.isUsed() && page.getTablePageHeader().getTableName().equals(table)) {
                return page.GetTypeByName(field);
            }
        }
        throw new MyExceptionHandler(0, this.bufferManager.getName() + "中不存在此表");
    }

    public void CreateIndex(IndexInfo indexInfo) throws MyExceptionHandler {
        bufferManager.CreateIndex(indexInfo);
        this.indexInfos.add(indexInfo);
    }


    public void DropIndex(String Index) throws MyExceptionHandler {
        for (IndexInfo indexInfo : indexInfos) {
            if (indexInfo.getIndexName().equals(Index)
                    && indexInfo.getDataBaseName().equals(bufferManager.getName())) {
                bufferManager.DropIndex(indexInfo);
                indexInfos.remove(indexInfo);
                return;
            }
        }
        throw new MyExceptionHandler(0, "此索引不存在");
    }

    public void CreateTable(String tableName, Schema schema) throws MyExceptionHandler {
        bufferManager.CreateTable(tableName, schema);
        String PrimaryColumn = "";
        String PrimaryType = "";
        for (Column column : schema.getColumns()) {
            if (column.isPrimaryKey()) {
                PrimaryColumn = column.getName();
                PrimaryType = column.getType();
            }
        }
        indexInfos.add(new IndexInfo(
                tableName + "_primary_index", tableName,
                PrimaryColumn, PrimaryType, bufferManager.getName()));
    }

    public void DropTable(String tableName) throws MyExceptionHandler {
        bufferManager.DropTable(tableName);
        indexInfos.removeIf(indexInfo -> indexInfo.getTableName().equals(tableName));
    }
}
