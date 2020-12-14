import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileFilter;
public class PaintProgram extends JPanel implements MouseMotionListener, ActionListener, AdjustmentListener, ChangeListener {
    private ArrayList<ArrayList<Point>> points;
    Stack<Shape> shapeStack;
    Stack<ArrayList<Point>> lineStack;
    Stack<String> mainStack;
    Stack<String> undoStack;
    JFrame frame;
    JMenuBar bar;
    JMenu menu, file;
    JPanel topPanel;
    JPanel topLeftPanel;
    JScrollBar widthBar;
    JMenuItem save, load;
    JButton[] colorOptions;
    Color[] colors;
    Color currentColor;
    JColorChooser colorChooser;
    int currentPenWidth;
    JButton lineButton, rectButton;
    ArrayList<Point> currList = new ArrayList<Point>(); // current list of points needed to draw a line
    ArrayList<Shape> shapes = new ArrayList<Shape>();
    boolean lineSelected = true, rectSelected = false;
    boolean firstIteration = true, oncePerEveryMouseMoved = false;
    JButton undoButton, redoButton;
    ImageIcon freeLineIcon, rectangleIcon, undoIcon, redoIcon; 
    Shape currShape;
    JFileChooser fileChooser;
    BufferedImage loadedImage;
    int x = 0, y = 0;

