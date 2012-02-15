/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.creator;

import environment.XMLScenarioManager;
import environment.geography.Agent;
import environment.geography.AgentGroup;
import environment.geography.AgentLine;
import environment.geography.Goals;
import environment.geography.Obstacle;
import environment.geography.Position;
import environment.geography.RoadMapPoint;
import environment.geography.SimulationScenario;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
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
    private List<AgentGroup> agentGroups = new ArrayList<AgentGroup> ();
    private List<Goals> goalLines = new ArrayList<Goals> ();
    private List<Position> roadMap;

 
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
            List<Position> test = new ArrayList<Position>();
           for(RoadMapPoint roadMapPoint : environment.getRoadMap()){
               test.add(roadMapPoint.getPosition());
           }
            this.setRoadMap(test);
            this.setObstacles(environment.getObstacles());
            this.setAgents(environment.getCrowd());
            this.setAgentLines(environment.getGenerationLines());
            this.setAgentGroups(environment.getAgentGroups());
            this.setGoalLines(environment.getEnvironmentGoals());
        } catch (JAXBException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

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
     
            temp.setStartPoint(tempPoint);
            tempPoint.setX(0.0);
            tempPoint.setY(0.0);
     
            temp.setEndPoint(tempPoint);
            environment.getEnvironmentGoals().add(temp);
        }

        environment.getObstacles().clear();
        if (!obstacles.isEmpty()) {
            for (int i = 0; i < obstacles.size(); i++) {
                Obstacle tempObstacle = new Obstacle();
                for (int j = 0; j < obstacles.get(i).getVertices().size(); j++) {
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
            for (int i = 0; i < agents.size(); i++) {
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
            for (int i = 0; i < agentLines.size(); i++) {
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
        
        environment.getAgentGroups().clear();
        if (!agentGroups.isEmpty()) {
            for (AgentGroup group: agentGroups) {
                AgentGroup tempGroup = new AgentGroup();

                Position start = new Position();
                start.setX((double) group.getStartPoint().getX());
                start.setY((double) group.getStartPoint().getY());

                Position end = new Position();
                end.setX((double) group.getEndPoint().getX());
                end.setY((double) group.getEndPoint().getY());

                tempGroup.setStartPoint(start);
                tempGroup.setEndPoint(end);

                
                tempGroup.setSize(group.getSize());
                tempGroup.setMinSpeed(group.getMinSpeed());
                tempGroup.setMaxSpeed(group.getMaxSpeed());
                tempGroup.setMeanSpeed(group.getMeanSpeed());
                tempGroup.setSDevSpeed(group.getSDevSpeed());

                environment.getAgentGroups().add(tempGroup);
            }
        }
        
        environment.getEnvironmentGoals().clear();
        if (!goalLines.isEmpty()) {
            for (int i = 0; i < goalLines.size(); i++) {
                Goals tempGoalLines = new Goals();
                
                Position start = new Position();
                start.setX((double) goalLines.get(i).getStartPoint().getX());
                start.setY((double) goalLines.get(i).getStartPoint().getY());

                Position end = new Position();
                end.setX((double) goalLines.get(i).getEndPoint().getX());
                end.setY((double) goalLines.get(i).getEndPoint().getY());

                tempGoalLines.setStartPoint(start);
                tempGoalLines.setEndPoint(end);

                environment.getEnvironmentGoals().add(tempGoalLines);
            }
        }
        
        environment.getRoadMap().clear();
        if (!roadMap.isEmpty()) {
            for (int i = 0; i < roadMap.size(); i++) {
                
                
                Position roadMapPointPosition = new Position();
                roadMapPointPosition.setX((double) roadMap.get(i).getX());
                roadMapPointPosition.setY((double) roadMap.get(i).getY());

                RoadMapPoint point = new RoadMapPoint();
                point.setPosition(roadMapPointPosition);
                point.setNumber(i);
               environment.getRoadMap().add(point);

            }
        }
        
        XMLScenarioManager manager = XMLScenarioManager.instance("environment.geography");
        try {

            manager.marshal(environment, new FileOutputStream(environment.getName() + ".xml"));

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (JAXBException ex) {
//            ex.printStackTrace();
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

    public List<AgentGroup> getAgentGroups() {
        return this.agentGroups;
    }

    public void setAgentGroups(List<AgentGroup> agentGroups) {
        this.agentGroups = agentGroups;
    }

    public List<Goals> getGoalLines() {
        return this.goalLines;
    }
    
    public void setGoalLines(List<Goals> goalLines) {
        this.goalLines = goalLines;
    }

    public List<Position> getRoadMap() {
        assert roadMap!= null;
        System.out.println("not null now");
        return this.roadMap;
    }

    public void setRoadMap(Collection<Position> roadMap) {
        this.roadMap = new ArrayList<Position>(roadMap);
    }


    
}
