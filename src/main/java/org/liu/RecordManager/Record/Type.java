package org.liu.RecordManager.Record;

import java.util.HashMap;

public class Type {
    public static final HashMap<String, Integer> TYPE_MAP = new HashMap<String, Integer>() {{
        put(Type.INT, 4);
        put(Type.FLOAT, 4);
        put(Type.CHAR, 1); //单个字符占用1个byte，字母
    }};
    public static final String INT = "int";
    //public static final String BIGINT = "bigint";
    public static final String FLOAT = "float";
    //public static final String DOUBLE = "double";
    //public static final String DECIMAL = "decimal";
    public static final String CHAR = "char";
//    public static final String VARCHAR = "varchar";
    //public static final String TIMESTAMP = "timestamp";
}
