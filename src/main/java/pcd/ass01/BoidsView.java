package pcd.ass01;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

public class BoidsView {

    private JFrame frame;
    private BoidsPanel boidsPanel;
    private JSlider cohesionSlider, separationSlider, alignmentSlider;
    private JButton startButton;
    private BoidsModel model;
    private int width, height;
    private boolean isRunning = true;

    public BoidsView(BoidsModel model, int width, int height) {
        this.model = model;
        this.width = width;
        this.height = height;

        frame = new JFrame("Boids Simulation");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel cp = new JPanel();
        LayoutManager layout = new BorderLayout();
        cp.setLayout(layout);

        boidsPanel = new BoidsPanel(this, model);
        cp.add(BorderLayout.CENTER, boidsPanel);

        JPanel slidersPanel = new JPanel();

        startButton = makeButton();
        startButton.addActionListener(e -> {
            if (isRunning) {
                pause();
                startButton.setText("PLAY");
            } else {
                play();
                startButton.setText("PAUSE");
            }
        });

        separationSlider = makeSlider();
        separationSlider.addChangeListener(l -> {
            var val = separationSlider.getValue();
            model.setSeparationWeight(0.1 * val);
        });

        alignmentSlider = makeSlider();
        alignmentSlider.addChangeListener(l -> {
            var val = alignmentSlider.getValue();
            model.setAlignmentWeight(0.1 * val);
        });

        cohesionSlider = makeSlider();
        cohesionSlider.addChangeListener(l -> {
            var val = cohesionSlider.getValue();
            model.setCohesionWeight(0.1 * val);
        });

        slidersPanel.add(startButton);
        slidersPanel.add(new JLabel("Separation"));
        slidersPanel.add(separationSlider);
        slidersPanel.add(new JLabel("Alignment"));
        slidersPanel.add(alignmentSlider);
        slidersPanel.add(new JLabel("Cohesion"));
        slidersPanel.add(cohesionSlider);

        cp.add(BorderLayout.SOUTH, slidersPanel);

        frame.setContentPane(cp);

        frame.setVisible(true);
    }

    private void play() {
        this.isRunning = true;
    }

    private void pause() {
        this.isRunning = false;
    }

    private JButton makeButton() {
        return new JButton("PAUSE");
    }

    private JSlider makeSlider() {
        var slider = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        Hashtable labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("0"));
        labelTable.put(10, new JLabel("1"));
        labelTable.put(20, new JLabel("2"));
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);
        return slider;
    }

    public void update(int frameRate) {
        boidsPanel.setFrameRate(frameRate);
        boidsPanel.repaint();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

}
