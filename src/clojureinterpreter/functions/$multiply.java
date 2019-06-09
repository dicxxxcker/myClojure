/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clojureinterpreter.functions;
import clojureinterpreter.Base;
import clojureinterpreter.CloFunction;
/**
 *
 * @author 敲可爱
 */
//仍有问题 比如列表乘法
public class $multiply implements CloFunction{
   @Override
    public Base invoke(Base[] params) throws Exception{
        if(params==null)
            throw new IllegalArgumentException("no argument!");
        if(params.length==1)
            return new Base(params[0].unwrap());
        //先拆包转化成string 再转化为double 可能有隐患
        Double ans = Double.parseDouble(params[0].unwrap().toString());
        for(int i=1;i<params.length;i++)
            ans *= Double.parseDouble(params[i].unwrap().toString());
        return new Base(ans);
    }
}
