/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package environment.creator;

import environment.XMLManager;
import environment.geography.ObjectFactory;
import environment.geography.SimulationScenario;
import java.awt.BorderLayout;
import java.awt.Color;

import java.awt.GridLayout;
import java.awt.Menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.KeyListener;
import java.io.File;

import java.io.FileOutputStream;
import javax.swing.JFileChooser;

import javax.swing.JOptionPane;

import javax.swing.JPanel;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Vaisagh
 */
public class EnvironmentCreatorApp implements ActionListener {

    /**
     * @param args
     */
    private DescriptionArea descriptionArea;
    private InteractionArea interactionArea;
    private AgentDescriptionArea agentArea;
    public JLabel statusBar = new JLabel();
    private SimulationScenario environment;
    private int currentLevel = 0;
    /**
     * level 0 = parameter screen
     * level 1 = obstacle area
     * level 2 = agents
     * level 3 = agent adjustment area
     * level 4 save
     */
    public static final int FINALLEVEL = 5;
    JButton clearButton;
    JButton nextButton;
    JButton previousButton;
    JFrame frame;
    private int xSize;
    private int ySize;
    public static int scale;
    private JFileChooser fileChooser;

    public EnvironmentCreatorApp() {
        environment = (new ObjectFactory()).createSimulationScenario();

        frame = new JFrame("Agent Simulation Environment Creator");
        frame.setLayout(new BorderLayout());

        interactionArea = new InteractionArea(statusBar);
        interactionArea.setBackground(Color.WHITE);
        interactionArea.setLevel(currentLevel);

        descriptionArea = new DescriptionArea(this);

        fileChooser = new JFileChooser(new File(".").getAbsolutePath());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        JPanel buttonArea = new JPanel();
        buttonArea.setLayout(new GridLayout(1, 3));
        buttonArea.setBackground(Color.lightGray);

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        menuBar.add(fileMenu);
        MenuItem load = new MenuItem("Load");
        MenuItem about = new MenuItem("About");
        about.setActionCommand("about");
        load.setActionCommand("load");
        fileMenu.add(about);
        fileMenu.add(load);
        about.addActionListener(this);
        load.addActionListener(this);
        frame.setMenuBar(menuBar);

        clearButton = new JButton("Clear");
        nextButton = new JButton("Next");
        previousButton = new JButton("Previous");
        previousButton.setEnabled(false);
        clearButton.setEnabled(false);

        buttonArea.add(previousButton);
        buttonArea.add(clearButton);
        buttonArea.add(nextButton);

        previousButton.setToolTipText("Move to previous step");
        nextButton.setToolTipText("Move to next step");
        clearButton.setToolTipText("Clear all points on screen");

        nextButton.addActionListener(this);
        previousButton.addActionListener(this);
        clearButton.addActionListener(this);


        statusBar.setForeground(Color.BLUE);



        frame.add(buttonArea, BorderLayout.NORTH);
        frame.add(interactionArea, BorderLayout.CENTER);
        frame.add(descriptionArea, BorderLayout.CENTER);
        frame.add(statusBar, BorderLayout.SOUTH);


        frame.setResizable(false);
        frame.setSize(900, 400);
        frame.setLocation(10, 10);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub


        EnvironmentCreatorApp app = new EnvironmentCreatorApp();



    }

