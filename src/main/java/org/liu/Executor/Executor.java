package org.liu.Executor;

import org.liu.CatologManager.CatalogManager;
import org.liu.Common.MyExceptionHandler;

import java.util.List;

public class Executor {
    public static CatalogManager catalogManager;
    static {
        try {
            catalogManager = new CatalogManager();
        } catch (MyExceptionHandler e) {
            throw new RuntimeException(e);
        }
    }

    public static void CreateDataBase(String s) throws MyExceptionHandler {
        // create database db01;
        String replace = s.replaceFirst(";","");
        catalogManager.CreateDataBase(replace);
    }

    public static void UseBase(String s) throws MyExceptionHandler {
        // use database db01;
        String replace = s.replaceFirst(";","");
        catalogManager.UseDataBase(replace);
    }
}
