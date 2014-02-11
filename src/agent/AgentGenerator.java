/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

import app.RVOModel;
import com.google.common.collect.HashMultimap;
import environment.geography.AgentLine;
import environment.geography.Goals;
import environment.geography.Position;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This class defines the agent generator line that can generate agents at
 * requested positions with a given frequency
 *
 * TODO : Fix so that multiple generators can be used if needed
 * @author Vaisagh
 */
public class AgentGenerator implements Steppable {
    private static boolean finished = true;

    int generatorsPerLine;
    Point2d startPoint;
    Point2d endPoint;
    RVOModel model;
    double gap;
    private int steps;
    private final HashMultimap<Integer, Point2d> actualRoadMap;
    private final double maxSpeed;
    private final double minSpeed;
    private final double sdevSpeed;
    private final double meanSpeed;
    private final List<? extends Point2d> generationPoints;
    private Vector2d preferedDirection;

    public AgentGenerator(AgentLine agentLine, RVOModel model, HashMultimap<Integer, Point2d> actualRoadMap) {
        finished = false;
        maxSpeed = agentLine.getMaxSpeed();
        minSpeed = agentLine.getMinSpeed();
        meanSpeed = agentLine.getMeanSpeed();
        sdevSpeed = agentLine.getSDevSpeed();


        startPoint = new Point2d(agentLine.getStartPoint().getX(), agentLine.getStartPoint().getY());
        endPoint = new Point2d(agentLine.getEndPoint().getX(), agentLine.getEndPoint().getY());
        this.actualRoadMap = actualRoadMap;

        generatorsPerLine = agentLine.getNumber();
        steps = 0;
        this.model = model;

        Vector2d distance = new Vector2d(startPoint);
        distance.sub(endPoint);



        generationPoints = getGoalPoints(RVOAgent.RADIUS, generatorsPerLine, startPoint, endPoint);
        createAgents();
    }
    
    public AgentGenerator(AgentLine agentLine, RVOModel model, Vector2d direction) {
        finished = false;
        maxSpeed = agentLine.getMaxSpeed();
        minSpeed = agentLine.getMinSpeed();
        meanSpeed = agentLine.getMeanSpeed();
        sdevSpeed = agentLine.getSDevSpeed();


        startPoint = new Point2d(agentLine.getStartPoint().getX(), agentLine.getStartPoint().getY());
        endPoint = new Point2d(agentLine.getEndPoint().getX(), agentLine.getEndPoint().getY());
        
        this.actualRoadMap = null;
        this.preferedDirection = new Vector2d(direction);

        generatorsPerLine = agentLine.getNumber();
        steps = 0;
        this.model = model;

        Vector2d distance = new Vector2d(startPoint);
        distance.sub(endPoint);



        generationPoints = getGoalPoints(RVOAgent.RADIUS, generatorsPerLine, startPoint, endPoint);
        createAgents();
    }

    public static List<? extends Point2d> getGoalPoints(double agentRadius, int numberOfAgents, Point2d startPoint, Point2d endPoint) {
        assert numberOfAgents >= 1;
        ArrayList<Point2d> results = new ArrayList<Point2d>();
        double generationLineLength = startPoint.distance(endPoint) - 2 * agentRadius;


        double firstAgentDistance;
        double subsequentDistance = 0.0;
        if (numberOfAgents == 1) {
            firstAgentDistance = agentRadius + generationLineLength / 2;
        } else {
            firstAgentDistance = agentRadius;
            subsequentDistance = generationLineLength / (numberOfAgents - 1);
            if (subsequentDistance < agentRadius * 2) {
                System.out.println("WARNING!!! Cannot generate that many points. "
                        + "Reducing to what is physically possible");
                //vvt : Not sure if it would be better to just make the program fail here.
                subsequentDistance = agentRadius * 2;
            }
        }
//        double subsequentDistance = agentRadius * 2;
        Vector2d unitDirection = new Vector2d(endPoint);
        unitDirection.sub(startPoint);
        unitDirection.normalize();



        Point2d firstAgentLocation = new Point2d(unitDirection);
        firstAgentLocation.scale(firstAgentDistance);
        firstAgentLocation.add(startPoint);

        results.add(firstAgentLocation);
        for (int i = 1; i < numberOfAgents; i++) {
            Point2d newPosition = new Point2d(unitDirection);
            newPosition.scale(firstAgentDistance + subsequentDistance * i);
            newPosition.add(startPoint);

            assert !Double.isNaN(newPosition.x);

            results.add(newPosition);
        }

        return results;
    }

   

    @Override
    public void step(SimState ss) {
        createAgents();
        steps++;
        if (steps > 50) { //TODO : MAke into a variable in property set
            model.getGeneratorStoppable().stop();
            AgentGenerator.finished = true;
        }
    }

    private void createAgents() {
        for (int i = 0; i < generatorsPerLine; i++) {
            RVOAgent agent = new RVOAgent(model.getRvoSpace());
            Point2d agentPosition = generationPoints.get(i);
            agent.setCurrentPosition(agentPosition.x, agentPosition.y);

            double initialSpeed = model.random.nextGaussian() * sdevSpeed + meanSpeed;
            if (initialSpeed < minSpeed) {
                initialSpeed = minSpeed;
            } else if (initialSpeed > maxSpeed) {
                initialSpeed = maxSpeed;
            }

            agent.setPreferredSpeed(initialSpeed);
            agent.setMaximumSpeed(maxSpeed);

            
            if(actualRoadMap !=null){
                agent.addRoadMap(actualRoadMap);
            }else{
                 this.preferedDirection.normalize();
                this.preferedDirection.scale(100);
                //TODO : Added for setting hu nan's experiments
                    Point2d goal = new Point2d(agent.getCurrentPosition());
                    goal.add(this.preferedDirection);
                    agent.setGoal(goal);
            }

            //   agent.setGoal(new Point2d(6.0, 0.0));
            agent.setPrefVelocity();

            model.addNewAgent(agent);

        }
    }
    
    public static boolean finished(){
        return AgentGenerator.finished;
    }
}
