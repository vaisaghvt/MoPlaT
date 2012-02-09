/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.dataTracking;

import agent.RVOAgent;
import app.PropertySet;
import app.RVOModel;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import javax.vecmath.Point2d;
import javax.vecmath.Tuple2d;
import javax.vecmath.Vector2d;
import org.jfree.chart.JFreeChart;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import sim.engine.SimState;

/**
 *
 * @author vaisaghvt
 */
public class PhysicaDataTracker implements DataTracker {

    private final RVOModel model;
    private int stepNumber;
    public static final float E_S = 2.23f;
    public static final float E_W = 1.26f;
    public static final String TRACKER_TYPE = "Physica";
    private final static int NUMBER_OF_DATA_TO_COLLECT = -1;
    private final ArrayListMultimap<RVOAgent, Double> energySpentByAgent;
    private final ArrayListMultimap<RVOAgent, Vector2d> velocityListForAgent;
    private final ArrayListMultimap<RVOAgent, Point2d> positionListForAgent;

    public PhysicaDataTracker(RVOModel model, Collection<? extends RVOAgent> agents) {
        stepNumber = 0;
        this.model = model;

        energySpentByAgent = ArrayListMultimap.create();
        velocityListForAgent = ArrayListMultimap.create();
        positionListForAgent = ArrayListMultimap.create();



    }

    @Override
    public void step(SimState ss) {

        for (RVOAgent agent : model.getAgentList()) {

            velocityListForAgent.put(agent, agent.getVelocity());

            positionListForAgent.put(agent, agent.getCurrentPosition());


            double energyInCurrentTimeStep = agent.getMass() * (E_S + (E_W * agent.getVelocity().lengthSquared())) * PropertySet.TIMESTEP;
            double energySoFar = 0;
            if (stepNumber > 0) {
                energySoFar = energySpentByAgent.get(agent).get(stepNumber - 1);
            }
            energySpentByAgent.put(agent, (energySoFar + energyInCurrentTimeStep));
        }

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

                writeToFileAgentTuple2dList(currentFolder + "Velocity", velocityListForAgent);
                writeToFileAgentTuple2dList(currentFolder + "Position", positionListForAgent);
                writeToFileAgentNumberList(currentFolder + "Energy", energySpentByAgent);



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

    @Override
    public boolean hasChart() {
        return false;
    }

    @Override
    public JFreeChart getChart() {
        return null;
    }

    private static <E extends Tuple2d> void writeToFileAgentTuple2dList(String fileName, ArrayListMultimap<RVOAgent, E> dataForAgent) {
        File fileX = new File(fileName + "_x");
        File fileY = new File(fileName + "_y");
        PrintWriter writerX = null;
        PrintWriter writerY = null;
        try {
            writerX = new PrintWriter(new BufferedWriter(new FileWriter(fileX)));
            writerY = new PrintWriter(new BufferedWriter(new FileWriter(fileY)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        for (RVOAgent agent : dataForAgent.keySet()) {

            int writeTime;
            if (NUMBER_OF_DATA_TO_COLLECT > 0) {
                writeTime = dataForAgent.get(agent).size() / NUMBER_OF_DATA_TO_COLLECT;
            } else if (NUMBER_OF_DATA_TO_COLLECT == -1) {
                writeTime = 1;
            } else {
                assert false;
            }
            writerX.print("Agent " + agent.getId());
            writerY.print("Agent " + agent.getId());
            int i = 1;
            for (E element : dataForAgent.get(agent)) {
                if (i % (writeTime) == 0) {

                    writerX.print("\t" + element);
                    writerY.print("\t" + element);
                }
                i++;
            }
            writerX.println();
            writerY.println();
        }
        writerX.close();
        writerY.close();
    }

    private static <E extends Number> void writeToFileAgentNumberList(String fileName, Multimap<RVOAgent, E> dataForAgent) {
        File file = new File(fileName);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        for (RVOAgent agent : dataForAgent.keySet()) {

            int writeTime;
            if (NUMBER_OF_DATA_TO_COLLECT > 0) {
                writeTime = dataForAgent.get(agent).size() / NUMBER_OF_DATA_TO_COLLECT;
            } else if (NUMBER_OF_DATA_TO_COLLECT == -1) {
                writeTime = 1;
            } else {
                assert false;
            }
            writer.print("Agent " + agent.getId());
            int i = 1;
            for (E element : dataForAgent.get(agent)) {
                if (i % (writeTime) == 0) {

                    writer.print("\t" + element);
                }
                i++;
            }
            writer.println();
        }
        writer.close();
    }
}
