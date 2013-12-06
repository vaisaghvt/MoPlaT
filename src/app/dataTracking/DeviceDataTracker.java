/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.dataTracking;

import agent.RVOAgent;
import app.PropertySet;
import app.RVOModel;
import com.google.common.collect.ArrayListMultimap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import javax.vecmath.Point2d;
import org.jfree.chart.JFreeChart;
import sim.engine.SimState;

/**
 *
 * @author AngelaTeo
 */
public class DeviceDataTracker implements DataTracker {

    private final RVOModel model;
    private int stepNumber;
    public static final float E_S = 2.23f;
    public static final float E_W = 1.26f;
    public static final String TRACKER_TYPE = "Device";
    private final ArrayListMultimap<Integer, Point2d> positionListForTimeStep;
    private ArrayList<ArrayList<ArrayList<Integer>>> numAgentsMatrix;
    private ArrayList<Double> FlowRateArray = new ArrayList<Double>();
    private HashMap<Integer, Double> TotalTimeTaken = new HashMap<Integer, Double>();
    private HashMap<Integer, Double> TrackStart = new HashMap<Integer, Double>();
    private HashMap<Integer, Double> DistanceTravelled = new HashMap<Integer, Double>();
    private double endTime;
    private int numAgentsExit = 0;
    private ArrayList<Integer> numExitArray = new ArrayList<Integer>();

    public DeviceDataTracker(RVOModel model, Collection<? extends RVOAgent> agents) {
        stepNumber = 0;
        this.model = model;
        positionListForTimeStep = ArrayListMultimap.create();
    }

    @Override
    public void storeToFile() {

        createMatrix();
        System.out.println("Attempt to call write function");
        writeMatrixToFile();
        System.out.println("Finished writing.");
        createFlowRate();
        writeFlowRateToFile();
        writeTimeTakenAgent();
        writeDistanceTravelled();

    }

    @Override
    public String trackerType() {
        return "device Data Tracker";
    }

    @Override
    public boolean hasChart() {
        return false;
    }

    @Override
    public JFreeChart getChart() {
        return null;
    }

