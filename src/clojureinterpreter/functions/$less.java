/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clojureinterpreter.functions;

import clojureinterpreter.*;
import java.lang.reflect.*;
/**
 *
 * @author 敲可爱
 */
public class $less implements CloFunction{

    @Override
    public Base invoke(Base[] parameters) throws Exception {
        if(parameters.length != 2)
            throw new RuntimeException("arguments number wrong! wrong number : "+ parameters.length);     
        Object[] nums = new Object[2];
        nums[0] = parameters[0].unwrap();
        nums[1] = parameters[1].unwrap();
        return new Base((Double)nums[0] < (Double)nums[1]);
    }   
}