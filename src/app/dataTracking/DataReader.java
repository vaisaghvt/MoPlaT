/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.dataTracking;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * 
 * REads the data written as binary files and converts them to text files that
 * can later be read in matlab using one of the scripts below.
 * @author vaisagh
 */
public class DataReader {

    private static Boolean convertLatticeFile(File inputFile) {
        PrintWriter writer;

        
        if (!inputFile.exists()) {
            System.out.println("Invalid file!" + inputFile);
            return false;
        }


        try {
            DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));


            File outputFile = new File(inputFile.getAbsoluteFile() + ".txt");


            writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
            int numberOfTimeSteps = reader.readInt();
//            System.out.println("time Steps =" + numberOfTimeSteps);
//                    int latticeWidth = reader.readInt();
//                    System.out.println("width =" + latticeWidth);
//                    int latticeHeight = reader.readInt();
//                    System.out.println("length =" + latticeHeight);
            for (int x = 0; x < numberOfTimeSteps; x++) {
                int numberOfAgents = reader.readInt();
                writer.println(numberOfAgents);
                for (int z = 0; z < numberOfAgents; z++) {
//                                System.out.println("Here");
                    writer.println(reader.readInt() + "," + reader.readInt());
                }
//                            writer.println();
//                        }
//                        writer.println();
            }
            writer.close();
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return true;

    }

    private static Boolean convertFloatFile(File inputFile) {
        PrintWriter writer;

     
        if (!inputFile.exists()) { // No point in checking next file since one 
            //error most probably means there are many more errors
            System.out.println("Invalid file!" + inputFile);
            return false;
        }


        try {
            DataInputStream reader = new DataInputStream(new FileInputStream(inputFile));


            File outputFile = new File(inputFile.getAbsoluteFile() + ".txt");

//            System.out.println(inputFile);
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));

//            System.out.println(reader.available());

            int numberOfTimeSteps = reader.readInt();
//            System.out.println("steps=" + numberOfTimeSteps);
            writer.println(numberOfTimeSteps);
            int numberOfAgents = reader.readInt();
//            System.out.println("agents" + numberOfAgents);

            for (int x = 0; x < numberOfTimeSteps; x++) {

                for (int y = 0; y < numberOfAgents; y++) {

                    writer.print(reader.readFloat());
                    if (y != numberOfAgents - 1) {
                        writer.print(",");
                    }

//                    System.out.println(x + "," + y);
                }
                writer.println();
            }
            writer.close();
            reader.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.print("*");
        return true;
    }

    private static List<File> findAllFile(String[] args) {
        if (args.length < 2) {
            System.out.println("you used: DataReader" + Arrays.toString(args));
            System.out.println("actual usage: DataReader TYPE files");
            System.exit(1);
        }
        final FILE_TYPE type;
        if (args[0].equalsIgnoreCase(FILE_TYPE.LATTICE.toString())) {
            type = FILE_TYPE.LATTICE;
        } else if (args[0].equalsIgnoreCase(FILE_TYPE.FLOAT.toString())) {
            type = FILE_TYPE.FLOAT;
        } else {
            type = null;
            System.out.println("TYPE =" + Arrays.toString(FILE_TYPE.values()));
            System.exit(1);
        }



        File dir = new File(args[1]);
        if (!dir.exists()) {
            
            System.out.println(dir.getAbsolutePath()+" doesn't exist!");
            System.exit(1);
        }
        List<File> listOfFiles = processFilesInDirectory(dir, type);

//        for (File file : listOfFiles) {
//            System.out.println(file.getName());
//        }
        return listOfFiles;
    }
    
        private static List<File> processFilesInDirectory(File dir, FILE_TYPE type) {
            System.out.println(dir.getAbsoluteFile());
            List<File> listOfFiles= new ArrayList<File>();
            recurseDirectory(dir, type, listOfFiles);
            return listOfFiles;
        
    }

    private static boolean satisfiesType(File child, FILE_TYPE type) {
        String fileName = child.getName();
//        System.out.println(child.getAbsoluteFile());
        if (fileName.contains(".txt")) {
           
            return false;
        
        }
        if (type == FILE_TYPE.FLOAT && (fileName.contains("Position") || fileName.contains("Velocity"))) {
            return true;
        } else if (type == FILE_TYPE.LATTICE && fileName.contains("Lattice")) {
            return true;
        }
        return false;
    }

    private static void recurseDirectory(File dir, FILE_TYPE type, List<File> listOfFiles) {
        for (File child : dir.listFiles()) {
            if (child.isFile() && satisfiesType(child, type)) {
                listOfFiles.add(child);
            } else if (child.isDirectory()) {
             //   System.out.println("Recursing through"+ child.getAbsolutePath());
                recurseDirectory(child, type, listOfFiles);
            }

        }
        
    }

    public enum FILE_TYPE {

        LATTICE,
        FLOAT;
    }

    public static void main(String[] args) {
        List<File> listOfFiles = findAllFile(args);
        System.out.println("Trying to convert "+listOfFiles.size()+ " files");
        final FILE_TYPE type;
        if (args[0].equalsIgnoreCase(FILE_TYPE.LATTICE.toString())) {
            type = FILE_TYPE.LATTICE;
        } else if (args[0].equalsIgnoreCase(FILE_TYPE.FLOAT.toString())) {
            type = FILE_TYPE.FLOAT;
        } else {
            type = null;
            System.out.println("TYPE =" + Arrays.toString(FILE_TYPE.values()));
            System.exit(1);
        }

        final ExecutorService threadPool = Executors.newCachedThreadPool();
        //        this.threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


        List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();

        System.out.print("Submitted:");
        for (File fileName : listOfFiles) {

//			final CollisionAvoidanceParameters syncParam = createSyncedCollisionAvoidanceParameters(param);
            final File finalFile = fileName;
//            System.out.println("adding file "+fileName);
            System.out.print("*");
            tasks.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    // TODO Auto-generated method stub
                    if (type == FILE_TYPE.LATTICE) {
                        return convertLatticeFile(finalFile);
                    } else {
                        return convertFloatFile(finalFile);
                    }
                }
            });
        }
        System.out.print("\nCompleted:");
        List<Future<Boolean>> futures;
        try {
            futures = threadPool.invokeAll(tasks);
            for (Future<Boolean> future : futures) {
                
                if (future.get() == false) {
                    System.out.println("Conversion Failed");
                    threadPool.shutdown();
                    System.exit(1);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }






        System.out.println("Conversion Sucessful");
        threadPool.shutdown();
    }
}
