/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clojureinterpreter.functions;

import clojureinterpreter.Base;

/**
 *
 * @author 敲可爱
 */
public class $not implements clojureinterpreter.CloFunction{

    @Override
    public Base invoke(Base[] parameters) throws Exception {
        if(parameters==null||parameters.length>1)
            throw new RuntimeException("arguments number wrong!");
        if(parameters[0].unwrap()==null||(Boolean)parameters[0].unwrap()==false)
            return new Base(true);
        return new Base(false);
    }   
}
