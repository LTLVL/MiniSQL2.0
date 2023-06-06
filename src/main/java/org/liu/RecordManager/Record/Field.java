package org.liu.RecordManager.Record;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data //对应于一条记录中某一个字段的数据信息，如存储数据的数据类型，是否是空，存储数据的值等等；
public class Field {
    private String type;
    private boolean isNull;
    private Integer intValue = -1;
    private Float floatValue = -1F;
    private String stringValue = "";
    //private T value;
    private int pos; //该信息所在行的id

    public Field(String type, boolean isNull, Integer intValue, int pos) {
        this.type = type;
        this.isNull = isNull;
        this.intValue = intValue;
        this.pos = pos;
    }

    public Field(String type, boolean isNull, Float floatValue, int pos) {
        this.type = type;
        this.isNull = isNull;
        this.floatValue = floatValue;
        this.pos = pos;
    }

    public Field(String type, boolean isNull, String stringValue, int pos) {
        this.type = type;
        this.isNull = isNull;
        this.stringValue = stringValue;
        this.pos = pos;
    }

    //    @Override
//    public int compareTo(T t) {
//        return value.compareTo(t);
//    }

    @Override
    public String toString() {
        switch (type) {
            case "int" -> {
                return intValue + "\t";
            }
            case "float" -> {
                return floatValue + "\t";
            }
            case "string" -> {
                return stringValue + "\t";
            }
            default -> {
                return null;
            }
        }
    }

    public <T extends Comparable<? super T>> int compareTo(Object cmpValue) {
        switch (type) {
            case "int" -> {
                return intValue.compareTo((Integer) cmpValue);
            }
            case "float" -> {
                return floatValue.compareTo((Float) cmpValue);
            }
            case "string" -> {
                return stringValue.compareTo((String) cmpValue);
            }
            default -> {
                return 0;
            }
        }
    }

    public Object getValue() {
        switch (type) {
            case "int" -> {
                return intValue;
            }
            case "float" -> {
                return floatValue;
            }
            case "string" -> {
                return stringValue;
            }
            default -> {
                return 0;
            }
        }
    }

    public void setValue(Comparable value) {
        switch (type) {
            case "int" -> {
                setIntValue((Integer) value);
            }
            case "float" -> {
                setFloatValue((Float) value);
            }
            case "string" -> {
                setStringValue((String) value);
            }
        }
    }
}
