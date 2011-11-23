/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.creator;

import environment.geography.Agent;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Vaisagh
 * 
 * Description : This class creates and handles the form in which you enter 
 * details of the agent
 */
class AgentDescriptionFrame extends JFrame implements ListSelectionListener, ActionListener {

    AgentEditorLevel handle;
    List<Agent> agents;
    JTextField speed;
    JButton update;
    JList agentList;
    JRadioButton highCommitment = new JRadioButton("High");
    JRadioButton mediumCommitment = new JRadioButton("Medium");
    JRadioButton lowCommitment = new JRadioButton("Low");
    ButtonGroup commitmentGroup = new ButtonGroup();

    AgentDescriptionFrame(List<Agent> agents, AgentEditorLevel handle) {
        this.handle = handle;
        this.agents = agents;
        speed = new JTextField(40);
        speed.setText("1.3");


        highCommitment.setActionCommand("High");



        highCommitment.setActionCommand("Medium");


        highCommitment.setActionCommand("Low");

        mediumCommitment.setSelected(true);


        //Group the radio buttons.

        commitmentGroup.add(lowCommitment);
        commitmentGroup.add(mediumCommitment);
        commitmentGroup.add(highCommitment);

        //Register a listener for the radio buttons.

        lowCommitment.addActionListener(this);
        mediumCommitment.addActionListener(this);
        highCommitment.addActionListener(this);





        this.setTitle("Agent Description Area");
        this.setLayout(new BorderLayout());

        List<String> agentNames = new ArrayList<String>();

        for (int i = 0; i < agents.size(); i++) {
            agentNames.add("Agent " + agents.get(i).getId());
        }


        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));

        agentList = new JList(agentNames.toArray());

        JScrollPane agentScrollingList = new JScrollPane(agentList);
        // agentScrollingList.setSize(this.getWidth(), this.getHeight()/3);
        //panel.add(agentScrollingList);
        agentList.addListSelectionListener(this);


        panel.add(new JLabel("Select agents"));
        panel.add(agentScrollingList);

        panel.add(new JLabel("Prefered Speed"));
        panel.add(speed);

        panel.add(new JLabel("Commitment Level"));
        panel.add(lowCommitment);

        panel.add(new JLabel(""));
        panel.add(mediumCommitment);

        panel.add(new JLabel(""));
        panel.add(highCommitment);



        update = new JButton("Update");
        update.addActionListener(this);

        this.add(panel, BorderLayout.CENTER);
        this.add(update, BorderLayout.SOUTH);


        this.setResizable(false);
        this.setSize(300, 250);
        this.setLocation(900, 200);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {




        // Find out which indexes are selected.
        for (int i = 0; i < agents.size(); i++) {
            if (agentList.isSelectedIndex(i)) {
                handle.setAgentHighlight(i, true);

            } else {
                handle.setAgentHighlight(i, false);

            }

        }
        handle.repaintRequest();


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int count = 0;
        if (e.getActionCommand().equalsIgnoreCase("update")) {
            for (int i = 0; i < agents.size(); i++) {
                if (agentList.isSelectedIndex(i)) {
                    agents.get(i).setPreferedSpeed(Double.parseDouble(speed.getText()));
                    if (highCommitment.isSelected()) {
                        agents.get(i).setCommitmentLevel(3);
                    } else if (mediumCommitment.isSelected()) {
                        agents.get(i).setCommitmentLevel(2);
                    } else {
                        agents.get(i).setCommitmentLevel(1);
                    }
                    count++;

                }
            }
            String message = count + " agents have been updated";
            JOptionPane.showMessageDialog(new JFrame(), message, "Updated", JOptionPane.PLAIN_MESSAGE);

        }
    }
}
