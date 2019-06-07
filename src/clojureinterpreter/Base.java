/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clojureinterpreter;

import java.lang.Number;
/**
 *
 * @author 敲可爱
 */
public class Base {
    
    private Object obj;
    
    public Base(){}
    public Base(Object ob){ obj = ob;}
    //Base quote(Base b); 
    public Object unwrap(){return obj;}
    public void set(Object ob){obj = ob;}
}
