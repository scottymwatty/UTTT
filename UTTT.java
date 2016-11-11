import java.awt.*;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

public class UTTT{
 
    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;
    public static final String TITLE = "Ultamite Tic Tac Toe!";
    public static Box boxes[][][][] = new Box[3][3][3][3];
    public static Box big_boxes[] = new Box[9];
    public static List<Line2D> drawLines = new ArrayList<Line2D>();
    public static List<Box> drawHoverBoxes = new ArrayList<Box>();

public static void main(String[] args){
        UTTT uttt = new UTTT();
        int x_start = 45;
        int y_start = 45;
        int y_offset = 230;
        int x_offset = 230;
        drawLines.add(new Line2D.Float(x_start+223, y_start+5,   x_start+223, y_start+670));
        drawLines.add(new Line2D.Float(x_start+453, y_start+5,   x_start+453, y_start+670));
        drawLines.add(new Line2D.Float(x_start+5,   y_start+223, x_start+670, y_start+223));
        drawLines.add(new Line2D.Float(x_start+5,   y_start+453, x_start+670, y_start+453));
        for (int i=0;i<3;i++){
            for (int j=0;j<3;j++){
                drawLines.add(new Line2D.Float(x_start+79+(j*x_offset),  y_start+28+(i*y_offset),  x_start+79+(j*x_offset),  y_start+186+(i*y_offset)));
                drawLines.add(new Line2D.Float(x_start+134+(j*x_offset), y_start+28+(i*y_offset),  x_start+134+(j*x_offset), y_start+186+(i*y_offset)));
                drawLines.add(new Line2D.Float(x_start+28+(j*x_offset),  y_start+80+(i*y_offset),  x_start+186+(j*x_offset), y_start+80+(i*y_offset)));
                drawLines.add(new Line2D.Float(x_start+28+(j*x_offset),  y_start+135+(i*y_offset), x_start+186+(j*x_offset), y_start+135+(i*y_offset)));
            }
        }
        for (int i=0;i<3;i++){
            for (int j=0;j<3;j++){
                for (int k=0;k<3;k++){
                    for (int l=0;l<3;l++){
                        System.out.print(i+","+j+":"+k+","+l);
                        boxes[i][j][k][l] = uttt.new Box((x_start+27+(55*l + j*x_offset)), (y_start+27+(55*k + i*y_offset)), 50, 50);
                    }
                }
                //big_boxes[big_count] = uttt.new Box(x_start+(j*x_offset),y_start+(i*y_offset),215,215);
            }
        }




        JFrame jf = new JFrame(TITLE);
        Container cp = jf.getContentPane();

        cp.add(new JComponent() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                //---Drawing Available Move----

                //-----Drawing Hover Boxes-----
                /*g.setColor(new_color);
                g2.fillRect(x, y, width, height);*/
                for (int i=0; i<drawHoverBoxes.size();i++){
                    Box temp = drawHoverBoxes.get(i);
                    g.setColor(temp.getColor());
                    g2.fillRect(temp.getX(), temp.getY(), temp.getWidth(), temp.getHeight());
                }

                //-------Drawing X or O--------

                //-----Drawing Board Lines-----
                g2.setStroke(new BasicStroke(10));
                g.setColor(new Color(0,0,0));
                for (int i=0;i<drawLines.size();i++){
                    if (i == 4) {
                        g2.setStroke(new BasicStroke(2));
                    }
                    g2.draw(drawLines.get(i));
                }
            }
        });
        cp.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                highlightBox(e.getX(), e.getY(), new Color(255,0,0), cp);
            }
            //Unused methods, but still need to override them
            @Override
            public void mouseEntered(MouseEvent e) { }
            @Override
            public void mouseExited(MouseEvent e) { }
            @Override
            public void mousePressed(MouseEvent e) { }
            @Override
            public void mouseReleased(MouseEvent e) { }
        });
        cp.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) { 
                highlightBox(e.getX(), e.getY(), new Color(255,0,0), cp);
            }
            @Override
            public void mouseDragged(MouseEvent e) { }
        });

        jf.setSize(WIDTH, HEIGHT);
        jf.setVisible(true);
    }

    class Box {
        private int x, y, height, width;
        private Color color;
        Box(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.width = width;
            System.out.println("("+x+", "+y+", "+(x+width)+", "+(y+height)+")");

        }
        public int getX(){
            return x;
        }
        public int getY(){
            return y;
        }
        public int getHeight(){
            return height;
        }
        public int getWidth(){
            return width;
        }
        public Color getColor(){
            return color;
        }
        public void changeColor(Color new_color){
            //Add this box to the drawHoverBoxes list

            color = new_color;
            drawHoverBoxes.add(this);
            
        }
        public void removeFromList(){
            drawHoverBoxes.remove(this);
        }
    }

    public static void highlightBox(int x, int y, Color new_color, Container cp) {
        System.out.println("Checking for "+x+","+y);
        for (int i = 0; i<3; i++){
            for (int j = 0; j<3; j++){
                for (int k = 0; k<3; k++){
                    for (int l = 0;l<3; l++){
                        double top = boxes[i][j][k][l].getY();
                        double bottom = top + boxes[i][j][k][l].getHeight();
                        double left = boxes[i][j][k][l].getX();
                        double right = left + boxes[i][j][k][l].getWidth();
                        //System.out.println(i+":"+j+" (" + (int) left + ", "+(int) top+", "+(int) right+", "+(int) bottom+")");
                        if (y > top && y < bottom && x > left && x < right) {
                            System.out.println("Yep!" + (i+1) + ","+(j+1) + ":"+(k+1)+","+(l+1));
                            boxes[i][j][k][l].changeColor(new_color);
                            cp.repaint();
                        } else {
                            boxes[i][j][k][l].removeFromList();
                        }
                    }
                }
            }
        }
    }

    
}