    public PaintProgram() {
        points = new ArrayList<ArrayList<Point>>();
        shapeStack = new Stack<>();
        lineStack = new Stack<>();
        mainStack = new Stack<>();
        undoStack = new Stack<>();
        frame = new JFrame("Paint Program");
        frame.add(this);
        bar = new JMenuBar();
        menu = new JMenu("Colors");
        colorOptions = new JButton[5];
        colors = new Color[] { Color.RED, Color.PINK, Color.YELLOW, Color.GREEN, Color.CYAN };
        currentColor = colors[0];
        topPanel = new JPanel();
        menu.setLayout(new GridLayout(1, 5));
        for (int x = 0; x < colorOptions.length; x++) {
            colorOptions[x] = new JButton();
            colorOptions[x].addActionListener(this);
            colorOptions[x].putClientProperty("colorIndex", x);
            colorOptions[x].setOpaque(true);
            colorOptions[x].setBackground(colors[x]);
            colorOptions[x].setBorderPainted(false);
            menu.add(colorOptions[x]);
        }
        colorChooser = new JColorChooser();
        colorChooser.getSelectionModel().addChangeListener(this);
        menu.add(colorChooser);
        this.addMouseMotionListener(this);
        topPanel.setLayout(new BorderLayout());
        topLeftPanel = new JPanel();
        file = new JMenu("File");
        save = new JMenuItem("Save");
        save.addActionListener(this);
        file.add(save);
        load = new JMenuItem("Load");
        load.addActionListener(this);
        file.add(load);
        bar.add(file);
        bar.add(menu);
        topLeftPanel.add(bar);
        freeLineIcon = new ImageIcon("freeline.png");
        freeLineIcon = new ImageIcon(freeLineIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH));
        lineButton = new JButton(freeLineIcon);
        lineButton.setFocusPainted(false);
        lineButton.setOpaque(true);
        lineButton.addActionListener(this);
        topLeftPanel.add(lineButton);
        rectangleIcon = new ImageIcon("rectangle.png");
        rectangleIcon = new ImageIcon(rectangleIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH));
        rectButton = new JButton(rectangleIcon);
        rectButton.setFocusPainted(false);
        rectButton.setOpaque(true);
        rectButton.addActionListener(this);
        topLeftPanel.add(rectButton);
        undoIcon = new ImageIcon("undo.png");
        undoIcon = new ImageIcon(undoIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH));
        undoButton = new JButton(undoIcon);
        undoButton.setFocusPainted(false);
        undoButton.addActionListener(this);
        topLeftPanel.add(undoButton);
        redoIcon = new ImageIcon("redo.png");
        redoIcon = new ImageIcon(redoIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH));
        redoButton = new JButton(redoIcon);
        redoButton.setFocusPainted(false);
        redoButton.addActionListener(this);
        topLeftPanel.add(redoButton);
        topPanel.add(topLeftPanel, BorderLayout.WEST);
        widthBar = new JScrollBar(JScrollBar.HORIZONTAL);
        widthBar.setMinimum(1);
        widthBar.setMaximum(25);
        widthBar.addAdjustmentListener(this);
        topPanel.add(widthBar);
        frame.add(topPanel, BorderLayout.NORTH);
        String currDir = System.getProperty("user.dir");
        fileChooser = new JFileChooser(currDir);
        frame.setSize(1000, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 1000, 600);
        if(loadedImage!=null)
            g2.drawImage(loadedImage, 0, 0, null);
        for (int x = 0; x < points.size(); x++) {
            for (int y = 0; y < points.get(x).size(); y++) {
                Point p = points.get(x).get(y);
                g2.setColor(p.getColor());
                g2.setStroke(new BasicStroke(p.getPenWidth()));
                // g2.fillOval(p.getX(), p.getY(), 10, 10);
                if (y >= 1) {
                    g2.drawLine(points.get(x).get(y - 1).getX(), points.get(x).get(y - 1).getY(), p.getX(), p.getY());
                }
            }
        }
        for (Shape shape : shapes) {
            g2.setColor(shape.getColor());
            g2.setStroke(new BasicStroke(shape.getPenWidth()));
            if (shape instanceof Block)
                g2.draw(((Block) shape).getRect());
        }
        if (lineSelected) {
            for (int y = 0; y < currList.size(); y++) {
                Point p = currList.get(y);
                g2.setColor(p.getColor());
                g2.setStroke(new BasicStroke(p.getPenWidth()));
                // g2.fillOval(p.getX(), p.getY(), 10, 10);
                if (y >= 1) {
                    g2.drawLine(currList.get(y - 1).getX(), currList.get(y - 1).getY(), p.getX(), p.getY());
                }
            }
        }
        if (rectSelected) {
            g2.setColor(currentColor);
            g2.setStroke(new BasicStroke(currShape.getPenWidth()));
            g2.draw(((Block) currShape).getRect());
        }
    }
    public BufferedImage createImage()
    {
        int width = this.getWidth();
        int height = this.getHeight();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        this.paint(g2);
        g2.dispose();
        return img;
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        oncePerEveryMouseMoved = false;
        if (lineSelected)
            currList.add(new Point(e.getX(), e.getY(), currentColor, currentPenWidth));
        else if (rectSelected) {
            if (firstIteration) {
                currShape = new Block(e.getX(), e.getY(), 0, 0, currentColor, currentPenWidth);
                shapes.add(currShape);
                mainStack.push("Shape");
                firstIteration = false;
                x = currShape.getX();
                y = currShape.getY();
            }
            int shapeWidth = e.getX() - x;
            int shapeHeight = e.getY() - y;
            currShape.setWidth(Math.abs(shapeWidth));
            currShape.setHeight(Math.abs(shapeHeight));
            if (shapeHeight < 0) {
                currShape.setY(e.getY());
            }
            if (shapeWidth < 0) {
                currShape.setX(e.getX());
            }
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
        if(!oncePerEveryMouseMoved)
        {
            if(currList.size()>0)
            {
                points.add(currList);
                mainStack.push("Line");
            }
            oncePerEveryMouseMoved=true;
        }
        currList = new ArrayList<Point>();
        currShape = new Block(0, 0, 0, 0, null, 0);
        firstIteration = true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == lineButton) {
            lineSelected = true;
            rectSelected = false;
            lineButton.setBackground(Color.GRAY);
            rectButton.setBackground(null);
        } else if (e.getSource() == rectButton) {
            rectSelected = true;
            lineSelected = false;
            rectButton.setBackground(Color.GRAY);
            lineButton.setBackground(null);
        } 
        else if(e.getSource() == undoButton)
        {
            if(mainStack.size()>0)
            {
                String lastAction = mainStack.pop();
                undoStack.push(lastAction);
                if(lastAction.equals("Shape"))
                {
                    if(shapes.size()>0)
                    {
                        shapeStack.push(shapes.remove(shapes.size()-1));
                        repaint();
                    }
                }
                else if(lastAction.equals("Line"))
                {
                    if(points.size()>0)
                    {
                        lineStack.push(points.remove(points.size()-1));
                        repaint();
                    }
                }
            }
        }
        else if(e.getSource() == redoButton)
        {
            if(undoStack.size()>0)
            {
                String lastAction = undoStack.pop();
                mainStack.push(lastAction);
                if(lastAction.equals("Shape"))
                {
                    Shape s = shapeStack.pop();
                    shapes.add(s);
                    repaint();
                }
                else if(lastAction.equals("Line"))
                {
                    ArrayList<Point> lineArray = lineStack.pop();
                    points.add(lineArray);
                    repaint();
                }
            }
        }
        else if(e.getSource() == save)
        {
            FileFilter filter = new FileNameExtensionFilter("*.png", "png");
            fileChooser.setFileFilter(filter);
            if(fileChooser.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
            {
                File file = fileChooser.getSelectedFile();
                try
                {
                    String st=file.getAbsolutePath();
                    if(st.indexOf(".png")>=0)
                    st=st.substring(0,st.length()-4);
                    ImageIO.write(createImage(),"png",new File(st+".png"));
                }
                catch(IOException exception)
                {

                }
            }
        }
        else if(e.getSource() == load)
        {
            fileChooser.showOpenDialog(null);
            File loadFile = fileChooser.getSelectedFile();
            try{
                loadedImage = ImageIO.read(loadFile);
            }
            catch(IOException exception)
            {

            }
            resetAll();
            repaint();
        }
        else {
            int index = Integer.parseInt(((JButton) e.getSource()).getClientProperty("colorIndex") + "");
            currentColor = colors[index];
        }
    }
    public void resetAll()
    {
        points = new ArrayList<ArrayList<Point>>();
        shapes = new ArrayList<Shape>();
        currList = new ArrayList<Point>();
        lineStack = new Stack<ArrayList<Point>>();
        shapeStack = new Stack<Shape>();
        mainStack = new Stack<String>();
        undoStack = new Stack<String>();
    }
    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        // TODO Auto-generated method stub
        currentPenWidth = e.getValue();
    }
    @Override
    public void stateChanged(ChangeEvent e) {
        // TODO Auto-generated method stub
        currentColor = colorChooser.getColor();

    }
    public class Shape {
        private int x, y, width, height, penWidth;
        private Color color;

        public Shape(int x, int y, int width, int height, Color color, int penWidth) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.penWidth = penWidth;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getPenWidth() {
            return penWidth;
        }

        public Color getColor() {
            return color;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void setPenWidth(int penWidth) {
            this.penWidth = penWidth;
        }

        public String toString() {
            return "x: " + getX() + "  y: " + getY() + "  w: " + getWidth() + "  h: " + getHeight();
        }
    }

    public class Block extends Shape {
        public Block(int x, int y, int width, int height, Color color, int penWidth) {
            super(x, y, width, height, color, penWidth);
        }

        public Rectangle getRect() {
            return new Rectangle(getX(), getY(), getWidth(), getHeight());
        }
    }

    public class Point {
        private int x;
        private int y;
        private Color color;
        private int penWidth;

        public Point(int x, int y, Color color, int penWidth) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.penWidth = penWidth;
        }

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Color getColor() {
            return color;
        }

        public int getPenWidth() {
            return penWidth;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void setPenWidth(int penWidth) {
            this.penWidth = penWidth;
        }
    }

    public static void main(String[] args) {
        PaintProgram app = new PaintProgram();
    }
}