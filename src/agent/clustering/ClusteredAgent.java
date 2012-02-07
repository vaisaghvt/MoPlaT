/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent.clustering;

/**
 * 
 *
 * @author vaisagh
 * Created: Jan, 2010
 *
 * 
 *
 * Description:This class is a subclass of RVOAgent that enables the grouping of 
 * a number of similar/ nearby agents into a single clusteredAgent. This is 
 * essential for the idea of group collision avoidance
 *
 */
import utility.PrecisePoint;
import agent.RVOAgent;
import environment.RVOSpace;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.SysexMessage;
import javax.vecmath.Vector2d;
import sim.portrayal.LocationWrapper;

/**
 *
 * @author Vaisagh
 */
public class ClusteredAgent extends RVOAgent {

    private List<RVOAgent> agents;
    final double maxRadius;

    public ClusteredAgent(RVOSpace rvoSpace, RVOAgent agent, double maxRadius) {
        super(rvoSpace);
        radius = agent.getRadius();
        velocity = new PrecisePoint(agent.getVelocity().getX(), agent.getVelocity().getY());
        agents = new ArrayList<RVOAgent>();
        agents.add(agent);
        this.setCurrentPosition(agent.getCurrentPosition().getX(), agent.getCurrentPosition().getY());
        this.maxRadius = maxRadius;


    }

    @Override
    public String getName(LocationWrapper wrapper) {
        return "Clustered Agent" + id;
    }

    public List<RVOAgent> getAgents() {
        return agents;
    }

    public void setRadius(double radius) {
        if (radius > maxRadius) {
            System.out.println("illegal radius of " + radius + " when max is" + this.maxRadius + this.agents);
            System.exit(0);
        }
        this.radius = radius;
    }

    public double getMaxRadius() {
        return maxRadius;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClusteredAgent other = (ClusteredAgent) obj;
        if(this.agents.containsAll(other.getAgents()) 
                && other.getAgents().containsAll(this.getAgents()))
            return true;
        else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.agents != null ? this.agents.hashCode() : 0);
        return hash;
    }

 

    void simplyAddAgent(RVOAgent agent) {
        agents.add(agent);
    }

    void updateVelocityAndMass() {
        Vector2d tempVelocity = new Vector2d(0, 0);
        mass=0.0;
        for (RVOAgent tempAgent : agents) {
            mass+=tempAgent.getMass();
            tempVelocity.add(tempAgent.getVelocity());
        }
//        mass/=agents.size();
        tempVelocity.scale(1.0 / (double) agents.size());
        this.velocity= new PrecisePoint(tempVelocity.x, tempVelocity.y);
    }
}
