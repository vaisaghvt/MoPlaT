/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.dataTracking;

import agent.RVOAgent;
import app.PropertySet;
import app.RVOModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.vecmath.Vector2d;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import sim.engine.SimState;
import utility.Geometry;
import utility.PrecisePoint;

/**
 *
 * @author hunan
 */
public class PBMDataTracker implements DataTracker {

    private HashMap<RVOAgent, ArrayList<Double>> speedListForAgent;
    private HashMap<RVOAgent, ArrayList<Double>> cumulativeDistanceForAgent;
    private HashMap<RVOAgent, ArrayList<Double>> energySpentByAgent;
    private HashMap<RVOAgent, ArrayList<Integer>> inconveniencesForAgent;
    private HashMap<RVOAgent, PrecisePoint> startPositionForAgent;
    private HashMap<RVOAgent, Double> displacementToGoalForAgent;
    
    private final RVOModel model;
    private int stepNumber;
    public static final float E_S = 2.23f;
    public static final float E_W = 1.26f;
    public static final String TRACKER_TYPE = "pbmText";
    private ArrayList<Double> totalEnergyList;
    private ArrayList<Double> totalDistanceList;
    private ArrayList<Integer> totalInconvenienceList;
    
    private final XYSeries energySeries;
    private final XYSeries distanceSeries;
    private final JFreeChart chart;
    private final XYSeries inconvenienceSeries;
    private final static int NUMBER_OF_DATA_TO_COLLECT=1;

    public PBMDataTracker(RVOModel model, Collection<? extends RVOAgent> agents) {
        stepNumber = 0;
        this.model = model;

        energySpentByAgent = new HashMap<RVOAgent, ArrayList<Double>>();
        speedListForAgent = new HashMap<RVOAgent, ArrayList<Double>>();
        cumulativeDistanceForAgent = new HashMap<RVOAgent, ArrayList<Double>>();
        inconveniencesForAgent = new HashMap<RVOAgent, ArrayList<Integer>>();
        startPositionForAgent = new HashMap<RVOAgent, PrecisePoint> ();
        displacementToGoalForAgent = new HashMap<RVOAgent, Double> ();

        totalDistanceList = new ArrayList<Double>();
        totalEnergyList = new ArrayList<Double>();
        totalInconvenienceList = new ArrayList<Integer>();
        for (RVOAgent agent : agents) {
            cumulativeDistanceForAgent.put(agent, new ArrayList<Double>());
            speedListForAgent.put(agent, new ArrayList<Double>());
            energySpentByAgent.put(agent, new ArrayList<Double>());
            inconveniencesForAgent.put(agent, new ArrayList<Integer>());
            startPositionForAgent.put(agent, new PrecisePoint(agent.getCurrentPosition().x, agent.getCurrentPosition().y));
        }

        energySeries = new XYSeries("EnergyGraph");
        distanceSeries = new XYSeries("DistanceGraph");
        inconvenienceSeries = new XYSeries("Inconvenience Graph");

        XYSeriesCollection dataset = new XYSeriesCollection();
//        dataset.addSeries(energySeries);
        dataset.addSeries(inconvenienceSeries);
//        dataset.addSeries(distanceSeries);
        chart = ChartFactory.createXYLineChart("Simulation Chart", // Title
                "step Number", // x-axis Label
                "value", // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
                );

    }

