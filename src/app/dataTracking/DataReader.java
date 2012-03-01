/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.dataTracking;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 *
 * @author vaisagh
 */
public class DataReader {

    public enum FILE_TYPE {

        LATTICE,
        FLOAT;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("usage: DataReader TYPE files");
            System.exit(1);
        }
        PrintWriter writer;
        if (args[0].equalsIgnoreCase(FILE_TYPE.LATTICE.toString())) {
            for (int i = 1; i < args.length; i++) {
                File inputFIle = new File(args[i]);
                if (!inputFIle.exists()) {
                    System.out.println("Invalid file!" + inputFIle + " \n Usage: DataReader TYPE files");
                    System.exit(1);
                }
                DataInputStream reader = null;

                try {
                    reader = new DataInputStream(new FileInputStream(inputFIle));


                    File outputFile = new File(args[i] + ".txt");


                    writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
                    int numberOfTimeSteps = reader.readInt();
                    System.out.println("time Steps =" + numberOfTimeSteps);
                    int latticeWidth = reader.readInt();
                    System.out.println("width =" + latticeWidth);
                    int latticeHeight = reader.readInt();
                    System.out.println("length =" + latticeHeight);
                    for (int x = 0; x < numberOfTimeSteps; x++) {
                        for (int y = 0; y < latticeWidth; y++) {
                            for (int z = 0; z < latticeHeight; z++) {
//                                System.out.println("Here");
                                writer.print(reader.readByte());
                                if (y != latticeWidth - 1) {
                                    writer.print(",");
                                }
                            }
                            writer.println();
                        }
                        writer.println();
                    }

                    writer.close();
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }


            }
        } else if (args[0].equalsIgnoreCase(FILE_TYPE.FLOAT.toString())) {
            for (int i = 1; i < args.length; i++) {
                File inputFIle = new File(args[i]);
                if (!inputFIle.exists()) {
                    System.out.println("Invalid file!" + inputFIle + " \n Usage: DataReader TYPE files");
                    System.exit(1);
                }
                DataInputStream reader = null;

                try {
                    reader = new DataInputStream(new FileInputStream(inputFIle));


                    File outputFile = new File(args[i] + ".txt");


                    writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
                    int numberOfTimeSteps = reader.readInt();
                    int numberOfAgents = reader.readInt();

                    for (int x = 0; x < numberOfTimeSteps; x++) {
                        for (int y = 0; y < numberOfAgents; y++) {

                            writer.print(reader.readFloat());
                            if (y != numberOfAgents - 1) {
                                writer.print(",");
                            }

                        }
                        writer.println();
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }


            }
        } else {
            System.out.println("TYPE =" + Arrays.toString(FILE_TYPE.values()));
            System.exit(1);
        }
    }
}