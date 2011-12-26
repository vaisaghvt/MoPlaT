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
    
    /*
     * given the id of an agent, return its column index at the FIRST ROW of stp_id, if not exist in the row, return -1
     */
    public int returnColumnIndex(int ptId, boolean fromLeft, int frame) {
        int cIndex=-1;
        ArrayList<Integer> cIndeces = new ArrayList<Integer>();
        for(int i=1;i< this.getPattern()[0][0].length-1;i++){
            if(this.getValue(frame, 0, i)== ptId){
                cIndeces.add(0,i);
            }
        }
        if(!cIndeces.isEmpty()){
            if(fromLeft){
                //return the minimal column index in the list
                cIndex = cIndeces.get(cIndeces.size()-1);
            }else{
                //return the maximum column index in the list
                cIndex = cIndeces.get(0);
            }
        }
        return cIndex;
    }
    
}