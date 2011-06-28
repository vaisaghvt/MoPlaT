/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package environment.creator;

import environment.geography.Goals;
import environment.geography.Obstacle;
import environment.geography.Position;
import environment.geography.SimulationScenario;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Vaisagh
 */
class DescriptionArea extends JPanel {

    public void setScale(String scale) {
        this.scale.setText(scale);
    }

    public void setxSize(String xSize) {
        this.xSize.setText(xSize);
    }

    public void setySize(String ySize) {
        this.ySize.setText(ySize);
    }
    JTextField Name;
    JTextField xSize;
    JTextField ySize;
    JTextField scale;
    JCheckBox integerSpace;
    JCheckBox latticeModel;
    EnvironmentCreatorApp source;

    public EnvironmentCreatorApp getSource() {
        return source;
    }

    public void setSource(EnvironmentCreatorApp source) {
        this.source = source;
    }

    public String getxSize() {
        return xSize.getText();
    }

    public String getySize() {
        return ySize.getText();
    }

    public DescriptionArea(EnvironmentCreatorApp eca) {
        source = eca;
        this.setLayout(new GridLayout(10, 4));


        this.setBackground(Color.lightGray);
        Name = new JTextField();
        Name.setColumns(20);
        this.add(new JLabel(""));
        this.add(new JLabel("Name : "));
        this.add(Name);
        this.add(new JLabel(""));
        Name.setText("Default");

        for (int i = 0; i < 4; i++) {
            this.add(new JLabel(""));
        }

        xSize = new JTextField();
        xSize.setColumns(20);
        this.add(new JLabel(""));
        this.add(new JLabel("X size(meters) :"));
        this.add(xSize);
        this.add(new JLabel(""));
        xSize.setText("3");

        for (int i = 0; i < 4; i++) {
            this.add(new JLabel(""));
        }

        ySize = new JTextField();
        ySize.setColumns(20);
        this.add(new JLabel(""));
        this.add(new JLabel("Y size (meters) :"));
        this.add(ySize);
        this.add(new JLabel(""));
        ySize.setText("3");

        for (int i = 0; i < 4; i++) {
            this.add(new JLabel(""));
        }

        scale = new JTextField();
        scale.setColumns(20);
        this.add(new JLabel(""));
        this.add(new JLabel("Scale (pix / m) :"));
        this.add(scale);
        this.add(new JLabel(""));
        scale.setText("100");


        for (int i = 0; i < 4; i++) {
            this.add(new JLabel(""));
        }


        latticeModel = new JCheckBox();
        this.add(new JLabel(""));
        this.add(new JLabel("Lattice Model :"));
        this.add(latticeModel);
        this.add(new JLabel(""));

        for (int i = 0; i < 4; i++) {
            this.add(new JLabel(""));
        }

      

    }

    public void loadDataToEnvironment(SimulationScenario environment) {
        environment.setName(Name.getText());
        environment.setXsize(Integer.parseInt(xSize.getText()));
        environment.setYsize(Integer.parseInt(ySize.getText()));
        environment.setScale(Integer.parseInt(scale.getText()));
        environment.setLogUsed(Boolean.TRUE);
        environment.setDisplayUsed(Boolean.TRUE);
        environment.setLatticeModel(this.isLatticeModel());
        if (isLatticeModel()) {
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
    }

    public String getScale() {
        return this.scale.getText();
    }

    public String getTitle() {
        return Name.getText();
    }


    public boolean isLatticeModel() {
        if (latticeModel.isSelected()) {
            return true;
        } else {
            return false;
        }
    }

    public void setLatticeModel(boolean state) {
        latticeModel.setSelected(false);
    }



    void setTitle(String name) {
        Name.setText(name);
    }


}
