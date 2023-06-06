package org.liu.IndexManager;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndexInfo {
    private String indexName; //索引名
    private String tableName; //表名
    private String columnName; //字段
    private String type; //索引字段的类型
    private String DataBaseName; // 所属数据库名

    public IndexInfo(String indexName, String tableName, String columnName, String type) {
        this.indexName = indexName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.type = type;
    }
}
