/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package motionPlanners;

import agent.RVOAgent;
import javax.vecmath.Vector2d;
import sim.util.Bag;

/**
 *
 * @author michaellees
 *
 * The interface which needs to implemented by all classes that implement 
 * collision detection algorithms
 *
 */
public interface VelocityCalculator {

       public Vector2d calculateVelocity(RVOAgent me,
            Bag neighbors, Bag obses, Vector2d preferredVelocity, double timeStep);
}
