package pcd.ass01;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

public class BoidsView {

    public static final String PAUSE_STRING = "PAUSE";
    private static final String PLAY_STRING = "PLAY";
    private JFrame frame;
    private BoidsPanel boidsPanel;
    private JSlider cohesionSlider, separationSlider, alignmentSlider;
    private JTextField nBoidsTextField;
    private JButton playButton;
    private BoidsModel model;
    private int width, height;
    private boolean isRunning = false;
    private int nBoids;

    public BoidsView(BoidsModel model, int width, int height, int nBoids) {
        this.model = model;
        this.width = width;
        this.height = height;
        this.nBoids = nBoids;

        frame = new JFrame("Boids Simulation");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel cp = new JPanel();
        LayoutManager layout = new BorderLayout();
        cp.setLayout(layout);

        boidsPanel = new BoidsPanel(this, model);
        cp.add(BorderLayout.CENTER, boidsPanel);

        JPanel slidersPanel = new JPanel();

        nBoidsTextField = new JTextField(String.valueOf(this.nBoids), 10);
        nBoidsTextField.setForeground(Color.BLACK);
        nBoidsTextField.addActionListener(l -> {
            nBoidsTextField.setForeground(Color.WHITE);
            if (isRunning) {
                nBoidsTextField.setBackground(Color.RED);
                nBoidsTextField.setText("STOP SIMULATION");
            } else {
                String text = nBoidsTextField.getText();
                if (!isNumeric(text)) {
                    nBoidsTextField.setBackground(Color.ORANGE);
                    nBoidsTextField.setText("PUT INTEGER");
                } else {
                    nBoidsTextField.setBackground(Color.WHITE);
                    nBoidsTextField.setForeground(Color.BLACK);
                    this.nBoids = (int) Double.parseDouble(nBoidsTextField.getText());
                }
            }
        });

        playButton = makeButton();
        playButton.addActionListener(e -> {
            if (isRunning) {
                pause();
                playButton.setText(PLAY_STRING);
            } else {
                play();
                playButton.setText(PAUSE_STRING);
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

        slidersPanel.add(playButton);
        slidersPanel.add(new JLabel("Set size end press Enter"));
        slidersPanel.add(nBoidsTextField);
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

    private boolean isNumeric(String text) {
        if (text == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(text);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private void play() {
        this.isRunning = true;
    }

    private void pause() {
        this.isRunning = false;
    }

    private JButton makeButton() {
        return new JButton(PLAY_STRING);
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

    public int getSizeBoids() {
        return this.nBoids;
    }
}