    @Override
    /* at every step, it creates a matrix of all the positions of the agents */
    /* this is where the time taken for each agent to evacuate is calculated */
    public void step(SimState ss) {
        boolean agentsInside = false;
//        int exit = 28;
        int exitY = 29;
        int exitX= 27;
        
        
        //EXIT is on the RIGHT SIDE
//        for (RVOAgent agent : model.getAgentList()) {
//            positionListForTimeStep.put(stepNumber, agent.getCurrentPosition());
//            if (DistanceTravelled.containsKey(agent.getId()) && agent.getCurrentPosition().getX() <= exit) {
//                DistanceTravelled.put(agent.getId(), agent.getSpeed() * 
//                        PropertySet.TIMESTEP + DistanceTravelled.get(agent.getId()));
//            } else if (agent.getCurrentPosition().getX() <= exit) {
//                DistanceTravelled.put(agent.getId(), agent.getSpeed() * PropertySet.TIMESTEP);
//            }
//
//            if (!TrackStart.containsKey(agent.getId())) {
//                TrackStart.put(agent.getId(), stepNumber * PropertySet.TIMESTEP);
//            }
//
//            if (agent.getCurrentPosition().getX() > exit 
//                    && !TotalTimeTaken.containsKey(agent.getId())) {
//                TotalTimeTaken.put(agent.getId(), stepNumber * PropertySet.TIMESTEP - TrackStart.get(agent.getId()));
//                numAgentsExit++;
//            }
//
//            if (agent.getCurrentPosition().getX() <= exit) {
//                agentsInside = true;
//            }
//
//
//        }
        
        /*
        //EXIT is on the TOP
                for (RVOAgent agent : model.getAgentList()) {
            positionListForTimeStep.put(stepNumber, agent.getCurrentPosition());
            if (DistanceTravelled.containsKey(agent.getId()) && agent.getCurrentPosition().getY() >= exit) {
                DistanceTravelled.put(agent.getId(), agent.getSpeed() * PropertySet.TIMESTEP + DistanceTravelled.get(agent.getId()));
            } else if (agent.getCurrentPosition().getY() >= exit) {
                DistanceTravelled.put(agent.getId(), agent.getSpeed() * PropertySet.TIMESTEP);
            }

            if (!TrackStart.containsKey(agent.getId())) {
                TrackStart.put(agent.getId(), stepNumber * PropertySet.TIMESTEP);
            }

            if (agent.getCurrentPosition().getY() < exit && !TotalTimeTaken.containsKey(agent.getId())) {
                TotalTimeTaken.put(agent.getId(), stepNumber * PropertySet.TIMESTEP - TrackStart.get(agent.getId()));
                numAgentsExit++;
            }

            if (agent.getCurrentPosition().getY() >= exit) {
                agentsInside = true;
            }


        }*/
        /*
        //exit is at the top and side
        for (RVOAgent agent : model.getAgentList()) {
            positionListForTimeStep.put(stepNumber, agent.getCurrentPosition());
            if (DistanceTravelled.containsKey(agent.getId()) && 
                    (agent.getCurrentPosition().getY() >= exitY && 
                    agent.getCurrentPosition().getX() <= exitX)) {
                DistanceTravelled.put(agent.getId(), agent.getSpeed() * PropertySet.TIMESTEP + DistanceTravelled.get(agent.getId()));
            } else if (agent.getCurrentPosition().getY() >= exitY && agent.getCurrentPosition().getX() <= exitX) {
                DistanceTravelled.put(agent.getId(), agent.getSpeed() * PropertySet.TIMESTEP);
            }

            if (!TrackStart.containsKey(agent.getId())) {
                TrackStart.put(agent.getId(), stepNumber * PropertySet.TIMESTEP);
            }

            if ((agent.getCurrentPosition().getY() <= exitY || 
                    agent.getCurrentPosition().getX()>= exitX) && 
                    !TotalTimeTaken.containsKey(agent.getId())) {
                TotalTimeTaken.put(agent.getId(), stepNumber * PropertySet.TIMESTEP - TrackStart.get(agent.getId()));
                numAgentsExit++;
            }

            if (agent.getCurrentPosition().getY() > exitY && agent.getCurrentPosition().getX() < exitX) {
                agentsInside = true;
            }
        }
        */
        for (RVOAgent agent : model.getAgentList()) {
            positionListForTimeStep.put(stepNumber, agent.getCurrentPosition());
            if (DistanceTravelled.containsKey(agent.getId()) && 
                    (agent.getCurrentPosition().getY() <= exitY && 
                    agent.getCurrentPosition().getX() <= exitX)) {
                DistanceTravelled.put(agent.getId(), agent.getSpeed() * PropertySet.TIMESTEP + DistanceTravelled.get(agent.getId()));
            } else if (agent.getCurrentPosition().getY() <= exitY && agent.getCurrentPosition().getX() <= exitX) {
                DistanceTravelled.put(agent.getId(), agent.getSpeed() * PropertySet.TIMESTEP);
            }

            if (!TrackStart.containsKey(agent.getId())) {
                TrackStart.put(agent.getId(), stepNumber * PropertySet.TIMESTEP);
            }

            if ((agent.getCurrentPosition().getY() >= exitY || 
                    agent.getCurrentPosition().getX()>= exitX) && 
                    !TotalTimeTaken.containsKey(agent.getId())) {
                TotalTimeTaken.put(agent.getId(), stepNumber * PropertySet.TIMESTEP - TrackStart.get(agent.getId()));
                numAgentsExit++;
            }

            if (agent.getCurrentPosition().getY() < exitY && agent.getCurrentPosition().getX() < exitX) {
                agentsInside = true;
            }
        }
        
        if (!agentsInside) {
            endTime = stepNumber * PropertySet.TIMESTEP;
        }
        stepNumber++;
        numExitArray.add(numAgentsExit);
        
    }

