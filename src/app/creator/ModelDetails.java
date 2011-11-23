/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.creator;

import environment.XMLScenarioManager;
import environment.geography.Agent;
import environment.geography.AgentLine;
import environment.geography.Goals;
import environment.geography.Obstacle;
import environment.geography.Position;
import environment.geography.SimulationScenario;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

/**
 *
 * @author vaisagh
 */
class ModelDetails {

    private SimulationScenario environment;
    private String title;
    private int scale;
    private int xSize;
    private int ySize;
    private Boolean latticeModel;
    private List<Obstacle> obstacles = new ArrayList<Obstacle>();
    private List<Agent> agents = new ArrayList<Agent>();
    private List<AgentLine> agentLines = new ArrayList<AgentLine>();

 
    public void loadFromFile(File file) {
        try {
            XMLScenarioManager settings = XMLScenarioManager.instance("environment.geography");
            environment = (SimulationScenario) settings.unmarshal(file);
            this.setTitle(environment.getName());
            this.setScale(environment.getScale());
            this.setXSize(environment.getXsize());
            this.setYSize(environment.getYsize());
            if (environment.isLatticeModel() != null) {
                this.setLatticeModel(environment.isLatticeModel());
            }
            this.setObstacles(environment.getObstacles());
            this.setAgents(environment.getCrowd());
            this.setAgentLines(environment.getGenerationLines());

        } catch (JAXBException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    public String getName() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void saveToXMLFile() {
        environment = new SimulationScenario();
        environment.setName(title);
        environment.setXsize(xSize);
        environment.setYsize(ySize);
        environment.setScale(scale);
        environment.setLogUsed(Boolean.TRUE);
        environment.setDisplayUsed(Boolean.TRUE);
        environment.setLatticeModel(latticeModel);
        if (latticeModel) {
            environment.setDirection(5);
            environment.getEnvironmentGoals().clear();
            Goals temp = new Goals();
            Position tempPoint = new Position();
            tempPoint.setX(0.0);
            tempPoint.setY(0.0);
            tempPoint.setZ(0.0);
            temp.getVertices().add(tempPoint);
            tempPoint.setX(0.0);
            tempPoint.setY(0.0);
            tempPoint.setZ(0.0);
            temp.getVertices().add(tempPoint);
            environment.getEnvironmentGoals().add(temp);
        }

        environment.getObstacles().clear();



        if (!obstacles.isEmpty()) {
            for (int i = 0; i
                    < obstacles.size(); i++) {
                Obstacle tempObstacle = new Obstacle();


                for (int j = 0; j
                        < obstacles.get(i).getVertices().size(); j++) {
                    Position tempStorage = new Position();
                    tempStorage.setX((double) obstacles.get(i).getVertices().get(j).getX());
                    tempStorage.setY((double) obstacles.get(i).getVertices().get(j).getY());

                    tempObstacle.getVertices().add(tempStorage);



                }


                environment.getObstacles().add(tempObstacle);


            }
        }

        environment.getCrowd().clear();


        if (!agents.isEmpty()) {
            for (int i = 0; i
                    < agents.size(); i++) {
                Agent tempAgent = new Agent();

                Position start = new Position();
                start.setX((double) agents.get(i).getPosition().getX());
                start.setY((double) agents.get(i).getPosition().getY());

                Position end = new Position();
                end.setX((double) agents.get(i).getGoal().getX());
                end.setY((double) agents.get(i).getGoal().getY());

                tempAgent.setGoal(end);
                tempAgent.setPosition(start);

                tempAgent.setId(agents.get(i).getId());
                tempAgent.setPreferedSpeed(agents.get(i).getPreferedSpeed());
                tempAgent.setCommitmentLevel(agents.get(i).getCommitmentLevel());
                environment.getCrowd().add(tempAgent);



            }
        }

        environment.getGenerationLines().clear();


        if (!agentLines.isEmpty()) {
            for (int i = 0; i
                    < agentLines.size(); i++) {
                AgentLine tempLine = new AgentLine();

                Position start = new Position();
                start.setX((double) agentLines.get(i).getStartPoint().getX());
                start.setY((double) agentLines.get(i).getStartPoint().getY());

                Position end = new Position();
                end.setX((double) agentLines.get(i).getEndPoint().getX());
                end.setY((double) agentLines.get(i).getEndPoint().getY());

                tempLine.setStartPoint(start);
                tempLine.setEndPoint(end);

                tempLine.setFrequency(agentLines.get(i).getFrequency());
                tempLine.setNumber(agentLines.get(i).getNumber());
                tempLine.setDirection(agentLines.get(i).getDirection());

                environment.getGenerationLines().add(tempLine);



            }
        }
        XMLScenarioManager manager = XMLScenarioManager.instance("environment.geography");
        try {

            manager.marshal(environment, new FileOutputStream(environment.getName() + ".xml"));

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (JAXBException ex) {
            ex.printStackTrace();
            System.out.println("writing to file failed");
            String message = "Failed to create" + environment.getName() + ".xml";
            JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
        }

        String message = "XML Document successfully created at " + environment.getName() + ".xml";
        JOptionPane.showMessageDialog(new JFrame(), message, "success", JOptionPane.PLAIN_MESSAGE);
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public void setScale(String scale) {
        this.scale = Integer.parseInt(scale);
    }

    public void setXSize(String xSize) {
        this.xSize = Integer.parseInt(xSize);
    }

    public void setYSize(String ySize) {
        this.ySize = Integer.parseInt(ySize);
    }

    public void setLatticeModel(Boolean latticeModel) {
        this.latticeModel = latticeModel;
    }

    public void setObstacles(List<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public void setXSize(Integer xsize) {
        this.xSize = xsize;
    }

    public void setYSize(Integer ysize) {
        this.ySize = ysize;
    }

    public void setAgentLines(List<AgentLine> generationLines) {
        this.agentLines = generationLines;
    }

    public int getScale() {
        return scale;
    }

    public String getTitle() {
        return title;
    }

    public int getxSize() {
        return xSize;
    }

    public int getySize() {
        return ySize;
    }

    public List<Obstacle> getObstacles() {
        return this.obstacles;
    }

    public boolean getLatticeSpaceFlag() {
        return this.latticeModel;
    }

    public List<AgentLine> getAgentLines() {
        return this.agentLines;
    }

    public List<Agent> getAgents() {
        return this.agents;
    }
}
