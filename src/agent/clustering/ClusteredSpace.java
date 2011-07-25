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
    public static int CLUSTER_DIAMETER;
    public static int NUMBER_OF_CLUSTERING_SPACES;
    protected static int numberOfClusteringSpaces;
    protected ArrayList<ClusteredAgent>[] clusteredAgents;
    protected Continuous2D[] clusteringLayers;

    public ClusteredSpace(int xSize, int ySize, double gridSize, RVOModel rm) {
        this(xSize, ySize, gridSize, rm, NUMBER_OF_CLUSTERING_SPACES);
    }

    public ClusteredSpace(int xSize, int ySize, double gridSize, RVOModel rm,
            int numberOfLayers) {
        super(xSize, ySize, gridSize, rm);
        numberOfClusteringSpaces = numberOfLayers;
        clusteringLayers = new Continuous2D[numberOfClusteringSpaces];
        clusteredAgents = new ArrayList[numberOfClusteringSpaces];

        for (int i = 0; i < numberOfClusteringSpaces; i++) {
            clusteringLayers[i] = new Continuous2D(gridDimension, xRealSize,
                    yRealSize);
            clusteredAgents[i] = new ArrayList<ClusteredAgent>();
        }
        
        scheduleClustering();
    }

    public Continuous2D getClusteredSpace(int i) {
        return clusteringLayers[i];
    }

    public static int getNumberOfClusteringSpaces() {
        return numberOfClusteringSpaces;
    }

    public void updatePositionOnMap(ClusteredAgent clusteredAgent, int layer) {
        clusteringLayers[layer].setObjectLocation(clusteredAgent, new Double2D(clusteredAgent.getX(), clusteredAgent.getY()));
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
                new Double2D(me.getCurrentPosition().x, me.getCurrentPosition().y), RVOAgent.SENSOR_RANGE * me.getRadius());
        Bag[] neighbourClusters = new Bag[numberOfClusteringSpaces];
        Bag finalNeighbourSet = new Bag();

        //Detect all neighbours. This detects all neighbours in all ranges for
        //now. Can't remember exactly why I have used such a big sensor range
        for (int layer = 0; layer < numberOfClusteringSpaces; layer++) {
            neighbourClusters[layer] = clusteringLayers[layer].getObjectsExactlyWithinDistance(new Double2D(me.getCurrentPosition().x, me.getCurrentPosition().y),
                    RVOAgent.SENSOR_RANGE * me.getRadius());

        }

