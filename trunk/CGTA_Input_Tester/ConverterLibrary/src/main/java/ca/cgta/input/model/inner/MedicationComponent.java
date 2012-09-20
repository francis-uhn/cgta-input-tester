/*
 * Created on May 15, 2012
 * MedicationComponent.java
 * 
 */
package ca.cgta.input.model.inner;

import java.util.Date;


/**
 * 
 * @author <a href="mailto:neal.acharya@uhn.on.ca">Neal Acharya</a>
 * @version $Revision:$ updated on $Date:$ by $Author:$
 */
public class MedicationComponent {
    
    
//1   cGTA    R   1   ID      0166 RX Component Type   B
//2   cGTA    R   350 CE          Component Code  
//3   cGTA    R   20  NM          Component Amount    22
//4   cGTA    R   300 CE          Component Units MG^mg
    
    
    
    public String myComponentType;
    public Ce myComponentCode;
    public double myComponentAmount;    
    public Ce myComponentUnits;    
    
    
    

}
