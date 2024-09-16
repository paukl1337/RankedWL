// paul
// 9-15-24
// working on: adding a gui to the program :D

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GUI extends JFrame
{
    ThreadManager tm = new ThreadManager();
    RankedWL rwl;
    JPanel panel;
    JButton requestButton;
    JTextField name;
    JLabel ranking;
    JLabel average;
    public GUI()
    {
        rwl = new RankedWL();
        initialize();
    }

    public void initialize()
    {
        setTitle("Ranked WL");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 100);
        setLayout(new BorderLayout());
        panel = new JPanel();
        ranking = new JLabel("");
        average = new JLabel("");
        name = new JTextField("Please enter your name");
        requestButton = new JButton("Start");
        name.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {}

            @Override
            public void focusLost(FocusEvent e)
            {
                rwl.userInput = name.getText();
            }
        });
        requestButton.addActionListener(e -> {
            tm.startPeriodicTask();
            rwl.userInput = name.getText();
        });
        panel.setLayout(new FlowLayout());
        panel.add(name);
        panel.add(requestButton);
        panel.add(ranking);
        add(panel, BorderLayout.CENTER);
        setVisible(true);

        this.addWindowListener(new WindowListener()
        {
            @Override
            public void windowOpened(WindowEvent e)
            {}

            @Override
            public void windowClosing(WindowEvent e)
            {
                tm.stopPeriodicTask();
            }
            @Override
            public void windowClosed(WindowEvent e)
            {}
            @Override
            public void windowIconified(WindowEvent e)
            {}
            @Override
            public void windowDeiconified(WindowEvent e)
            {}
            @Override
            public void windowActivated(WindowEvent e)
            {}
            @Override
            public void windowDeactivated(WindowEvent e)
            {}
        });
    }



}
