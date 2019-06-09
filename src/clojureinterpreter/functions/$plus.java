/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clojureinterpreter.functions;
import clojureinterpreter.*;

/**
 *
 * @author 敲可爱
 */
public class $plus implements CloFunction{
    @Override
    public Base invoke(Base[] params) throws Exception{
        //System.out.println("$plus!");
        //System.out.println(params.length);
        if(params==null)
            throw new IllegalArgumentException("no argument");
        if(params.length==1)
            return new Base(params[0].unwrap());
        //先拆包转化成string 再转化为double 可能有隐患
        Double ans = 0.0;
        for(int i=0;i<params.length;i++){
            ans += Double.parseDouble(params[i].unwrap().toString());
            //System.out.println(ans);
        }
        return new Base(ans);
    }
    
}