    /* Creating the matrix at runtime. */
    public void createMatrix() {
        double gridFactor = 2;
        int minX = 1;
        int minY = 2;
        int maxX = 27;
        int maxY = 29;
        int xSize = (int) ((maxX - minX + 1) / gridFactor);   //COL
        int ySize = (int) ((maxY - minY + 1) / gridFactor); //ROW

        numAgentsMatrix = new ArrayList<ArrayList<ArrayList<Integer>>>(stepNumber);
        //Initialize grid per step number
        for (int i = 0; i < stepNumber; i++) {
            //Initialize rows
            ArrayList<ArrayList<Integer>> Matrix = new ArrayList<ArrayList<Integer>>(ySize);
            for (int j = 0; j < ySize; j++) {
                Matrix.add(j, new ArrayList());
                for (int k = 0; k < xSize; k++) {
                    Matrix.get(j).add(0);
                }
            }
            numAgentsMatrix.add(Matrix);
        }

        for (int i = 0; i < stepNumber; i++) {
            for (int j = 0; j < positionListForTimeStep.get(i).size(); j++) {
                int xGrid = (int) ((positionListForTimeStep.get(i).get(j).getX() - minX - 1) / gridFactor);
                int yGrid = (int) ((positionListForTimeStep.get(i).get(j).getY() - minY - 1) / gridFactor);
                if (yGrid < 0 || xGrid >= xSize || yGrid >= ySize || xGrid <0 ) {
                    continue;
                }
                numAgentsMatrix.get(i).get(yGrid).set(xGrid, numAgentsMatrix.get(i).get(yGrid).get(xGrid).intValue() + 1);

            }
        }
    }

