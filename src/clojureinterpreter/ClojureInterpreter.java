/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clojureinterpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
/**
 *
 * @author 敲可爱
 */
public class ClojureInterpreter {
    
    ExecuteEngine engine = new ExecuteEngine();
    
    public static void main(String[] args) {
        try{
        ClojureInterpreter interpreter = new ClojureInterpreter();
        interpreter.Interprete("clojure.txt");
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void Interprete(String s){       
        int parenthese = 0;
        String cur;
        StringBuffer buffer = new StringBuffer();
        try{
            BufferedReader fin = new BufferedReader(new FileReader(s));
            boolean istext = false;
            while(fin.ready()){
                String str = fin.readLine();
                for(int i=0;i<str.length();i++){
                    if(istext){
                        if(str.charAt(i)=='"')
                            istext = false;
                        buffer.append(str.charAt(i));
                        continue;
                    }
                    if(str.charAt(i)==';')
                        break;
                    switch(str.charAt(i)){
                        case '"': 
                            istext = true; 
                            break;
                        case '(':
                            parenthese++;
                            break;
                        case ')':
                            parenthese--;
                            break;
                        case ',':
                            buffer.append(' ');
                            continue;
                    }
                    
                    buffer.append(str.charAt(i));
                    if(parenthese==0){
                        engine.executeSentence(buffer.toString().trim());
                        buffer = new StringBuffer();
                        continue;
                    }
                    if(parenthese<0)
                        throw new Exception("syntex error 括号不匹配 左括号少");
                }                
            }
            if(buffer.length()!=0)
                throw new Exception("syntex error 右括号少");
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

