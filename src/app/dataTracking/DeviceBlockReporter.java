/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.dataTracking;

/**
 *
 * @author angela
 */
import java.util.Scanner;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.List;

public class DeviceBlockReporter {

    /* this matrix is used to store blocks (e.g. 10) of timestamp records for summary analysis */
    private static ArrayList<ArrayList<ArrayList<Integer>>> numAgentsMatrix;
    private static ArrayList<ArrayList<ArrayList<Double>>> aveHeatMap;
    private static int heatMapFactor = 10;

    /* this gets the step number from the csv file. The step number shows the total number of timestamp records */
    public static int getStepNumber(Scanner srcConfig) {
        int stepNumber = 0;
        String inStr;
        StringTokenizer str;
        inStr = srcConfig.nextLine();
        if (inStr.startsWith("Matrix started")) { //right file, the next line is the timeStamp
            inStr = srcConfig.nextLine();
            str = new StringTokenizer(inStr, ",");
            stepNumber = Integer.parseInt(str.nextToken()); // stepnumber is next line after matrix started line
            // stepNumber = srcConfig.nextInt(); //get it
        }
        return stepNumber;
    }

    /* prints out the numAgentsMatrix for debugging purpose */
    public static void printNumAgents() {
        for (int i = 0; i < numAgentsMatrix.size(); i++) {
            for (int j = 0; j < numAgentsMatrix.get(i).size(); j++) {
                for (int k = 0; k < numAgentsMatrix.get(i).get(j).size(); k++) {
                    System.out.print(numAgentsMatrix.get(i).get(j).get(k) + " ");
                }
                System.out.println();
            }
        }
    }
    /* this routine gets records for a particular timestamp and stores them into numAgentsMatrix */
    /* the records need not be a square matrix, e.g. can be 27 x 26 or 16 x 26 etc   */

    public static void getNextBlockMatrix(Scanner srcConfig) {
        String tmStmp, inStr;
        StringTokenizer str;
        do {//skip all the blank lines
            tmStmp = srcConfig.nextLine();
            //System.out.println(tmStmp);
        } while (tmStmp.length() == 0 || tmStmp.startsWith(",,,,"));
        if (tmStmp.startsWith("timestamp")) { //get to a timestamp indicator
            inStr = srcConfig.nextLine(); //first record with values
            int j = 0, k = 0;
            //newMatrix is used to store 2-d array of values from the timestamp records
            ArrayList<ArrayList<Integer>> newMatrix = new ArrayList<ArrayList<Integer>>();
            while (!inStr.startsWith(",,,,") && inStr.length() != 0) {
                str = new StringTokenizer(inStr, ","); //tokenize the input line and extract each token
                newMatrix.add(j, new ArrayList<Integer>());
                while (str.hasMoreTokens()) {
                    newMatrix.get(j).add(k, Integer.parseInt(str.nextToken())); //put the value into newmatrix
                    //System.out.println(newMatrix.get(j).get(k)+" j="+j+" k="+k);
                    k++; //go to next token
                }
                inStr = srcConfig.nextLine(); //read the next line
                j++;
                k = 0; //next row and reset the k value of next row to prepare for new set of tokens
                //System.out.println(inStr+" j="+j);
            }
            numAgentsMatrix.add(newMatrix); //add the whole block to numAgentsMatrix
            // printNumAgents();
            //System.out.print(numAgentsMatrix.get(blockNumber).get(0).get(0) + "/");
        }
    }
    /* routine pulls out a block of timestamp records (about 10) and stores all into numAgentsMatrix */
    /* The numAgentsMatrix will then be summarized in the routine WriteMatrixToFile                  */

    public static void getAllBlocks(Scanner srcConfig, int blockSize) {
        numAgentsMatrix.clear();
        for (int i = 0; i < blockSize; i++) {
            getNextBlockMatrix(srcConfig);
        }
    }

    public static void readConfig(String fileName, int num) throws FileNotFoundException {
        int stepNumber = 0;


        FileReader fconfig = new FileReader(fileName);
        //BufferedReader brStream = new BufferedReader(fconfig);
        Scanner srcConfig = new Scanner(fconfig);

        stepNumber = getStepNumber(srcConfig);
        //System.out.print("stepNumber=" + stepNumber);
        writeMatrixToFile(stepNumber, srcConfig, num);
    }

