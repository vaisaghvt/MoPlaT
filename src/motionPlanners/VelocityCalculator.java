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
 *
 */
public interface VelocityCalculator {

       public Vector2d calculateVelocity(RVOAgent me,
            Bag neighbors, Bag obses, Vector2d preferredVelocity, double timeStep);
}
