/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package environment.Obstacle;
import agent.RVOAgent;
import java.awt.Color;
import java.util.ArrayList;
import javax.vecmath.Point2d;
import environment.RVOSpace;
import sim.util.Double2D;
/**
 *
 * @author steven
 */
public class SocialForceObstacle extends ObstaclePortrayal{

    ArrayList<RVOAgent> staticAgents;
    double R = 0.15;
    
    public SocialForceObstacle(Point2d point1,Point2d point2,RVOSpace mySpace){
        this.vertices.add(point1);
        this.vertices.add(point2);
        
        double x1 = vertices.get(0).x;
        double y1 = vertices.get(0).y;
        double x2 = vertices.get(1).x;
        double y2 = vertices.get(1).y;
        
        int N = (int) Math.ceil(Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1))/R);    //numberOfStaticAgents
        double xIncrement = linspace(x1,x2,N);
        double yIncrement = linspace(y1,y2,N);
        
        double bufStaticAgentX = x1;
        double bufStaticAgentY = y1;

        staticAgents = new ArrayList<RVOAgent>();
        
        for (int i=0; i<N; i++){
            bufStaticAgentX += xIncrement;
            bufStaticAgentY += yIncrement;
            
            
            Point2d location = new Point2d(bufStaticAgentX,bufStaticAgentY);
            RVOAgent tempAgent = new RVOAgent(location, location, mySpace, Color.black);
//            tempAgent.setSocialForceObstacle(true);
            
            mySpace.getCurrentAgentSpace().setObjectLocation(
            tempAgent,
            new Double2D(location.x,location.y));
        }
        
    }
    
    // Linear Spacing function
    // a = first vertex
    // b = second vertex
    // n = number of static agent
    private double linspace(double a, double b, int n) {
        return (b-a)/n;
    }
    
    public ArrayList<RVOAgent> getStaticAgents(){
        return staticAgents;
    }
}