    @Override
    public void step(SimState ss) {
        double totalEnergy = 0, totalDistanceTravelled = 0;
        int countTotalInconvenience = 0, earlierInconvenienceCost = 0;
        for (RVOAgent agent : model.getAgentList()) {
            speedListForAgent.get(agent).add(agent.getVelocity().length());

            double distanceInCurrentTimeStep = agent.getVelocity().length()
                    * PropertySet.TIMESTEP;
            totalDistanceTravelled += distanceInCurrentTimeStep;
            double distanceSoFar = 0;
            if (stepNumber > 0) {
                distanceSoFar = cumulativeDistanceForAgent.get(agent).get(stepNumber - 1);
            }
            cumulativeDistanceForAgent.get(agent).add(distanceSoFar + distanceInCurrentTimeStep);

            double energyInCurrentTimeStep = agent.getMass() * (E_S + (E_W * agent.getVelocity().lengthSquared())) * PropertySet.TIMESTEP;
            double energySoFar = 0;
            if (stepNumber > 0) {
                energySoFar = energySpentByAgent.get(agent).get(stepNumber - 1);
            }
            energySpentByAgent.get(agent).add((energySoFar + energyInCurrentTimeStep));
            totalEnergy += energyInCurrentTimeStep;
            
            Vector2d distanceToGoalVector = new Vector2d(startPositionForAgent.get(agent).toVector());
            distanceToGoalVector.sub(agent.getGoal());
            Vector2d vectorToCurrentPosition = new Vector2d(startPositionForAgent.get(agent).toVector());
            vectorToCurrentPosition.sub(agent.getCurrentPosition());
            double displacementToGoal = vectorToCurrentPosition.dot(distanceToGoalVector);
            displacementToGoalForAgent.put(agent, displacementToGoal);
            
            
            
            

            Vector2d relativeVelocity = new Vector2d(agent.getPrefVelocity());
            relativeVelocity.sub(agent.getVelocity());

            earlierInconvenienceCost = 0;
            if (stepNumber > 0) {
                earlierInconvenienceCost = inconveniencesForAgent.get(agent).get(stepNumber - 1);
            }
            if (relativeVelocity.length() > Geometry.EPSILON) {
                countTotalInconvenience++;
                inconveniencesForAgent.get(agent).add(earlierInconvenienceCost + 1);
            } else {
                inconveniencesForAgent.get(agent).add(earlierInconvenienceCost);
            }
        }
        earlierInconvenienceCost = 0;
        if (stepNumber > 0) {
            earlierInconvenienceCost = totalInconvenienceList.get(stepNumber - 1);
        }

        totalInconvenienceList.add(earlierInconvenienceCost + countTotalInconvenience);
        inconvenienceSeries.add(stepNumber, earlierInconvenienceCost + countTotalInconvenience);

        totalDistanceList.add(totalDistanceTravelled);
        distanceSeries.add(stepNumber, totalDistanceTravelled);

        totalEnergyList.add(totalEnergy);
        energySeries.add(stepNumber, totalEnergy);

        stepNumber++;
    }

    @Override
    public String trackerType() {
        return TRACKER_TYPE;
    }

    @Override
    public void storeToFile() {
 
        LocalDate date = new LocalDate();
        LocalTime time = new LocalTime();

        String dateString = date.toString("dd-MMM-yyyy");
        String timeString = time.toString("HH_mm_ss_sss");
        String currentFolder = "data" + File.separatorChar;

        File directory = new File(currentFolder + TRACKER_TYPE);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                System.out.println("Type Directory could not be created for " + directory);
            }
        }
        if (directory.exists()) {
            currentFolder = currentFolder + TRACKER_TYPE + File.separatorChar;

            directory = new File(currentFolder + dateString);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    System.out.println("Date Directory could not be created for " + directory);
                }
            }
            if (directory.exists()) {
                currentFolder = currentFolder + dateString + File.separatorChar;
            }
            directory = new File(currentFolder + timeString);

            if (!directory.mkdir()) {
                System.out.println("Time Directory could not be created for " + directory);
            }
            if (directory.exists()) {
                currentFolder = currentFolder + timeString + File.separatorChar;

                for (RVOAgent agent : energySpentByAgent.keySet()) {
                    double energy = energySpentByAgent.get(agent).get(energySpentByAgent.get(agent).size()-1)/displacementToGoalForAgent.get(agent);
                    energySpentByAgent.get(agent).remove(energySpentByAgent.get(agent).size()-1);
                    energySpentByAgent.get(agent).add(energy);
                }

                writeToFileAgentNumberList(currentFolder + "Speed", speedListForAgent);
                writeToFileAgentNumberList(currentFolder + "Distance", cumulativeDistanceForAgent);
                writeToFileAgentNumberList(currentFolder + "Energy", energySpentByAgent);
                writeToFileAgentNumberList(currentFolder + "Inconveniences", this.inconveniencesForAgent);


                PropertySet.writePropertiesToFile(currentFolder + "properties");

//                try {
//                    ChartUtilities.saveChartAsJPEG(new File(currentFolder + "chart.jpg"), chart, 500,
//                            300);
//                } catch (IOException e) {
//                    System.err.println("Problem occurred creating chart.");
//                }
            }
        }


    }

    public static <E extends Number> void writeToFileAgentNumberList(String fileName, HashMap<RVOAgent, ArrayList<E>> dataForAgent) {
        File file = new File(fileName);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        for (RVOAgent agent : dataForAgent.keySet()) {
            int writeTime = dataForAgent.get(agent).size()/NUMBER_OF_DATA_TO_COLLECT;
            writer.print("Agent " + agent.getId());
            int i=1;
            for (E element : dataForAgent.get(agent)) {
                if(i%(writeTime)==0){       
                    
                    writer.print("\t" + element);
                }
                i++;
            }
            writer.println();
        }
        writer.close();
    }

    @Override
    public boolean hasChart() {
        if(this.chart!=null){
            return true;
        }
        return false;
    }

    @Override
    public JFreeChart getChart() {
        return this.chart;
    }
}