    /* writing the file as a matrix at every time stamp */
    public void writeMatrixToFile() {

        String fileName = "matrix_" + (new Date().getTime()) + ".csv";

        File file = new File(fileName);
        System.out.println("Creating " + fileName);



        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));


            System.out.println("Start writing " + stepNumber + " entries");

            writer.write("Matrix started\n", 0, "Matrix started\n".length());
            writer.write(Integer.toString(stepNumber), 0, Integer.toString(stepNumber).length());
            writer.write("\n", 0, "\n".length());

            for (int i = 0; i < stepNumber; i++) {
                System.out.println("Index:" + i);

                writer.write("timestamp", 0, "timestamp".length());
                writer.write(Integer.toString(i), 0, Integer.toString(i).length());

                writer.write("\n", 0, "\n".length());
                for (int j = 0; j < numAgentsMatrix.get(i).size(); j++) {
                    for (int k = 0; k < numAgentsMatrix.get(i).get(j).size(); k++) {
                        writer.write(numAgentsMatrix.get(i).get(j).get(k).toString(), 0, numAgentsMatrix.get(i).get(j).get(k).toString().length());
                        writer.write(",", 0, ",".length());
                    }
                    writer.write("\n", 0, "\n".length());
                }
                writer.write("\n", 0, "\n".length());
            }
            writer.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Creating matrix successful");
    }

    /*
     public void writeMatrixToFileNew() {

     int heatMapFactor = 10;

     String fileName = "matrixNEW.csv";
     File file = new File(fileName);
     System.out.println("Creating " + fileName);

     try {
     BufferedWriter writer = new BufferedWriter(new FileWriter(file));



     System.out.println("Start writing " + stepNumber + " entries");

     writer.write("Matrix started\n", 0, "Matrix started\n".length());

     //Average Heatmap that would be output
     ArrayList<ArrayList<ArrayList<Double>>> aveHeatMap = new ArrayList<ArrayList<ArrayList<Double>>>(stepNumber / heatMapFactor);

     //loop in blocks depending on heatmap factor
     for (int timeBlock = 0; timeBlock < stepNumber / heatMapFactor; timeBlock++) {
     int i;
     // creating the 2d list per time block
     ArrayList<ArrayList<Double>> newMatrix = new ArrayList<ArrayList<Double>>();
     //loop within the timeblock 0-9, 10-19, etc.
     for (i = timeBlock * heatMapFactor; i < (timeBlock + 1) * heatMapFactor && i < stepNumber; i++) {

     System.out.println("Index:" + i);

     for (int j = 0; j < numAgentsMatrix.get(i).size(); j++) {
     if (newMatrix.size() <= j) {
     newMatrix.add(j, new ArrayList<Double>());  //create a list in each row
     }
     System.out.println("j loop " + i);
     for (int k = 0; k < numAgentsMatrix.get(i).get(j).size(); k++) {

     if (newMatrix.get(j).size() <= k) {     // initialise cells to 0
     newMatrix.get(j).add(k, new Double(0));
     }

     newMatrix.get(j).set(k, newMatrix.get(j).get(k) + numAgentsMatrix.get(i).get(j).get(k));
     System.out.println("k loop " + i);
     }
     }
     }
     aveHeatMap.add(newMatrix);
     for (int j = 0; j < numAgentsMatrix.get(i).size(); j++) {
     for (int k = 0; k < numAgentsMatrix.get(i).get(j).size(); k++) {
     aveHeatMap.get(timeBlock).get(j).set(k, aveHeatMap.get(timeBlock).get(j).get(k) / (heatMapFactor));   //take the average per block
     }
     }
     }

     //write to file
     for (int i = 0; i < aveHeatMap.size(); i++) {
     writer.write("timestamp", 0, "timestamp".length());
     writer.write(Integer.toString(i * heatMapFactor), 0, Integer.toString(i * heatMapFactor).length());
     writer.write("-", 0, "-".length());
     writer.write(Integer.toString((i + 1) * heatMapFactor - 1), 0, Integer.toString((i + 1) * heatMapFactor - 1).length());

     writer.write("\n", 0, "\n".length());
     for (int j = 0; j < aveHeatMap.get(i).size(); j++) {
     for (int k = 0; k < aveHeatMap.get(i).get(j).size(); k++) {
     writer.write(aveHeatMap.get(i).get(j).get(k).toString(), 0, aveHeatMap.get(i).get(j).get(k).toString().length());
     writer.write(",", 0, ",".length());
     }
     writer.write("\n", 0, "\n".length());
     }
     writer.write("\n", 0, "\n".length());
     }

     writer.close();
     } catch (IOException ex) {
     ex.printStackTrace();
     }
     System.out.println("Creating matrix successful");
     }
     */
    /* counts the flow rate = no. of agents in a timeblock of factor */
    public void createFlowRate() {
        int factor = 10;
        double counter;


        for (int timeBlock = 0; timeBlock < stepNumber / factor; timeBlock++) {

            //counter = numExitArray.get((timeBlock+1)*factor-1);

            if (timeBlock != 0) {
                counter = numExitArray.get((timeBlock + 1) * factor - 1) - numExitArray.get(timeBlock * factor - 1);
            } else {
                counter = numExitArray.get((timeBlock + 1) * factor - 1);
            }
            counter = counter / (factor);

            FlowRateArray.add(timeBlock, counter);
        }
    }

    /* write the flow rate to file */
    public void writeFlowRateToFile() {

        String fileName = "FlowRate_" + (new Date().getTime()) + ".csv";
        File file = new File(fileName);
        System.out.println("Creating " + fileName);


        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < FlowRateArray.size(); i++) {
                writer.write(FlowRateArray.get(i).toString(), 0, FlowRateArray.get(i).toString().length());
                writer.write("\n", 0, "\n".length());
            }
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println(fileName + " Successful!");
    }

    public void writeTimeTakenAgent() {
        String fileName = "TimeTaken_" + (new Date().getTime()) + ".csv";
        File file = new File(fileName);
        System.out.println("Creating " + fileName);
        double average = 0;

        for (int key : TotalTimeTaken.keySet()) {
            average += TotalTimeTaken.get(key);
        }

        average /= TotalTimeTaken.size();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("Total time taken:" + "," + endTime + "\n", 0, ("Total time taken:" + "," + endTime + "\n").length());
            writer.write("Average time taken:" + "," + average + "\n", 0, ("Average time taken:" + "," + average + "\n").length());
            for (int key : TotalTimeTaken.keySet()) {
                writer.write(new Integer(key).toString(), 0, new Integer(key).toString().length());
                writer.write(",", 0, 1);
                writer.write(TotalTimeTaken.get(key).toString(), 0, TotalTimeTaken.get(key).toString().length());
                writer.write("\n", 0, "\n".length());
            }
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println(fileName + " Successful!");

    }

    public void writeDistanceTravelled() {

        String fileName = "DistanceTravelled_" + (new Date().getTime()) + ".csv";
        File file = new File(fileName);
        System.out.println("Creating " + fileName);


        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (int key : DistanceTravelled.keySet()) {
                writer.write(new Integer(key).toString(), 0, new Integer(key).toString().length());
                writer.write(",", 0, 1);
                writer.write(DistanceTravelled.get(key).toString(), 0, DistanceTravelled.get(key).toString().length());
                writer.write("\n", 0, "\n".length());
            }
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
