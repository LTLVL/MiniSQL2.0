package org.liu.Executor;

import org.liu.Common.MyExceptionHandler;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Interface {
    public static void StartMiniSQL() throws MyExceptionHandler, FileNotFoundException {
        System.out.println("Welcome to MiniSQL 2.0");
        Scanner sc = new Scanner(System.in);
        StringBuilder s = new StringBuilder();
        boolean flag = false;
        System.out.print("MiniSQL> ");
        Parser.Start();
        do {
            s.append(" ").append(sc.nextLine());
            if (s.toString().contains(";")) {
                Parser.parse(s.toString());
                s = new StringBuilder();
                System.out.print("MiniSQL> ");
            }
            if(s.toString().contains("exit")){
                Parser.parse(s.toString());
                break;
            }
        } while (true);

    }
}
