/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clojureinterpreter;

import java.util.concurrent.Callable;

/**
 *
 * @author 敲可爱
 */
public interface CloFunction {   
    Base invoke (Base[] parameters) throws Exception;
}
