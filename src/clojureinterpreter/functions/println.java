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
public class println implements CloFunction{

    @Override
    public Base invoke(Base[] parameters) throws Exception {
        for(Base b : parameters)
            System.out.println(b.unwrap());
        return new Base(this);
    }
    
}
