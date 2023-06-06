package org.liu.Executor;

import org.liu.CatologManager.CatalogManager;
import org.liu.Common.MyExceptionHandler;

import java.util.List;

public class Parser {

    public static void parse(List<String> strings) throws MyExceptionHandler {
        for (String string : strings) {
            parseSentence(string);
        }



    }

    public static void parseSentence(String s) throws MyExceptionHandler {
        String[] split = s.trim().toLowerCase().split(" ");
        if(split[0].equals("create")&&split[1].equals("database")){
            Executor.CreateDataBase(split[2]);
            return;
        }
        if(split[0].equals("use")&&split[1].equals("database")){
            Executor.UseBase(split[2]);
            return;
        }
    }
}
