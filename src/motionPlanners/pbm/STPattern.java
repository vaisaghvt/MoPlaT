/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package motionPlanners.pbm;

import agent.RVOAgent;
import java.util.ArrayList;
import javax.vecmath.Point2d;
import motionPlanners.pbm.WorkingMemory;
import motionPlanners.pbm.WorkingMemory.STRATEGY;

/**
 *
 * @author hunan
 */
class STPattern {


    private int[][][] pattern; //[no of predicted frames]([no of attenuation levels for attention][number of visionary section])
    
    /*
     * constructor
     */
    public STPattern(int pf, int a_row, int a_column) {
        this.pattern = new int [pf+1][a_row][a_column];
    }

    public int[][][] getPattern() {
        return pattern;
    }

    public void setPattern(int[][][] pattern) {
        this.pattern = pattern;
    }
    
    public void setValue(int pfindex, int rowindex, int columnindex, int value){
        pattern[pfindex][rowindex][columnindex] = value;
    }
    
    public int getValue(int pfindex, int rowindex, int columnindex){
        return pattern[pfindex][rowindex][columnindex];
    }
    
    public void setSlice(int pf, int[][] slice){
        for(int i=0;i<pattern.length;i++){
            if(i== pf){
                for(int j=0; j<3; j++) //row = 3
                    System.arraycopy(slice[j], 0, pattern[i][j], 0, 11);
            }
        }
    }


}