/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clojureinterpreter;

import java.lang.*;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author 敲可爱
 */
public class ExecuteEngine {

    
    //本地变量表 实现作用域
    private HashMap<String,Base> localVariableSheet;
    //变量表列表
    private final List<HashMap<String, Base>> LocalVar = new ArrayList<>();
    {
        LocalVar.add(new HashMap<String,Base>());
        localVariableSheet = LocalVar.get(0);
    }
    
    //导入的java包 用来动态加载java类
    private final ArrayList<String> javaPackages = new ArrayList<>();
    //函数映射
    private final keywordSets keys = new keywordSets();
    private final HashSet<String> basicSpecialSet = new HashSet<>();
    
    
    {
        basicSpecialSet.add("def");
        basicSpecialSet.add("defn");
        basicSpecialSet.add("fn");
        basicSpecialSet.add("if");
        basicSpecialSet.add("let");
        basicSpecialSet.add("quote");
        basicSpecialSet.add("do");
    }
    
    {
        javaPackages.add("java.lang");
    }
    
    public Base executeSentence(String s) throws Exception {
        return referFunction(analysis(s));
    }
    
    public Base[] analysis(String s) throws Exception {
        //词法分析
        Deque<Integer> deque = new LinkedList<>();
        //最终答案
        Base[] thewords = null;
        //中间列表 最后生成数组
        List<Base> words = new LinkedList<>();
        //存储着每一个词法单元
        StringBuffer buffer = new StringBuffer();
        //state标志着是否有特殊情况，比如字符串 大括号等
        // "1 ( 2 [ 3 { 4 
        int state = 0;
        //嵌套层数
        int nestnumber = 0;
        HashMap<Integer, Character> stateMapping = new HashMap<Integer, Character>() {
            {
                this.put(2, ')');
                this.put(3, ']');
                this.put(4, '}');
            }
        };
        HashMap<Integer, Character> reverseStateMapping = new HashMap<Integer, Character>() {
            {
                this.put(2, '(');
                this.put(3, '[');
                this.put(4, '{');
            }
        };

        for (int i = 1; i < s.length() - 1; i++) {
            if (state != 0) {
                if (s.charAt(i) == '"' && s.charAt(i - 1) != '\\') {
                    buffer.append('"');
                    i++;
                    for (; !(s.charAt(i) == '"' && s.charAt(i - 1) != '\\'); i++) {
                        buffer.append(s.charAt(i));
                    }
                    buffer.append('"');
                    continue;
                }
                buffer.append(s.charAt(i));
                //出现对应元素 减少嵌套层数 当嵌套层数为0时 跳出循环
                if (s.charAt(i) == stateMapping.get(state)) {
                    nestnumber--;
                    if (nestnumber == 0) {
                        words.add(new Base(buffer.toString()));
                        buffer = new StringBuffer();
                        state = 0;
                        continue;
                    }
                }
                //出现相同的左元素 嵌套层数+1
                if (s.charAt(i) == reverseStateMapping.get(state)) {
                    nestnumber++;
                }
                continue;
            }

            //用空格隔断词法
            if (s.charAt(i) == ' ') {
                if (buffer.length() != 0) {
                    words.add(new Base(buffer.toString()));
                    buffer = new StringBuffer();
                }
                continue;
            }
            if (s.charAt(i) == '"' && s.charAt(i - 1) != '\\') {
                buffer.append('"');
                i++;
                for (; !(s.charAt(i) == '"' && s.charAt(i - 1) != '\\'); i++) {
                    buffer.append(s.charAt(i));
                }
                buffer.append('"');
                words.add(new Base(buffer.toString()));
                buffer = new StringBuffer();
                continue;
            }
            //是否隔断并返回存疑
            if (reverseStateMapping.containsValue(s.charAt(i))) {
                //此处存疑
                if (buffer.length() != 0&&buffer.charAt(buffer.length()-1)!='#') {
                    words.add(new Base(buffer.toString()));
                    buffer = new StringBuffer();
                }

                nestnumber = 1;
                buffer.append(s.charAt(i));
                switch (s.charAt(i)) {
                    case '(':
                        state = 2;
                        break;
                    case '[':
                        state = 3;
                        break;
                    case '{':
                        state = 4;
                        break;
                }
                continue;
            }
            buffer.append(s.charAt(i));
            if(i+1==s.length()-1){
                words.add(new Base(buffer.toString()));
                    buffer = new StringBuffer();
            }
        }
        //符号不匹配报错
        if (state != 0) {
            throw new RuntimeException(reverseStateMapping.get(state) + "符号不匹配");
        }
        //删去被#_注释的部分 须改动
        for(int i=0;i<words.size();i++)
           if(words.get(i).unwrap().toString().length()>=2&&words.get(i).unwrap().toString().substring(0, 2).equals("#_")){
               if(words.get(i).unwrap().toString().length()!=2)
                   words.remove(i);
               else{
                   words.remove(i);
                   words.remove(i);
               }
               i--;
           }
        
        //trim 使格式标准
        words.forEach((Base b) -> {
            b = new Base(b.unwrap().toString().trim());
        });
        thewords = new Base[words.size()];
        //可优化
        for (int i = 0; i < words.size(); i++) {
            thewords[i] = words.get(i);
        }
        System.out.println("analysis sentence:  " + s);
        return thewords;
    }

