/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clojureinterpreter;

import java.util.HashMap;
import java.util.HashSet;
import clojureinterpreter.functions.*;

/**
 *
 * @author 敲可爱
 */
public class keywordSets {
    //static final HashSet<String> keywordsSet = new HashSet<>();
    //特殊形式 即clojure中的特殊形式 是所有其他运算的基础 其他即使如'+'都不是基础的
    
    final HashMap<String,CloFunction> coreFunMapping = new HashMap<>();
    { 
        //内置函数
        coreFunMapping.put("+",new $plus());
        coreFunMapping.put("-",new $minus());
        coreFunMapping.put("*",new $multiply());
        coreFunMapping.put("/",new $divide());
        coreFunMapping.put("not", new $not());
        coreFunMapping.put("println", new println());
        coreFunMapping.put(">",new $bigger());
    }
    
    final HashMap<String,Base> vars = new HashMap<>();
    {
        //将库函数添加到变量表中
        for(String s: coreFunMapping.keySet())
            vars.put(s, new Base(coreFunMapping.get(s)));       
    }
}
