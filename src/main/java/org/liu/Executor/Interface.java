package org.liu.Executor;

import org.liu.Common.MyExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Interface {
    public static void StartMiniSQL() throws MyExceptionHandler {
        System.out.println("Welcome to MiniSQL 2.0");
        Scanner sc = new Scanner(System.in);
        List<String> strings = new ArrayList<>();
        while(true){
            System.out.print("MiniSQL> ");
            String s = sc.nextLine();
            strings.add(s);
            if(s.contains(";")){
                Parser.parse(strings);
                strings.clear();
            }
            if(s.equals("exit")){
                break;
            }
        }
    }
}
