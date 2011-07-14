/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

import app.RVOModel;
import environment.geography.Goals;
import environment.geography.Position;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 *
 * @author vaisaghvt
 */
public class AgentGenerator implements Steppable {

    int number;
    Point2d startPoint;
    Point2d endPoint;
    RVOModel model;
    int directionX;
    int directionY;
    double gap;
    ArrayList<Goals> goals;

    public AgentGenerator(Point2d start, Point2d end, int number, int direction,List<Goals> passedGoals, RVOModel model) {
        startPoint = new Point2d(start.getX(), start.getY());
        endPoint = new Point2d(end.getX(), end.getY());
        this.number = number;
        this.model = model;
        //0 means -x, 1 means x, 2 means y, 3 means -y
        switch (direction) {
            case 0:
                directionX = -1;
                directionY = 0;
                break;
            case 1:
                directionX = 1;
                directionY = 0;
                break;
            case 2:
                directionX = 0;
                directionY = 1;
                break;
            case 3:
                directionX = 0;
                directionY = -1;
                break;
        }

        this.goals = new ArrayList(passedGoals.size());
        for(int i=0;i<passedGoals.size();i++){
            Goals tempGoal = new Goals();
           Position startPosition = new Position();

           startPosition.setX(passedGoals.get(i).getVertices().get(0).getX());
            startPosition.setY(passedGoals.get(i).getVertices().get(0).getY());

           Position endPosition = new Position();

           endPosition.setX(passedGoals.get(i).getVertices().get(1).getX());
            endPosition.setY(passedGoals.get(i).getVertices().get(1).getY());


            tempGoal.getVertices().add(startPosition);
            tempGoal.getVertices().add(endPosition);
            goals.add(tempGoal);
        }

        Vector2d distance = new Vector2d(startPoint);
        distance.sub(endPoint);


        gap = (number>1)?distance.length() / (number-1):distance.length()/2;
        System.out.println("distance = " + distance.length());
        System.out.println("number = "+number+",gap = " + gap);
    }

    @Override
    public void step(SimState ss) {
        for (int i = 0; i < number; i++) {
            RVOAgent agent = new RVOAgent(model.getRvoSpace());
            if (directionY != 0) {

                agent.setCurrentPosition(startPoint.getX() + gap * i, startPoint.getY());
            } else {

                agent.setCurrentPosition(startPoint.getX(), startPoint.getY() + gap * i);
            }

         //   agent.setGoal(new Point2d(6.0, 0.0));
            agent.setCheckPoints(goals);
            agent.findPrefVelocity();
            agent.setVelocity(agent.getPrefVelocity());

            model.addNewAgent(agent);

        }


    }
}
