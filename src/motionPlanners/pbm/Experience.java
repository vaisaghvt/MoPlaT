/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package motionPlanners.pbm;

import agent.RVOAgent;
import java.util.ArrayList;


/**
 *
 * @author hunan
 */
public class Experience extends ArrayList {
    private RVOAgent agent;
    protected ArrayList<ExperienceInstance> expBase;

    public Experience(WorkingMemory wm){
        agent = wm.getMyAgent();
        expBase = new ArrayList<ExperienceInstance>();
    }
    
    /*
     * add a new exp instance
     */
    public void addExpInstance(ExperienceInstance ei){
        expBase.add(ei);
    }
    
    /*
     * get all experience instances
     */
    public ArrayList<ExperienceInstance> getAllExpInstances(){
        return expBase;
    }
    
}
