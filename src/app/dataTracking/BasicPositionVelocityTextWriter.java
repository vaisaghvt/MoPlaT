/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.dataTracking;

import agent.RVOAgent;
import app.PropertySet;
import app.RVOModel;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.io.Files;
import device.Device;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point2d;
import javax.vecmath.Tuple2d;
import javax.vecmath.Vector2d;
import org.jfree.chart.JFreeChart;
import sim.engine.SimState;

/**
 *
 * @author vaisaghvt
 */
public class BasicPositionVelocityTextWriter implements DataTracker {

    private static int numberOfAgents;
    private final RVOModel model;
    private int stepNumber;
    public static final String TRACKER_TYPE = "PosVelText";
    private final static int NUMBER_TO_COLLECT = -1;
//    private final ArrayListMultimap<Integer, Double> energySpentByAgent;
    private final ArrayListMultimap<Integer, Vector2d> velocityListForTimeStep;
    private final ArrayListMultimap<Integer, Point2d> positionListForTimeStep;
 private final ArrayListMultimap<Integer, Vector2d> prefVelocityListForTimeStep;

    public BasicPositionVelocityTextWriter(RVOModel model, Collection<? extends RVOAgent> agents) {
        stepNumber = 0;
        this.model = model;

        velocityListForTimeStep = ArrayListMultimap.create();
        positionListForTimeStep = ArrayListMultimap.create();
        prefVelocityListForTimeStep = ArrayListMultimap.create();

        numberOfAgents = model.getAgentList().size();
    }

    @Override
    public void step(SimState ss) {
        for (RVOAgent agent : model.getAgentList()) {
            velocityListForTimeStep.put(stepNumber, agent.getVelocity());
            prefVelocityListForTimeStep.put(stepNumber, agent.getVelocity());

            //            System.out.println(agent.getVelocity());
            positionListForTimeStep.put(stepNumber, agent.getCurrentPosition());
        }
        stepNumber++;
    }

    @Override
    public String trackerType() {
        return TRACKER_TYPE;
    }

    @Override
    public void storeToFile() {

        String currentFolder = "data"
                + File.separatorChar + this.trackerType()
                + File.separatorChar + model.getScenarioName()
                + File.separatorChar + PropertySet.USECLUSTERING
                + File.separatorChar + RVOModel.publicInstance.seed()
                + File.separatorChar;

        String testFile = currentFolder + "test";
        try {
            Files.createParentDirs(new File(testFile));
        } catch (IOException ex) {
            Logger.getLogger(BasicPositionVelocityTextWriter.class.getName()).log(Level.SEVERE, null, ex);
        }



        try {
            writeToFileAgentTuple2dList(currentFolder + model.getScenarioName() + "_" + RVOModel.publicInstance.seed() +  "_" + "Velocity", velocityListForTimeStep);
            writeToFileAgentTuple2dList(currentFolder + model.getScenarioName() + "_" + RVOModel.publicInstance.seed() + "_" + "Position", positionListForTimeStep);
            writeToFileAgentTuple2dList(currentFolder + model.getScenarioName() + "_" + RVOModel.publicInstance.seed() +  "_" + "PrefVelocity", prefVelocityListForTimeStep);
        } catch (IOException ex) {
            Logger.getLogger(BasicPositionVelocityTextWriter.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
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

    private static <E extends Tuple2d> void writeToFileAgentTuple2dList(String fileName, ArrayListMultimap<Integer, E> dataForAgent) throws IOException {
        System.out.println("Creating " + fileName);
        File file = new File(fileName);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        int writeTime;
        if (NUMBER_TO_COLLECT > 0) {
            writeTime = dataForAgent.keySet().size() / NUMBER_TO_COLLECT;
            //Write integer TimeSteps
            writer.println("NumberOfTimeSteps: "+NUMBER_TO_COLLECT);
        } else if (NUMBER_TO_COLLECT == -1) {
            writeTime = 1;
            writer.println("NumberOfTimeSteps: "+dataForAgent.keySet().size());
        } else {
            assert false;
        }

//write integerNumberOfAgents
        writer.println("NumberOfAgents: "+numberOfAgents);
        NumberFormat doubleFormat = new DecimalFormat("######.000");
        for (Integer timeStep : new TreeSet<Integer>(dataForAgent.keySet())) {
            if (timeStep % (writeTime) == 0) {

//                System.out.println(timeStep);


                for (E element : dataForAgent.get(timeStep)) {
                    if (element != null) {
                        writer.print("("+doubleFormat.format(element.getX())+","+doubleFormat.format(element.getY())+")");
                    } else {
                        writer.print("(0,0)");
                    }
                    writer.print('\t');
                }
                writer.println();
            }
        }
        System.out.println("done");
        writer.close();
    }

   
}