//        System.out.println("Starting filtering Process after detecting"
//                + neighbourClusters.numObjs + " clusters:");

        /**
         * Loop to remove all the clusters that are close to the agent.
         */
        for (int layer = 0; layer < numberOfClusteringSpaces; layer++) {
            for (int i = 0; i < neighbourClusters[layer].size(); i++) {

                ClusteredAgent tempAgent = (ClusteredAgent) neighbourClusters[layer].get(i);
                Vector2d fromClusterCenterToAgent = new Vector2d(me.getCurrentPosition());
                fromClusterCenterToAgent.sub(tempAgent.getCentre());
                // distance from cluster center to agent center


                //remove if cluster center is too close to the agent and add 
                //only individual agents ( this takes care of agents which are
                //in clusters taht are within this region but the agents are actually outside

                if ((int) Math.round((fromClusterCenterToAgent.length() - tempAgent.getRadius()) * 100) <= RVOAgent.RADIUS + CLUSTER_DIAMETER) {
//                  System.out.println("For agent at"+me.getCurrentPosition()+" deleted a cluster at"+ tempAgent.getCentre());
                    //Again lots of repitition across layers, can be avoided
                    for (int k = 0; k < tempAgent.getAgents().size(); k++) {
                        Vector2d distance = new Vector2d(me.getCurrentPosition());
                        distance.sub(((RVOAgent) tempAgent.getAgents().get(k)).getCurrentPosition());
                        if (distance.length() * 100 > RVOAgent.RADIUS + CLUSTER_DIAMETER) {
                            finalNeighbourSet.add(tempAgent.getAgents().get(k));

                        }
                    }

                    neighbourClusters[layer].remove(i);
                    i--;
                    continue;
                }
            }
        }


        //Add neighbour agents within my first inner circle
        for (int i = 0; i < neighbourAgents.size(); i++) {
            Vector2d distance = new Vector2d(me.getCurrentPosition());
            distance.sub(((RVOAgent) neighbourAgents.get(i)).getCurrentPosition());
//                System.out.println("Non Clustered agent at distance "+distance.length());
            //Lots of redundancy
            if (me.getCurrentPosition() != ((RVOAgent) neighbourAgents.get(i)).getCurrentPosition() && distance.length() * 100 <= RVOAgent.RADIUS + CLUSTER_DIAMETER) {

                finalNeighbourSet.add(neighbourAgents.get(i));
//                System.out.println("For agent at" + me.getCurrentPosition() + " added a neighbour at" + ((RVOAgent) neighbourAgents.get(i)).getCurrentPosition());

            }
        }

        for (int i = 0; i < neighbourClusters[0].size(); i++) {

            ClusteredAgent tempAgent = (ClusteredAgent) neighbourClusters[0].get(i);
            Vector2d fromClusterCenterToAgent = new Vector2d(me.getCurrentPosition());
            fromClusterCenterToAgent.sub(tempAgent.getCentre());

            if (((fromClusterCenterToAgent.length() - tempAgent.getRadius()) * 100 >= RVOAgent.RADIUS + CLUSTER_DIAMETER)
                    && ((fromClusterCenterToAgent.length() - tempAgent.getRadius()) * 100 <= RVOAgent.RADIUS + CLUSTER_DIAMETER + ALPHA * CLUSTER_DIAMETER)) {
                finalNeighbourSet.add(tempAgent);
            }

        }


        for (int layer = 1; layer < numberOfClusteringSpaces; layer++) {
            for (int i = 0; i < neighbourClusters[layer].size(); i++) {
                Vector2d distance = new Vector2d(me.getCurrentPosition());
                distance.sub(((ClusteredAgent) neighbourClusters[layer].get(i)).getCurrentPosition());
                double totalMinDistance = distance.length() - ((ClusteredAgent) neighbourClusters[layer].get(i)).getRadius();
                //   double totalMaxDistance = distance.length() + ((ClusteredAgent) neighbourClusters[layer].get(i)).getRadius();
                if (layer < numberOfClusteringSpaces - 1
                        && totalMinDistance * 100 <= (RVOAgent.RADIUS + (ClusteredSpace.CLUSTER_DIAMETER * (1 - Math.pow(ALPHA, (layer + 1))) / (1 - ALPHA)))
                        && totalMinDistance * 100 >= (RVOAgent.RADIUS + (ClusteredSpace.CLUSTER_DIAMETER * (1 - Math.pow(ALPHA, (layer))) / (1 - ALPHA)))) {
                    finalNeighbourSet.add(neighbourClusters[layer].get(i));
//                  System.out.println("For agent at"+me.getCurrentPosition()+" added a neighbour at"+ ((RVOAgent)neighbourClusters[layer].get(i)).getCurrentPosition());
                } else if (totalMinDistance * 100 >= (RVOAgent.RADIUS + (ClusteredSpace.CLUSTER_DIAMETER * (1 - Math.pow(ALPHA, (layer))) / (1 - ALPHA)))) {
                    finalNeighbourSet.add(neighbourClusters[layer].get(i));
//                
                }
            }
        }
        return finalNeighbourSet;

    }

    public void scheduleClustering() {
        rvoModel.schedule.scheduleRepeating(new Clustered(), 1, 1.0);
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
                clusteringLayers[i].clear();
                clusteredAgents[i].clear();
            }
            ClusteredAgent tempCluster;
            RVOAgent tempAgent;
            ArrayList<ClusteredAgent> tempClusterList = new ArrayList<ClusteredAgent>();
            boolean added = false;
            for (int layer = 0; layer < numberOfClusteringSpaces; layer++) {
                tempClusterList.clear();

                for (int i = 0; i < rvoModel.getAgentList().size(); i++) {
                    tempAgent = rvoModel.getAgentList().get(i);
                    for (int j = 0; j < tempClusterList.size(); j++) {

                        if (tempClusterList.get(j).addAgent(tempAgent)) {
                            added = true;
                            break;
                        }
                    }
                    if (!added) {
                        tempCluster = new ClusteredAgent(ClusteredSpace.this,
                                rvoModel.getAgentList().get(i));
                        tempCluster.setMaxRadius((Math.pow(ALPHA, layer)
                                * ((double) CLUSTER_DIAMETER / 100.0)) / 2.0);
                        tempClusterList.add(tempCluster);

                    }

                    added = false;

                }


                for (int i = 0; i < tempClusterList.size(); i++) {
                    boolean toBeDeleted = false;
                    ClusteredAgent currentCluster = tempClusterList.get(i);
                    Bag lowerLevelClusters = new Bag();

                    //If there are no agents near the center then delete those clusters
                    if (layer > 1) {
                        lowerLevelClusters =
                                clusteringLayers[layer - 1].getObjectsExactlyWithinDistance(
                                new Double2D(currentCluster.getCentre().getX(),
                                currentCluster.getCentre().getY()),
                                clusteredAgents[0].get(0).getMaxRadius());

                        if (lowerLevelClusters.isEmpty()) {
                            toBeDeleted = true;
                            break;
                        }

                    }



                    // If any other cluster is too close then delete that cluster too
                    for (int j = 0; j < tempClusterList.size() && !toBeDeleted; j++) {
                        if (i != j) {
                            ClusteredAgent otherCluster = tempClusterList.get(j);
                            Vector2d distance = new Vector2d(otherCluster.getCentre());
                            distance.sub(currentCluster.getCentre());
                            if ((distance.length() <= otherCluster.getRadius()
                                    + currentCluster.getRadius() + 0.3)) {
                                toBeDeleted = true;
                                break;
                            }



                        }
                    }

                    // add all clusters to the list that haven't been deleted
                    if (!toBeDeleted) {
                        clusteredAgents[layer].add(currentCluster);
                        updatePositionOnMap(clusteredAgents[layer].get(
                                clusteredAgents[layer].size() - 1), layer);
                    } else {
                        // Add the agents as individual entities if they are actually to be deleted. This might cause redundancy at the higher layers but is necessary because otherwise agents might not be detected.
                        for (int j = 0; j < tempClusterList.get(i).getAgents().size(); j++) {
                            clusteredAgents[layer].add(new ClusteredAgent(ClusteredSpace.this,
                                    tempClusterList.get(i).getAgents().get(j)));
                            updatePositionOnMap(clusteredAgents[layer].get(clusteredAgents[layer].size() - 1), layer);
                        }
                        toBeDeleted = false;
                    }

                }

            }
        }
    }
}
