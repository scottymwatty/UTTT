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
    public static Box newBoxes[][][][] = new Box[3][3][3][3]; 
    public static Box boxes[][][][] = new Box[3][3][3][3];
    public static Box big_boxes[][] = new Box[3][3];
    //Variables used for drawing
    public static List<Line2D> drawLines = new ArrayList<Line2D>();
    public static Box hoverBox;
    public static List<Box> availableToPlay = new ArrayList<Box>();

    public static void main(String[] args){
        //-----Initialize Lines-----
        UTTT uttt = new UTTT();
        // everything is based off of these start and offset points,
        // which makes it easy to move everything if we want to
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
        // -----Initialize Boxes-----
        for (int i=0;i<3;i++){
            for (int j=0;j<3;j++){
                for (int k=0;k<3;k++){
                    for (int l=0;l<3;l++){
                        boxes[i][j][k][l] = uttt.new Box((x_start+27+(55*l + j*x_offset)), (y_start+27+(55*k + i*y_offset)), 50, 50, i+1, j+1, k+1, l+1);
                    }
                }
                big_boxes[i][j] = uttt.new Box(x_start+(j*x_offset),y_start+(i*y_offset),215,215, i+1, j+1, 0, 0);
            }
        }

        // Now the fun part
        JFrame jf = new JFrame(TITLE);
        Container cp = jf.getContentPane();
        
        cp.add(new JComponent() { //Our lovely component that does all the work
            public void paintComponent(Graphics g) {
                /*Basically, we have 1 paint statement, and it draws things that are
                ** in the lists. This way, when we want to draw something new to the screen
                ** we just add it to its appropriate list, and then repaint */
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                //---Drawing Available Move----
                for (int i=0;i<availableToPlay.size();i++){
                    Box temp = availableToPlay.get(i);
                    g.setColor(temp.getColor());
                    g2.fillRect(temp.getX(), temp.getY(), temp.getWidth(), temp.getHeight());
                }

                //-----Drawing Hover Box-----
                /*g.setColor(new_color);
                g2.fillRect(x, y, width, height);*/
                if (hoverBox != null) {
                    g.setColor(hoverBox.getColor());
                    g2.fillRect(hoverBox.getX(), hoverBox.getY(), hoverBox.getWidth(), hoverBox.getHeight());
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
        
        // Now we add MouseListeners to that component
        cp.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	//Display mouse click position and "send" to server
            	System.out.println(sendBoxPosition(e.getX(), e.getY()));
            	
            	selectBox(e.getX(), e.getY(), new Color(0,0,255), cp); //changes box color to blue
            	
            	/* Hard coding for mouse clicks
            	int x_start = 45;
                int y_start = 45;
                int y_offset = 230;
                int x_offset = 230;
                  
                //create variables for big box and small box sizes
                int x = (e.getX() - x_start) / x_offset; 
                int y = (e.getY() - y_start) / y_offset;
                int mx = e.getX() - x * x_offset - x_start;
                int my = e.getY() - y * y_offset - y_start;
                int little_x = (mx - 27) / 55;
                int little_y = (my - 27) / 55;
 
                //display exact location of mouse click within big box and little box
                System.out.println("Mouse click location: " + (y+1) + ","+ (x+1) + "," + (little_y+1) + "," + (little_x+1)); 
                */
            }
            //Unused methods, but still need to override them
            @Override
            public void mouseEntered(MouseEvent e) { }
            @Override
            public void mouseExited(MouseEvent e) { }
            @Override
            public void mouseReleased(MouseEvent e) { }
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
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
        private int x, y, height, width, big_row, big_col, little_row, little_col;
        private Color color;
        Box(int x, int y, int width, int height, int big_row, int big_col, int little_row, int little_col) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.width = width;
            this.big_row = big_row;
            this.big_col = big_col;
            this.little_row = little_row;
            this.little_col = little_col;
            //System.out.println("("+x+", "+y+", "+(x+width)+", "+(y+height)+")");

        }
        // Screw setters. Getters are what it's all about
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
        public int getBigRow(){
            return big_row;
        }
        public int getBigCol(){
            return big_col;
        }
        public int getLittleRow(){
            return little_row;
        }
        public int getLittleCol(){
            return little_col;
        }
        public Color getColor(){
            return color;
        }
        // Ok, technically this is a setter, but it isn't called setColor, so it doesn't count
        public void changeColor(Color new_color){
            //Add this box to the drawHoverBoxes list
            color = new_color;
        }
    }

    public static Box findBox(int x, int y)  { //We use this process quite a bit, so it makes sense to make it a function
        Boolean found = false;
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
                        	//System.out.println("Correct Location: " + (i+1) + ","+(j+1) + ","+(k+1)+","+(l+1));
                            found = true; 
                        	return boxes[i][j][k][l];
                        }
                    }
                }
            }
        }
        return null;
    }

    /* Find what box we're pointing at. If it's not a box, it returns null
    ** If we're still pointing at the same box, there's no need to repaint the screen,
    ** so only repaint if the old_box is different from the new box.
    */
    public static void highlightBox(int x, int y, Color new_color, Container cp) {
        Box old_box = hoverBox;
        hoverBox = findBox(x,y);
        if (old_box != hoverBox) {
            if (hoverBox != null) {
                hoverBox.changeColor(new_color);
                //System.out.println(hoverBox.getBigRow()+", " + hoverBox.getBigCol()+ "| " + hoverBox.getLittleRow()+ ", "+ hoverBox.getLittleCol());
                //if (hoverBox.getBigRow() != 2 || hoverBox.getBigCol() != 2) { //Demoing out how we can restrict them to play in the correct spot on the board
                  //  hoverBox = null;
                //}
            }
            cp.repaint();
        }
    }
    
    public static void selectBox(int x, int y, Color new_color, Container cp) {
        Box temp = findBox(x,y);
        if (temp != null) {
            big_boxes[temp.getBigRow()-1][temp.getBigCol()-1].changeColor(new_color);
            availableToPlay.add(big_boxes[temp.getBigRow()-1][temp.getBigCol()-1]);
        }

    }
    /*Sends box position to the server 
     * Returns string for server to receive
     */
    public static String sendBoxPosition(int x, int y) {
    	Box temp = findBox(x,y);
    	if (temp != null) { //if temp is not null, then display the position of the mouse
        	return (temp.getBigRow()+", " + temp.getBigCol()+ "| " + temp.getLittleRow()+ ", "+ temp.getLittleCol());
    	} else { //otherwise return nothing (empty string)
    		return "";
    	}
    }
}
