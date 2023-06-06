package org.liu.RecordManager.Record;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Condition<T extends Comparable<? super T>> {
    private String name;
    private String operator;
    private T value;

    public boolean satisfy(Field field) {
        T cmpValue = this.value;
        return switch (this.operator) {
            case "=" -> field.compareTo(cmpValue) == 0;
            case "<>" -> field.compareTo(cmpValue) != 0;
            case ">" -> field.compareTo(cmpValue) > 0;
            case "<" -> field.compareTo(cmpValue) < 0;
            case ">=" -> field.compareTo(cmpValue) >= 0;
            case "<=" -> field.compareTo(cmpValue) <= 0;
            default ->  //undefined operator
                    false;
        };
    }
}

