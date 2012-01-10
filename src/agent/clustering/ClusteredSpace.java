/**
 * 
 *
 * @author Vaisagh
 * Created: Jan, 2011
 *
 * 
 *
 * Description:This class extends RVOSpace to have multiple layers to process the
 * perception of agents as clusters. It overrides the existing senseNeighbours()
 * to sense agents from the clustering layers. 
 *
 */
package agent.clustering;

import agent.RVOAgent;
import app.RVOModel;
import environment.RVOSpace;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.vecmath.Vector2d;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 *
 * @author Vaisagh
 */
public final class ClusteredSpace extends RVOSpace {

    public static double ALPHA;
    public static int NUMBER_OF_CLUSTERING_SPACES;
    final double AGENT_DIAMETER = RVOAgent.RADIUS * 2.0;
    protected static int numberOfClusteringSpaces;
    protected ArrayList<Continuous2D> clusteringLayers;

    public ClusteredSpace(int xSize, int ySize, double gridSize, RVOModel rm) {
        this(xSize, ySize, gridSize, rm, NUMBER_OF_CLUSTERING_SPACES);
    }

    public ClusteredSpace(int xSize, int ySize, double gridSize, RVOModel rm,
            int numberOfLayers) {
        super(xSize, ySize, gridSize, rm);
        numberOfClusteringSpaces = numberOfLayers;
        clusteringLayers = new ArrayList<Continuous2D>();
        for (int i = 0; i < numberOfClusteringSpaces; i++) {
            clusteringLayers.add(new Continuous2D(gridDimension, xRealSize,
                    yRealSize));

        }

        scheduleClustering();
    }

    public Continuous2D getClusteredSpace(int i) {
        return clusteringLayers.get(i);
    }

    public static int getNumberOfClusteringSpaces() {
        return numberOfClusteringSpaces;
    }

    public void updatePositionOnMap(ClusteredAgent clusteredAgent, int layer) {
        clusteringLayers.get(layer).setObjectLocation(clusteredAgent, new Double2D(clusteredAgent.getX(), clusteredAgent.getY()));
    }

    /**
     * This function replaces the function in RVOSpace, and calculates all the 
     * neighbours as clusters of agents depending on distance.
     * @param me : the agent whose neighbours need to be found
     * @return Bag of neighbours found
     */
    @Override
    public Bag senseNeighbours(RVOAgent me) {


        Bag neighbourAgents = agentSpace.getObjectsExactlyWithinDistance(
                new Double2D(me.getCurrentPosition().x, me.getCurrentPosition().y),
                ClusteredSpace.calculateRadiusForRegion(0) + RVOAgent.RADIUS);
 
        Bag[] neighbourClusters = new Bag[numberOfClusteringSpaces];
        Set<RVOAgent> finalNeighbourSet = new HashSet<RVOAgent>();
        finalNeighbourSet.addAll(neighbourAgents);

        //Detect all neighbours. This detects all neighbours in all ranges for
        //now. Can't remember exactly why I have used such a big sensor range
        for (int layer = 0; layer < numberOfClusteringSpaces; layer++) {
            ClusteredAgent clusteredMe = new ClusteredAgent(this, me, ClusteredSpace.calculateMaxClusterRadiusForRegion(layer));
            neighbourClusters[layer] = clusteringLayers.get(layer).getObjectsExactlyWithinDistance(
                    new Double2D(me.getCurrentPosition().x, me.getCurrentPosition().y),
                    ClusteredSpace.calculateRadiusForRegion(layer + 1) + RVOAgent.RADIUS);
            neighbourClusters[layer].remove(clusteredMe);
        }

//        System.out.println("Starting filtering Process after detecting"
//                + neighbourClusters.numObjs + " clusters:");

        /*
        Loop to remove all the clusters that are close to the agent.
         */
        for (int layer = 0; layer < numberOfClusteringSpaces; layer++) {
            for (int i = 0; i < neighbourClusters[layer].size(); i++) {
                if (neighbourClusters[layer].get(i).getClass() != ClusteredAgent.class) {
                    continue;
                }

                ClusteredAgent clusteredNeighbour = (ClusteredAgent) neighbourClusters[layer].get(i);
                Vector2d fromClusterCenterToAgent = new Vector2d(me.getCurrentPosition());

                // vector from cluster center to agent center
                fromClusterCenterToAgent.sub(clusteredNeighbour.getCurrentPosition());

                double minDistance = (fromClusterCenterToAgent.length() - clusteredNeighbour.getRadius());
                double maxDistance = (fromClusterCenterToAgent.length() + clusteredNeighbour.getRadius());
                /*
                break up clusters that are too close to the agent and thus will 
                 * not be detected as clusters. They need to be broken up so that 
                 * they will at least be perceived as individuals.
                 */
                if (minDistance <= ClusteredSpace.calculateRadiusForRegion(layer)
                        && maxDistance >= ClusteredSpace.calculateRadiusForRegion(layer)) {
//                  System.out.println("For agent at"+me.getCurrentPosition()+
//                    " deleted a cluster at"+ tempAgent.getCentre());

                    /*Adds all the individual agents not within the first region 
                     * as individuals into respective layer  so that they can be 
                     * added in later
                     */
                    for (RVOAgent individualAgent : clusteredNeighbour.getAgents()) {
                        neighbourClusters[layer].add(individualAgent);
                    }

                    neighbourClusters[layer].remove(i);
                    i--;
                    continue;
                }
            }
        }



        /*
         * For each region beyond the first two
         */
        for (int layer = 0; layer < numberOfClusteringSpaces; layer++) {
            for (Object agent : neighbourClusters[layer]) {
                RVOAgent clusteredAgent = (RVOAgent) agent;
                Vector2d fromClusterCenterToAgentCenter = new Vector2d(me.getCurrentPosition());
                fromClusterCenterToAgentCenter.sub(clusteredAgent.getCurrentPosition());
                double minDistance =
                        (fromClusterCenterToAgentCenter.length() - clusteredAgent.getRadius());


                //   double totalMaxDistance = distance.length() + ((ClusteredAgent) neighbourClusters[layer].get(i)).getRadius();
                if (layer < numberOfClusteringSpaces - 1) {
                    if (minDistance <= calculateRadiusForRegion(layer + 1)
                            && minDistance >= calculateRadiusForRegion(layer)) {

                        finalNeighbourSet.add(clusteredAgent);
//                  System.out.println("For agent at"+me.getCurrentPosition()+" added a neighbour at"+ ((RVOAgent)neighbourClusters[layer].get(i)).getCurrentPosition());
                    }
                } else {
                    if (minDistance >= calculateRadiusForRegion(layer)) {
                        finalNeighbourSet.add(clusteredAgent);
                    }
                }
            }
        }

        finalNeighbourSet.remove(me);
        Bag finalList = new Bag();
        finalList.addAll(finalNeighbourSet);
        return finalList;

    }