    @Override
    public void actionPerformed(ActionEvent event) {
        /**
         * The menu options
         */
        if (event.getActionCommand().equalsIgnoreCase("load") && currentLevel == 0) {
            File file = null;
            int returnVal;
            boolean test = true;
            do {

                returnVal = fileChooser.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    if (file.getName().substring(file.getName().length() - 3).equalsIgnoreCase("xml")) {
                        test = false;
                    }
                }

            } while (returnVal == JFileChooser.APPROVE_OPTION && test);
            if (!test) {
                try {
                    XMLManager settings = XMLManager.instance();
                    environment = (SimulationScenario) settings.unmarshal(file);
                    descriptionArea.setTitle(environment.getName());
                    descriptionArea.setScale(environment.getScale().toString());
                    descriptionArea.setxSize(environment.getXsize().toString());
                    descriptionArea.setySize(environment.getYsize().toString());
                    if (environment.isLatticeModel() != null) {
                        descriptionArea.setLatticeModel(environment.isLatticeModel());
                    }
                    interactionArea.setObstacles(environment.getObstacles());
                    interactionArea.setAgents(environment.getCrowd());

                } catch (JAXBException ex) {
                    Logger.getLogger(EnvironmentCreatorApp.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(EnvironmentCreatorApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (event.getActionCommand().equalsIgnoreCase("about")) {
            JOptionPane.showMessageDialog(frame, "This program facilitates the creation of environments for the crowd simulation test bed", "About", JOptionPane.INFORMATION_MESSAGE);
        } else if (event.getActionCommand().equalsIgnoreCase("clear")) {
            interactionArea.clearAllPoints();



        } else if (event.getActionCommand().equalsIgnoreCase("next")) {

            if (currentLevel < FINALLEVEL) {
                statusBar.setText("Level updated to " + (currentLevel + 1));
                currentLevel++;
                interactionArea.setLevel(currentLevel);
            }

            if (currentLevel == 1) {
                frame.remove(descriptionArea);
                frame.add(interactionArea, BorderLayout.CENTER);
                frame.setTitle(descriptionArea.getTitle() + "  - Create Obstacles -");


                xSize = Integer.parseInt(descriptionArea.getxSize());
                ySize = Integer.parseInt(descriptionArea.getySize());
                scale = Integer.parseInt(descriptionArea.getScale());
                interactionArea.setXSize(xSize);
                interactionArea.setYSize(ySize);
                interactionArea.setScale(scale);
                interactionArea.setLatticeSpaceFlag(descriptionArea.isLatticeModel());
                this.frame.setSize(xSize * scale + 8, ySize * scale + 100);


                interactionArea.setEnabled(true);
                interactionArea.repaint();
                clearButton.setEnabled(true);
                previousButton.setEnabled(true);
            } else if (currentLevel == FINALLEVEL) {
                agentArea.dispose();
                frame.setTitle(descriptionArea.getTitle() + "  - Final Stage -");
                interactionArea.setBackground(Color.lightGray);
                interactionArea.setEnabled(false);
                statusBar.setText("Final stage");
                nextButton.setText("Finish and Save");
                clearButton.setEnabled(false);
            } else if (currentLevel == 4) { //Agent editing level
                frame.setTitle(descriptionArea.getTitle() + "  - Agent updating -");
                interactionArea.setBackground(Color.lightGray);
                interactionArea.setEnabled(false);
                statusBar.setText("Agent Editting stage");
                clearButton.setEnabled(false);
                agentArea = new AgentDescriptionArea(interactionArea.getAgents(), interactionArea);
            } else if (currentLevel == 3) {
                frame.setTitle(descriptionArea.getTitle() + "  - Create Agents -");
            } else if (currentLevel == 2) {
                frame.setTitle(descriptionArea.getTitle() + "  - Create Agent generation Line -");
            }


        } else if (event.getActionCommand().equalsIgnoreCase("previous")) {

            if (currentLevel != 0) {
                statusBar.setText("Level set to " + (currentLevel - 1));
                frame.setTitle(descriptionArea.getTitle() + "  - Create Obstacles -");
                currentLevel--;
                interactionArea.setLevel(currentLevel);
            }
            if (currentLevel == 0) {
                frame.remove(interactionArea);
                //   descriptionArea = new DescriptionArea();

                frame.setTitle(descriptionArea.getTitle());
                frame.add(descriptionArea, BorderLayout.CENTER);
                frame.repaint();
                clearButton.setEnabled(false);
                previousButton.setEnabled(false);
            } else if (currentLevel == FINALLEVEL - 2) {
                agentArea.dispose();
                interactionArea.setBackground(Color.WHITE);
                interactionArea.setEnabled(true);
                nextButton.setText("Next");
                clearButton.setEnabled(true);
                frame.setTitle(descriptionArea.getTitle() + "  - Create Agents -");
            } else if (currentLevel == FINALLEVEL - 1) {
                agentArea = new AgentDescriptionArea(interactionArea.getAgents(), interactionArea);
                statusBar.setText("Agent Editting stage");
                frame.setTitle(descriptionArea.getTitle() + "  - Agent updating -");


            }


        } else if (event.getActionCommand().equalsIgnoreCase("finish and save")) {


            descriptionArea.loadDataToEnvironment(environment);
            interactionArea.loadObjectsIntoScenario(environment);
            XMLManager manager = XMLManager.instance();
            try {

                manager.marshal(environment, new FileOutputStream(environment.getName() + ".xml"));

            } catch (FileNotFoundException ex) {
                Logger.getLogger(EnvironmentCreatorApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JAXBException ex) {
                Logger.getLogger(EnvironmentCreatorApp.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("writing to file failed");
                String message = "Failed to create" + environment.getName() + ".xml";
                JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
            }

            String message = "XML Document successfully created at " + environment.getName() + ".xml";
            JOptionPane.showMessageDialog(new JFrame(), message, "success", JOptionPane.PLAIN_MESSAGE);
            System.exit(0);

        }
    }
}