    public static void summarizeMatrix(int stepNumber) {

        // creating the 2d list per time block
        ArrayList<ArrayList<Double>> newMatrix = new ArrayList<ArrayList<Double>>();

        //loop within the timeblock 0-9, 10-19, etc.
        for (int i = 0; i < heatMapFactor; i++) {
//                for(i=timeBlock*heatMapFactor; i<(timeBlock+1)*heatMapFactor && i<stepNumber; i++ ){

            System.out.println("Index:" + i + " numAgentsSize=" + numAgentsMatrix.size());
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
                    //System.out.println("k loop " + i);
                }
            }
        }
        aveHeatMap.add(newMatrix);

    }

    public static void writeMatrixToFile(int stepNumber, Scanner srcConfig, int num) {



        String fileName = "matrixBlock" + num + ".csv";
        File file = new File(fileName);
        System.out.println("Creating " + fileName);

        BufferedWriter writer = null;
        numAgentsMatrix = new ArrayList<ArrayList<ArrayList<Integer>>>(heatMapFactor); //create the hold matrix
        //Average Heatmap that would be output
        //ArrayList<ArrayList<ArrayList<Double>>> aveHeatMap = new ArrayList<ArrayList<ArrayList<Double>>>(stepNumber / heatMapFactor);
        aveHeatMap = new ArrayList<ArrayList<ArrayList<Double>>>(stepNumber / heatMapFactor);
        try {
            writer = new BufferedWriter(new FileWriter(file));



            System.out.println("Start writing " + stepNumber + " entries");

            writer.write("Matrix started\n", 0, "Matrix started\n".length());

            for (int timeBlock = 0; timeBlock < stepNumber / heatMapFactor; timeBlock++) {
                int i;
                getAllBlocks(srcConfig, heatMapFactor);
                summarizeMatrix(stepNumber);
                i = 0; //reset otherwise got outofbounds
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

    public static void getBlockDouble(Scanner srcConfig, ArrayList<ArrayList<ArrayList<Double>>> aveHeatMap) {
        String tmStmp, inStr;
        StringTokenizer str;
        do {//skip all the blank lines
            tmStmp = srcConfig.nextLine();
            //System.out.println(tmStmp);
        } while (tmStmp.length() == 0);
        if (tmStmp.startsWith("timestamp")) { //get to a timestamp indicator
            inStr = srcConfig.nextLine(); //first record with values
            int j = 0, k = 0;
            //newMatrix is used to store 2-d array of values from the timestamp records
            ArrayList<ArrayList<Double>> newMatrix = new ArrayList<ArrayList<Double>>();
            while (!inStr.startsWith(",,,,") && inStr.length() != 0) {
                str = new StringTokenizer(inStr, ","); //tokenize the input line and extract each token
                newMatrix.add(j, new ArrayList<Double>());
                while (str.hasMoreTokens()) {
                    newMatrix.get(j).add(k, Double.parseDouble(str.nextToken())); //put the value into newmatrix
                    //System.out.println(newMatrix.get(j).get(k)+" j="+j+" k="+k);
                    k++; //go to next token
                }
                inStr = srcConfig.nextLine(); //read the next line
                j++;
                k = 0; //next row and reset the k value of next row to prepare for new set of tokens
                //System.out.println(inStr+" j="+j);
            }
            aveHeatMap.add(newMatrix); //add the whole block to numAgentsMatrix
            // printNumAgents();
            //System.out.print(numAgentsMatrix.get(blockNumber).get(0).get(0) + "/");
        }
    }

    public static void padHorizontal(ArrayList<ArrayList<ArrayList<Double>>> newMatrix, int from, int to) {
        for (int j = 0; j < newMatrix.size(); j++) {
            for (int k = 0; k < newMatrix.get(j).size(); k++) {
                for (int x = from; x < to; x++) {
                    newMatrix.get(j).get(k).add(x, 0.0);
                }
            }
        }
    }

    public static void padVertical(ArrayList<ArrayList<ArrayList<Double>>> newMatrix, int from, int to, int jDepth) {
        ArrayList<ArrayList<Double>> matrix = new ArrayList<ArrayList<Double>>(to - from);
        System.out.println("matrix size" + newMatrix.size());
        for (int j = 0; j < newMatrix.size(); j++) {
            for (int k = 0; k < newMatrix.get(j).size(); k++) {
                for (int x = 0; x < newMatrix.get(j).get(k).size(); x++) {
                    System.out.print(newMatrix.get(j).get(k).get(x));
                }
                System.out.println();
            }
        }
        for (int i = 0; i < to - from; i++) {
            for (int j = 0; j < jDepth; j++) {
                matrix.add(j, new ArrayList<Double>());
                System.out.println("add " + j);
                for (int k = 0; k < newMatrix.get(0).get(0).size(); k++) {
                    matrix.get(j).add(k, 0.0);
                }
            }
            newMatrix.add(matrix);
        }
        System.out.println("matrix size" + newMatrix.size());
        for (int j = 0; j < newMatrix.size(); j++) {
            for (int k = 0; k < newMatrix.get(j).size(); k++) {
                for (int x = 0; x < newMatrix.get(j).get(k).size(); x++) {
                    System.out.print(newMatrix.get(j).get(k).get(x));
                }
                System.out.println();
            }
        }
        // newMatrix.add(matrix);

    }

    public static void consolidateFiles(int numFiles) throws FileNotFoundException {
        String fileName = "matrixConsol" + ".csv";
        File file = new File(fileName);
        BufferedWriter writer = null;

        ArrayList<ArrayList<ArrayList<Double>>> newMatrix = new ArrayList<ArrayList<ArrayList<Double>>>();
        FileReader fconfig = new FileReader("matrixBlock" + 0 + ".csv");
        //BufferedReader brStream = new BufferedReader(fconfig);
        Scanner srcConfig = new Scanner(fconfig);
        String inStr;
        aveHeatMap.clear();
        inStr = srcConfig.nextLine();
        System.out.println("string " + inStr);
        do { //read in the first file and store in aveHeatMap
            getBlockDouble(srcConfig, aveHeatMap); //value is now double so Matrix need to change
        } while (srcConfig.hasNext());
        System.out.println("size of aveHeat" + aveHeatMap.size());
        for (int i = 1; i < numFiles; i++) {
            fconfig = new FileReader("matrixBlock" + i + ".csv");
            srcConfig = new Scanner(fconfig);
            inStr = srcConfig.nextLine();
            newMatrix.clear();
            do {
                getBlockDouble(srcConfig, newMatrix);
            } while (srcConfig.hasNext());
            System.out.println("size of newMatrix" + newMatrix.size());
            System.out.println("k size " + aveHeatMap.get(0).get(0).size());
            if (aveHeatMap.get(0).get(0).size() < newMatrix.get(0).get(0).size()) {
                padHorizontal(aveHeatMap, aveHeatMap.get(0).get(0).size(), newMatrix.get(0).get(0).size());
                System.out.println("aveHeatMap padded horizontally " + aveHeatMap.get(0).get(0).size());
            } else {
                padHorizontal(newMatrix, newMatrix.get(0).get(0).size(), aveHeatMap.get(0).get(0).size());
            }
            System.out.println("k size " + aveHeatMap.get(0).get(0).size());
            System.out.println("row " + newMatrix.size());
            if (aveHeatMap.size() < newMatrix.size()) {
                padVertical(aveHeatMap, aveHeatMap.size(), newMatrix.size(), aveHeatMap.get(0).size());
            } else {
                padVertical(newMatrix, newMatrix.size(), aveHeatMap.size(), newMatrix.get(0).size());
                System.out.println("newMatrix padded vertically ");
            }

            for (int j = 0; j < aveHeatMap.size(); j++) {
                for (int k = 0; k < aveHeatMap.get(j).size(); k++) {
                    for (int x = 0; x < aveHeatMap.get(j).get(k).size(); x++) {
                       // System.out.println("ave " + j + " " + k + " " + aveHeatMap.size() + " " + aveHeatMap.get(j).size() + " " + aveHeatMap.get(j).get(k).size());
                       // System.out.println("NEW " + j + " " + k + " " + newMatrix.size() + " " + newMatrix.get(j).size() + " " + newMatrix.get(j).get(k).size());
                        aveHeatMap.get(j).get(k).set(x, (aveHeatMap.get(j).get(k).get(x) + newMatrix.get(j).get(k).get(x)) / 2);
                    }
                }
            }
            System.out.println("row " + newMatrix.size());

            try {
                writer = new BufferedWriter(new FileWriter(file));



                System.out.println("Start writing " + " entries");

                writer.write("Matrix started\n", 0, "Matrix started\n".length());
                //write to file
                for (int j = 0; j < aveHeatMap.size(); j++) {
                    writer.write("timestamp", 0, "timestamp".length());
                    writer.write(Integer.toString(j * heatMapFactor), 0, Integer.toString(j * heatMapFactor).length());
                    writer.write("-", 0, "-".length());
                    writer.write(Integer.toString((j + 1) * heatMapFactor - 1), 0, Integer.toString((j + 1) * heatMapFactor - 1).length());

                    writer.write("\n", 0, "\n".length());
                    for (int k = 0; k < aveHeatMap.get(j).size(); k++) {
                        for (int x = 0; x < aveHeatMap.get(j).get(k).size(); x++) {
                            writer.write(aveHeatMap.get(j).get(k).get(x).toString(), 0, aveHeatMap.get(j).get(k).get(x).toString().length());
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
    }

    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
        System.out.println("in deviceReporter" + args[0]);
        //ArrayList<String> fileList = new ArrayList<String>();
        List<File> listOfFiles = DataReader.findAllFile(args);
//        fileList.add("matrix_1378047763014.csv");
//        fileList.add("matrix_1377925338412.csv");
//        fileList.add("matrix_1377925338412.csv");
//        fileList.add("matrix_1377925338412.csv");
        for (int i = 0; i < listOfFiles.size(); i++) {
            readConfig(listOfFiles.get(i).getAbsolutePath(), i);
        }
        consolidateFiles(listOfFiles.size());
    }
}