    //调用函数
    public Base referFunction(Base[] params) throws Exception {

        final String fun = params[0].unwrap().toString();
        System.out.println("refer function: " + fun);
        //System.out.println(keys.coreFunMapping.get(fun));
        //判断是否特殊形式(基础)
        if(basicSpecialSet.contains(fun)){
            return exeSpecial(fun,params);
        }
        Base[] otherParams = new Base[params.length - 1];
        for (int i = 1; i < params.length; i++) {
            otherParams[i - 1] = explainParam(params[i].unwrap().toString());
        }
        //判断是否调用java
        if (fun.charAt(0) == '.' || fun.equals("import") || fun.equals("new")) {
            return explainJava(fun, otherParams);
        }
        //查看是否本地变量
        if (localVariableSheet.containsKey(fun)){
            return localVariableSheet.get(fun);
        }
        //查找是否自定义函数或库函数
        if (keys.vars.containsKey(fun)) {
            return ((CloFunction) keys.vars.get(fun).unwrap()).invoke(otherParams);
        }
        //当函数是表达式的返回值时 
        if (fun.charAt(0) == '(') {
            return ((CloFunction) executeSentence(fun).unwrap()).invoke(otherParams);
        }
        //解析匿名函数
        if (fun.charAt(0) == '#'&&fun.length()>2&&fun.charAt(1)=='(') {
            return ((CloFunction) explainAnonymousFunc(fun.substring(1)).unwrap()).invoke(otherParams);
        }
        //查看是否关键字（:word）
        //查看是否绑定命令
        //查看是否定义函数
        if(fun.equals("defn"))
            return $defn(params);
        /*
            Base[] params = explainParameters(parameters);
            //先判断是否是特殊形式 、
            if(keywordSets.keywordsSet.contains(fun))
                return keywordSets.mapping.get(fun).invoke(explainParameters(parameters));
            //调用java方法及库
            if(fun.charAt(0)=='.'||fun.equals("new")||fun.equals("import"))
                return callJavaMethod(fun,parameters);
            if(fun.equals("new"))
                return generateJavaObject(params);
         */
        throw new RuntimeException("can't resolve! function not found!");
    }

