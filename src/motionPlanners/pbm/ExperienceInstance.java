/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package motionPlanners.pbm;

import motionPlanners.pbm.WorkingMemory.STRATEGY;

/**
 *
 * @author hunan
 */
class ExperienceInstance {

    private STRATEGY empiricalStg;
    private STPattern prototypicalSTP;
    
    public ExperienceInstance(STRATEGY strategy, STPattern pattern){
        empiricalStg = strategy;
        prototypicalSTP = pattern;
    }
  
    public STRATEGY getEmpiricalStg() {
        return empiricalStg;
    }

    public STPattern getPrototypicalSTP() {
        return prototypicalSTP;
    }
    
}
