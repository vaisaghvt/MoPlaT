/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.dataTracking;

import sim.engine.Steppable;

/**
 *
 * @author vaisaghvt
 */
public interface DataTracker extends Steppable{
    
    public void storeToFile(); 
}