    //解析匿名函数
    public Base explainAnonymousFunc(String func) {
        //lambada
        return new Base(new CloFunction() {
            @Override
            public Base invoke(Base[] parameters) throws Exception {
                //堆栈确认
                LocalVar.add(new HashMap<String,Base>());
                localVariableSheet = LocalVar.get(LocalVar.size()-1);
                
                //变量映射
                for(int i=0;i<parameters.length;i++)
                    localVariableSheet.put("$params"+(i+1), parameters[i]);
                //用长度为1的数组来解决本地变量无法修改的问题
                String[] fun = new String[1];
                fun[0] = func;
                //int arguNum = 0;
                //int[] arguNum = new int[1];
                //arguNum[0] = 0;
                boolean isString = false;
                for(int i = 2;i<fun[0].length()-1;i++){
                    if(isString&&!(fun[0].charAt(i)=='"'&&fun[0].charAt(i)!='\\'))
                        isString = false;
                    
                    //处理方式疑惑较大
                    else if(fun[0].charAt(i)=='%'){
                        int k = 1;
                        //处理 % 后有无数字的特殊情况
                        //类似于文本替换
                        for(;fun[0].charAt(i+k)>='0'&&fun[0].charAt(i+k)<='9';k++);
                        int arguIndex =  Integer.parseInt(fun[0].substring(i+1, i+k));
                        if(arguIndex > parameters.length)
                            throw new RuntimeException("arguments number wrong!");  
                        fun[0] = fun[0].substring(0, i)+" $params"+fun[0].substring(i+1);
                        i+="params".length()-"%".length();
                        //arguNum++;
                    }
                    else if(fun[0].charAt(i)=='"'&&fun[0].charAt(i)!='\\')
                        isString = true;
                }
                //if(parameters.length!=arguNum)
                //   throw new RuntimeException("arguments number wrong!");                
                
                Base ans = executeSentence(fun[0]);
                //返回
                LocalVar.remove(LocalVar.size()-1);
                localVariableSheet = LocalVar.get(LocalVar.size()-1);
                localVariableSheet = LocalVar.get(LocalVar.size()-1);
                return  ans;
            }
        });
    }

    public Base explainParam(String s) throws Exception {
        System.out.println("explainParam: "+s);
        //查看是否数字或字符串
        if (s.charAt(0) > '0' && s.charAt(0) < '9') {
            return new Base(Double.parseDouble(s));
        }
        if (s.charAt(0) == '"') {
            return new Base(s.substring(1, s.length() - 1));
        }
        //检查是否表达式
        if (s.charAt(0) == '(') {
            return executeSentence(s);
        }
        //检查是否特殊形式
        //检查是否本地变量
        if(localVariableSheet.containsKey(s))
            return localVariableSheet.get(s);
        //检查是否函数
        if (keys.vars.containsKey(s)) {
            return keys.vars.get(s);
        }
        //检查字面量
        //检查字符 待完善
        if (s.charAt(0) == '\\') {
            if (s.charAt(1) != 'u') {
                return new Base(s.charAt(1));
            }

        }
        //解析列表
        if(s.charAt(0) == '['){
            Base[] contents = analysis(s);
            CloList<Base> list = new CloList<>();
            for(Base base:contents)
                list.add(base);
            return new Base(list);
        }
        //解析字典
        //解析匿名函数
        if (s.charAt(0) == '#'&&s.length()>2&&s.charAt(1)=='(') {
            return new Base(((CloFunction) explainAnonymousFunc(s.substring(1)).unwrap()));
        }
        //
        /*
            case '.':;return explainJava(s,)
            case '"': return new Base(s.substring(1, s.length()-1));
            // #_ 放到之前处理
            case '#': {
                    if(s.charAt(1)=='"')
                        return new Base(Pattern.compile(s.substring(2, s.length()-1)));
                    //解析匿名函数
                    //if(s.charAt(1)=='(')
            }
            case '\\':{
                if(s.length()==2)
                    return new Base(s.charAt(1));
                //未完 解析unicode及其他可能
                //if(s.charAt(1)=='u')
            }
         */
        throw new Exception("can't resolve! no identifier found!");
        
    }

