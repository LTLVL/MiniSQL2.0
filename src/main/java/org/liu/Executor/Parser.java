package org.liu.Executor;

import org.liu.CatologManager.CatalogManager;
import org.liu.Common.MyExceptionHandler;

import java.io.FileNotFoundException;
import java.util.List;

public class Parser {

    public static void parse(String s) throws FileNotFoundException {
        parseSentence(s);
    }

    public static void parseSentence(String s) throws FileNotFoundException {
        String[] split = s.trim().toLowerCase().replaceAll(";", "").replaceAll(",", "").split("\\s+");
        if (split[0].equals("select")) {
            Executor.SelectTuple(s);
        }
        if (split[0].equals("run")) { // 运行sql文件
            Executor.RunSqlFile(s);
        }
        if (split[0].equals("show") && split[1].equals("databases")) {
            Executor.ShowDataBases();
            return;
        }
        if (split[0].equals("show") && split[1].equals("tables")) {
            Executor.ShowTables();
            return;
        }
        if (split[0].equals("create") && split[1].equals("database")) {
            Executor.CreateDataBase(split[2]);
            return;
        }
        if (split[0].equals("use") && split[1].equals("database")) {
            Executor.UseBase(split[2]);
            return;
        }
        if (split[0].equals("create") && split[1].equals("table")) {
            Executor.CreateTable(s);
            return;
        }
        if (split[0].equals("insert") && split[1].equals("into")) {
            Executor.InsertTuple(s);
        }
        if (split[0].equals("delete") && split[1].equals("from")) {
            Executor.DeleteTuple(s);
        }
        if (split[0].equals("update") && split[1].equals("from")) {
            Executor.UpdateTuple(s);
        }
        if (split[0].equals("create") && split[1].equals("index")) {
            Executor.CreateIndex(s);
        }
        if (split[0].equals("drop") && split[1].equals("table")) {
            Executor.DropTable(s);
        }
        if (split[0].equals("drop") && split[1].equals("index")) {
            Executor.DropIndex(s);
        }
        if (split[0].equals("exit")) {
            Executor.Exit();
        }

    }

    public static void Start() {
        Executor.Start();
    }
}