    public void scheduleClustering() {
        rvoModel.schedule.scheduleRepeating(new Clustered(), 3, 1.0);
    }

    private static double calculateRadiusForRegion(int region) {
        if (region == -2) {
            return RVOAgent.RADIUS;
        }
//        return calculateRadiusForRegion(region - 1) + ALPHA * 2.0 * calculateRadiusForRegion(region - 1);
        return ALPHA * 2.0 * calculateRadiusForRegion(region - 1);
    }

    private static double calculateMaxClusterRadiusForRegion(int layer) {
        if (layer == -2) {
            return RVOAgent.RADIUS;
        }
//        return ALPHA * calculateRadiusForRegion(layer - 1);
        return ALPHA * 2.0 * calculateMaxClusterRadiusForRegion(layer - 1);
    }

    class Clustered implements Steppable {

        /**
         * Executed once per step right at the beginning. 
         * @param model 
         */
        @Override
        public void step(SimState ss) {
//            System.out.println("updating ");

            //Clear all clusters
            for (int i = 0; i < numberOfClusteringSpaces; i++) {
                clusteringLayers.get(i).clear();

            }

//            for (int layer = 0; layer < numberOfClusteringSpaces; layer++) {
//                System.out.println("max cluster radius" + layer + "=" + calculateMaxClusterRadiusForRegion(layer));
//                System.out.println("region radius" + layer + "=" + calculateRadiusForRegion(layer));
//            }


            ClusteredAgent tempCluster;

            ArrayList<ClusteredAgent> tempClusterList = new ArrayList<ClusteredAgent>();
            boolean added = false;
            boolean simpleAddPossible = false;
            for (int layer = 0; layer < numberOfClusteringSpaces; layer++) {
                tempClusterList.clear();

                for (RVOAgent agent : rvoModel.getAgentList()) {

                    /*
                     * Check if it can be added to calculated clusters
                     */
                    double minExtendedDistance = Double.MAX_VALUE;
                    double distance;
                    double extendedDistance;
                    ClusteredAgent bestCluster = null;
                    double minDistance = Double.MAX_VALUE;


                    for (ClusteredAgent existingCluster : tempClusterList) {

                        distance = existingCluster.getCurrentPosition().distance(agent.getCurrentPosition()) + agent.getRadius();
                        if (distance < existingCluster.getRadius()) {

                            if (distance < minDistance) {
                                simpleAddPossible = true;
                                minDistance = distance;
                                bestCluster = existingCluster;
                            }

                        }
                        if (!simpleAddPossible) {


                            extendedDistance = distance + existingCluster.getRadius();

                            if (extendedDistance < minExtendedDistance
                                    && extendedDistance <= 2 * existingCluster.getMaxRadius()) {
                                minExtendedDistance = extendedDistance;
                                bestCluster = existingCluster;

                            }
                        }
                    }

                    if (simpleAddPossible) {
                        bestCluster.simplyAddAgent(agent);
                        bestCluster.updateVelocityAndMass();
                        added = true;
                        simpleAddPossible = false;

                    }

                    if (!added && bestCluster != null) {
//                        System.out.println("adding agent"+ agent +"to" +bestCluster.getAgents());
                        double newRadius = minExtendedDistance / 2.0;
                        Vector2d connectingOldCenterToPoint = new Vector2d(bestCluster.getCurrentPosition());
                        connectingOldCenterToPoint.sub(agent.getCurrentPosition());
                        connectingOldCenterToPoint.normalize();
                        connectingOldCenterToPoint.scale(newRadius - agent.getRadius());
                        double newX = agent.getCurrentPosition().x + connectingOldCenterToPoint.x;
                        double newY = agent.getCurrentPosition().y + connectingOldCenterToPoint.y;

                        ArrayList<Boolean> innerChecks = new ArrayList<Boolean>();
                        //If there are no agents near the center then delete those clusters

                        /*
                         * Was earlier checking for lower layer clusters... 
                         * checking for individual agents now... need to change 
                         * back if unreasonable behavior
                         */
                        innerChecks.add(
                                agentSpace.getObjectsExactlyWithinDistance(
                                new Double2D(newX, newY),
                                calculateMaxClusterRadiusForRegion(-1)).isEmpty());

                        innerChecks.add(
                                agentSpace.getObjectsExactlyWithinDistance(
                                new Double2D(newX - (newRadius / 2),
                                newY),
                                calculateMaxClusterRadiusForRegion(-1)).isEmpty());


                        innerChecks.add(
                                agentSpace.getObjectsExactlyWithinDistance(
                                new Double2D(newX + (newRadius / 2),
                                newY),
                                calculateMaxClusterRadiusForRegion(-1)).isEmpty());


                        innerChecks.add(
                                agentSpace.getObjectsExactlyWithinDistance(
                                new Double2D(newX,
                                newY - (newRadius / 2)),
                                calculateMaxClusterRadiusForRegion(-1)).isEmpty());

                        innerChecks.add(
                                agentSpace.getObjectsExactlyWithinDistance(
                                new Double2D(newX,
                                newY + (newRadius / 2)),
                                calculateMaxClusterRadiusForRegion(-1)).isEmpty());

                        boolean dontAdd = false;

                        for (Boolean isEmpty : innerChecks) {
                            if (isEmpty) {

                                dontAdd = true;
                                break;
                            }
                        }
                        if (!dontAdd) {

                            bestCluster.simplyAddAgent(agent);
                            bestCluster.setRadius(newRadius);
                            bestCluster.setCurrentPosition(newX, newY);
                            bestCluster.updateVelocityAndMass();
                            added = true;
                        }
                    }

                    /*
                     * if not added create a new cluster
                     */
                    if (!added) {

//                        System.out.println("dont' add. create seperate for " + agent);
                        tempCluster = new ClusteredAgent(ClusteredSpace.this,
                                agent, calculateMaxClusterRadiusForRegion(layer));

                        tempClusterList.add(tempCluster);

                    }

                    added = false;

                }



                for (ClusteredAgent currentCluster : tempClusterList) {
                    boolean toBeDeleted = false;


                    // add all clusters to the list that haven't been deleted
                    if (!toBeDeleted) {

                        updatePositionOnMap(currentCluster, layer);
                    } else {
                        /*Add the agents as individual entities if they are actually to be deleted. 
                         * This might cause redundancy at the higher layers but 
                         * is necessary because otherwise agents might not be detected.*/
                        for (RVOAgent individualAgent : currentCluster.getAgents()) {
                            ClusteredAgent tempAgent = new ClusteredAgent(ClusteredSpace.this,
                                    individualAgent, calculateMaxClusterRadiusForRegion(layer));

                            updatePositionOnMap(tempAgent, layer);
                        }
                        toBeDeleted = false;
                    }

                }

            }
        }
    }
}