    //调用java
    public Base explainJava(String fun, Base[] params) throws Exception {
        switch (fun) {
            case "import":
            case ".":
            case "new":
        }
        return null;
    }
    
    //基础函数 特殊形式
    private Base exeSpecial(String fun, Base[] strParams) throws Exception{
        switch(fun){
            case "def": return $def(strParams);
            case "defn": return $defn(strParams);
            case "do" : return $do(strParams);
            case "if" : return $if(strParams);
            case "let": return $let(strParams);
            case "quote" : return $quote(strParams);
        }
        throw new Exception("Special form not found!");
    }
    
    //声明变量
    private Base $def(Base[] strParams)throws RuntimeException{
        if(LocalVar.size()>1)
            throw new RuntimeException("error! can't define in a function scope!");
        if(strParams.length!=3)
            throw new RuntimeException("arguments number error!");
        localVariableSheet.put(strParams[1].unwrap().toString(), strParams[2]);
        return strParams[2];
    };
    
    
    //定义函数
    private Base $defn(Base[] strParams){
        if(LocalVar.size()>1)
            throw new RuntimeException("error! can't define in a function scope!");
        Base func = new Base();
        //放入函数映射中
        keys.vars.put(strParams[1].unwrap().toString(), func);       
        func.set(new CloFunction() {
            @Override
            public Base invoke(Base[] parameters) throws Exception {
                //类似于帧栈
                LocalVar.add(new HashMap<String,Base>());
                localVariableSheet = LocalVar.get(LocalVar.size()-1);
                int i = 2;
                if(strParams[2].unwrap().toString().charAt(0)=='"')
                    i++;
                //逗号问题需要改进
                Base[] funcParams = analysis(strParams[i++].unwrap().toString());
                if(funcParams.length!=parameters.length)
                    throw new RuntimeException("function arguments number error!!");
                //绑定 将形参与实参映射在一起
                for(int k = 0;k<funcParams.length;k++)
                    localVariableSheet.put(funcParams[k].unwrap().toString(),parameters[k]);
                
                //执行过程
                for(;i<strParams.length-1;i++)
                    executeSentence(strParams[i].unwrap().toString());
                
                Base ans = executeSentence(strParams[i].unwrap().toString());
                //放入到函数映射中
                //keys.vars.put(strParams[1].unwrap().toString(), ans);
                LocalVar.remove(LocalVar.size()-1);
                localVariableSheet = LocalVar.get(LocalVar.size()-1);
                return ans;
            }
        });
        
        return func;
    };
    
    
    //执行多个表达式并返回最后一个
    private Base $do(Base[] strParams) throws Exception {
        int i;
        for(i = 1;i<strParams.length - 1;i++)
            if(strParams[i].unwrap().toString().charAt(0)!='(')
                throw new RuntimeException("arguments error! not sentence!");
            else
                explainParam(strParams[i].unwrap().toString());
        return explainParam(strParams[i].unwrap().toString());
    };
    
    //条件分支
    private Base $if(Base[] strParams) throws Exception {
        if(strParams.length!=4)
            throw new RuntimeException("arguments number error!");
        String unwrap = strParams[1].unwrap().toString();
        double judge = 0;
        boolean True = false;
        if(unwrap==null||unwrap.equals(""))
            return explainParam(strParams[3].unwrap().toString());
        try{
            double i = Double.parseDouble(explainParam(unwrap).unwrap().toString());
            judge = i;
        }
        catch(Exception e){
        }
        try{
            True = Boolean.valueOf(unwrap);
        }
        catch(Exception e){
        }
        if(judge!=0||True==true)
            return explainParam(strParams[2].unwrap().toString());
        return explainParam(strParams[3].unwrap().toString());
    };
    //本地绑定
    private Base $let(Base[] strParams){return null;};
    //阻止求值
    private Base $quote(Base[] strParams){return null;};
    
}
