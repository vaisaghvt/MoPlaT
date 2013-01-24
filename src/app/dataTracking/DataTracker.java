/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.dataTracking;

import org.jfree.chart.JFreeChart;
import sim.engine.Steppable;

/**
 *
 * Implement this interface if you want some new data to be collected.
 * 
 * @author vaisaghvt
 */
public interface DataTracker extends Steppable{
       
    public void storeToFile(); 
    
    public String trackerType();

    public boolean hasChart();
    
    public JFreeChart getChart();
}
