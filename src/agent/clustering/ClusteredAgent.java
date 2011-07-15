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

import agent.RVOAgent;
import environment.RVOSpace;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import sim.portrayal.LocationWrapper;

/**
 *
 * @author Vaisagh
 */
public class ClusteredAgent extends RVOAgent {

    public static double MAXCLUSTERRADIUS = 0.8f;
    protected List<RVOAgent> agents;
    protected double maxRadius = MAXCLUSTERRADIUS;
  
    public ClusteredAgent(RVOSpace rvoSpace, RVOAgent agent) {
        super(rvoSpace);
        radius = agent.getRadius();
        velocity = new Vector2d(agent.getVelocity());
        setCentre(new Point2d(agent.getCurrentPosition()));
        agents = new ArrayList<RVOAgent>();
        agents.add(agent);

    }

    @Override
    public String getName(LocationWrapper wrapper) {
        return "Clustered Agent"+id;
    }



    public List<RVOAgent> getAgents() {
        return agents;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * These two methods are the same value as currentPosition, it is just that 
     * Centre is more intuitive.. hence using a seperate getter and setter that 
     * does the same thing.
     * @return location of center of the cluster.
     */
    public Point2d getCentre() {
        return currentPosition;
    }

    public final void setCentre(Point2d centre) {
        this.currentPosition = new Point2d(centre);
    }
    
    public double getMaxRadius() {
        return maxRadius;
    }

    public void setMaxRadius(double maxRadius) {
        this.maxRadius = maxRadius;
    }

    /**
     * This function tries to add the agent and returns a false if it isn't added
     * to the current cluster.
     *
     *
     * @param agent is the agent added to be added to it
     * @return boolean indicating whether agent was added sucessfully or not
     */
    public boolean addAgent(RVOAgent agent) {

        //If the agent velocity is in opposite direction.Then don't add it to cluster
//        if (Geometry.sameDirection(agent.getVelocity(), this.getVelocity()) < 0.0) {
//            if (Math.abs(agent.getVelocity().length() - this.getVelocity().length()) < 0.1) {
//                return false;
//            }
//        }


        Vector2d differenceVelocity = new Vector2d(agent.getVelocity());
        differenceVelocity.sub(this.getVelocity());
        if (Math.abs(differenceVelocity.length()) > 0.5) {

            return false;
        }




//        System.out.println("****************\nAdding agent "+agent.getId() +" to cluster");
//        System.out.println("Current Cluster center"+ this.getCentre());
//        System.out.println("New point position "+ agent.getCurrentPosition());

        double distanceFromCentreToAgent = agent.getCurrentPosition().distance(this.getCentre());
//        System.out.println("Distance from center to new position"+distanceFromCentreToAgent );
        if (distanceFromCentreToAgent < this.getRadius()) {

            double newCentreX = ((this.getCentre().x * this.agents.size()) + agent.getX()) / (agents.size() + 1);
            double newCentreY = ((this.getCentre().y * this.agents.size()) + agent.getY()) / (agents.size() + 1);
            this.setCentre(new Point2d(newCentreX, newCentreY));

            velocity.scale(agents.size() - 1);
            velocity.add(agent.getVelocity());
            velocity.scale(1.0 / agents.size());

            double maxDistance = Double.MIN_VALUE;

            for (RVOAgent tempAgent : agents) {
                Vector2d distance = new Vector2d(tempAgent.getCurrentPosition());
                distance.sub(this.getCentre());
                if (distance.length() > maxDistance) {
                    maxDistance = distance.length();
                }
            }
            this.setRadius(maxDistance + RVOAgent.RADIUS);

            agents.add(agent);


//            System.out.println("Within cluster oledi\n***************");
            return true;
        } else if ((distanceFromCentreToAgent + this.getRadius()) < (2.0 * maxRadius)) {
//            System.out.println("Can be fitted in this cluster");
//            Vector2d agentToCenter = new Vector2d(this.getCentre());
//            agentToCenter.sub(new Vector2d(agent.getCurrentPosition()));
//            agentToCenter.normalize();
//            agentToCenter.scale(-(distanceFromCentreToAgent) / 2.0);
//            agentToCenter.add(this.getCentre());
//            System.out.println("New center : "+agentToCenter);
//            System.out.println("Old radius : "+this.getRadius());
//            this.setCentre(new Point2d(agentToCenter));
//            this.setRadius((distanceFromCentreToAgent + (2.0 * this.getRadius())) / 2.0);
            //        System.out.println("New radius"+this.getRadius()+"\n****");

            /**
             *new velocity  = old velocity *(n-1) + new agent velocity / n
             */
            double newCentreX = ((this.getCentre().x * this.agents.size()) + agent.getX()) / (agents.size() + 1);
            double newCentreY = ((this.getCentre().y * this.agents.size()) + agent.getY()) / (agents.size() + 1);
            this.setCentre(new Point2d(newCentreX, newCentreY));



            velocity.scale(agents.size() - 1);
            velocity.add(agent.getVelocity());
            velocity.scale(1.0 / agents.size());


            agents.add(new RVOAgent(agent));

            double maxDistance = Double.MIN_VALUE;
            for (RVOAgent tempAgent : agents) {
                Vector2d distance = new Vector2d(tempAgent.getCurrentPosition());
                distance.sub(this.getCentre());
                if (distance.length() > maxDistance) {
                    maxDistance = distance.length();
                }
            }
            this.setRadius(maxDistance + RVOAgent.RADIUS);
            return true;
        }
        return false;
    }

  
}
