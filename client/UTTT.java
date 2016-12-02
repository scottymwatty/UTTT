import java.awt.*;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

import java.text.*;
import java.io.*;

public class UTTT {
 
    //JFrame Variables
    public final int WIDTH = 550;
    public final int HEIGHT = 650;
    public final String TITLE = "Ultamite Tic Tac Toe!";

    //Box arrays
    public Box boxes[][][][] = new Box[3][3][3][3];
    public Box big_boxes[][] = new Box[3][3];
    private char big_box_move[][] = new char[3][3];
    private Box again_box = new Box(300, 50, 100, 30, 0, 0, 0, 0);


    //Variables used for drawing
    public List<Line2D> drawLines = new ArrayList<Line2D>();
    public Box hoverBox;
    public List<Box> availableToPlay = new ArrayList<Box>();
    public List<Ellipse2D> o_moves = new ArrayList<Ellipse2D>();
    public List<Ellipse2D> big_o_moves = new ArrayList<Ellipse2D>();
    public List<Line2D> x_moves = new ArrayList<Line2D>();
    public List<Line2D> big_x_moves = new ArrayList<Line2D>();
    public String message = "Welcome to UTTT";
    public static Boolean done = false;
    public char player_symbol;
    public Boolean my_turn = false;
    public Boolean game_over = false;
    public int row_to_play = 0, col_to_play = 0, num_of_last_moves = 0;

    private BufferedReader fromServer;
    private BufferedWriter toServer;

    //-----The main loop-----
    public static void main(String[] args) throws IOException{
        UTTT uttt = new UTTT();
        uttt.initilizeLines();
        final Container cp = uttt.setupJFrame();
        uttt.connectToServer();
        while (!done) {
            uttt.readInput(cp);
        }
    }
    
    class Box {
        private int x, y, height, width, big_row, big_col, little_row, little_col;
        private Color color;
        private Boolean taken;
        Box(int x, int y, int width, int height, int big_row, int big_col, int little_row, int little_col) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.width = width;
            this.big_row = big_row;
            this.big_col = big_col;
            this.little_row = little_row;
            this.little_col = little_col;
            taken = false;
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
        public Boolean isTaken(){
            return taken;
        }
        public void setTaken(){
            taken = true;
        }
        public void resetTaken(){
            taken = false;
        }
        // Ok, technically this is a setter, but it isn't called setColor, so it doesn't count
        public void changeColor(Color new_color){
            //Add this box to the drawHoverBoxes list
            color = new_color;
        }
    }

    public Box findBox(int x, int y)  { //We use this process quite a bit, so it makes sense to make it a function
        if (game_over && y > again_box.getY() && y < (again_box.getY() + again_box.getHeight()) && x > again_box.getX() && x < (again_box.getX() + again_box.getWidth())) {
            return again_box;
        } else {
            again_box.changeColor(new Color(50,210,50));
        }
        if (!game_over) {
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
                                if (boxes[i][j][k][l].isTaken() || big_box_move[i][j] != ' ') {
                                    return null;
                                } else if ((row_to_play == 0 && col_to_play == 0) || (boxes[i][j][k][l].getBigRow() == row_to_play && boxes[i][j][k][l].getBigCol() == col_to_play)) {
                                    return boxes[i][j][k][l];
                                } else {
                                    return null;
                                }
                            }
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
    public void highlightBox(int x, int y, Color new_color, Container cp) {
        if (my_turn || game_over) {
            Box old_box = hoverBox;
            hoverBox = findBox(x,y);
            if (old_box != hoverBox ) {
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
    }
    
    public void selectBox(int x, int y, Color new_color, Container cp) throws IOException{
        String move = sendBoxPosition(x,y);
        if (game_over) {
            Box temp = findBox(x,y);
            if (temp != null) {
                toServer.write("again");
                toServer.newLine();
                toServer.flush();
                for (int i=0;i<3;i++){
                    for (int j=0;j<3;j++){
                        for (int k=0;k<3;k++){
                            for (int l=0;l<3;l++){
                                boxes[i][j][k][l].resetTaken();
                            }
                        }
                        big_box_move[i][j] = ' ';
                    }
                }
                game_over = false;
                hoverBox = null;
                availableToPlay.clear();
                o_moves.clear();
                big_o_moves.clear();
                x_moves.clear();
                big_x_moves.clear();
                message = "Waiting for the other player...";
                row_to_play = 0;
                col_to_play = 0;
                cp.repaint();
            }
        } else if (move != "" && my_turn) {
            toServer.write(move);
            toServer.newLine();
            toServer.flush();
        }

    }
    /*Sends box position to the server 
     * Returns string for server to receive
     */
    public String sendBoxPosition(int x, int y) {
    	Box temp = findBox(x,y);
    	if (temp != null && ((row_to_play == 0 && col_to_play == 0) || (temp.getBigRow() == row_to_play && temp.getBigCol() == col_to_play))) { //if temp is not null, then display the position of the mouse
        	return (temp.getBigRow()+"," + temp.getBigCol()+ "," + temp.getLittleRow()+ ","+ temp.getLittleCol());
    	} else { //otherwise return nothing (empty string)
    		return "";
    	}
    }

    public void initilizeLines(){
        //-----Initialize Lines-----
        UTTT uttt = new UTTT();
        // everything is based off of these start and offset points,
        // which makes it easy to move everything if we want to
        int x_start = 32;
        int y_start = 102;
        int y_offset = 161;
        int x_offset = 161;
        drawLines.add(new Line2D.Float(x_start+156, y_start+4,   x_start+156, y_start+469));
        drawLines.add(new Line2D.Float(x_start+317, y_start+4,   x_start+317, y_start+469));
        drawLines.add(new Line2D.Float(x_start+4,   y_start+156, x_start+469, y_start+156));
        drawLines.add(new Line2D.Float(x_start+4,   y_start+317, x_start+469, y_start+317));
        for (int i=0;i<3;i++){
            for (int j=0;j<3;j++){
                drawLines.add(new Line2D.Float(x_start+55+(j*x_offset),  y_start+20+(i*y_offset),  x_start+55+(j*x_offset),  y_start+130+(i*y_offset)));
                drawLines.add(new Line2D.Float(x_start+94+(j*x_offset), y_start+20+(i*y_offset),  x_start+94+(j*x_offset), y_start+130+(i*y_offset)));
                drawLines.add(new Line2D.Float(x_start+20+(j*x_offset),  y_start+56+(i*y_offset),  x_start+130+(j*x_offset), y_start+56+(i*y_offset)));
                drawLines.add(new Line2D.Float(x_start+20+(j*x_offset),  y_start+95+(i*y_offset), x_start+130+(j*x_offset), y_start+95+(i*y_offset)));
            }
        }
        
        
        //uttt.new MoveSymbol(90,90,120,30, 120, 60, 90,180);
        // -----Initialize Boxes-----
        for (int i=0;i<3;i++){
            for (int j=0;j<3;j++){
                for (int k=0;k<3;k++){
                    for (int l=0;l<3;l++){
                        boxes[i][j][k][l] = uttt.new Box((x_start+18+(39*l + j*x_offset)), (y_start+19+(39*k + i*y_offset)), 34, 34, i+1, j+1, k+1, l+1);
                    }
                }
                big_boxes[i][j] = uttt.new Box(x_start+(j*x_offset),y_start+(i*y_offset),150,150, i+1, j+1, 0, 0);
                big_box_move[i][j] = ' ';
            }
        }
        again_box.changeColor(new Color(50,210,50));
    }

    public Container setupJFrame() {
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
                g.setColor(new Color(50,210,50));
                for (int i=0;i<availableToPlay.size();i++){
                    Box temp = availableToPlay.get(i);
                    g2.fillRect(temp.getX(), temp.getY(), temp.getWidth(), temp.getHeight());
                }

                //-----Drawing Hover Box-----
                /*g.setColor(new_color);
                g2.fillRect(x, y, width, height);*/
                if (hoverBox != null) {
                    g.setColor(hoverBox.getColor());
                    g2.fillRect(hoverBox.getX(), hoverBox.getY(), hoverBox.getWidth(), hoverBox.getHeight());
                }

                //-----Drawing Board Lines-----
                g2.setStroke(new BasicStroke(10));
                g.setColor(new Color(75,75,75));
                for (int i=0;i<drawLines.size();i++){
                    if (i == 4) {
                        g2.setStroke(new BasicStroke(2));
                    }
                    g2.draw(drawLines.get(i));
                }

                //-------Drawing X or O--------
                g2.setStroke(new BasicStroke(5));
                g.setColor(new Color(0,0,0));
                for (int i=0;i<o_moves.size();i++){
                    if ((i > (o_moves.size()-num_of_last_moves) -1 )&& player_symbol == 'X' && my_turn){
                        g.setColor(new Color(255,0,0));
                    }
                    g2.draw(o_moves.get(i));
                }
                g.setColor(new Color(0,0,0));
                for (int i=0;i<x_moves.size();i++){
                    if ((i > (x_moves.size()-(2*num_of_last_moves)-1) ) && player_symbol == 'O' && my_turn){
                        g.setColor(new Color(255,0,0));
                    }
                    g2.draw(x_moves.get(i));
                }
                g2.setStroke(new BasicStroke(8));
                g.setColor(new Color(0,0,0));
                for (int i=0;i<big_o_moves.size();i++){
                    g2.draw(big_o_moves.get(i));
                }
                for (int i=0;i<big_x_moves.size();i++){
                    g2.draw(big_x_moves.get(i));
                }

                //---------Drawing Text---------
                g.setColor(new Color(0,0,0));
                Font font = new Font("Serif", Font.PLAIN, 30);
                g2.setFont(font);
                g2.drawString(message, 125 ,75);

                if (game_over) {
                    g.setColor(again_box.getColor());
                    g2.fillRect(again_box.getX(), again_box.getY(), again_box.getWidth(), again_box.getHeight());
                    g.setColor(new Color(0,0,0));
                    g2.drawString("Again?", 310 ,75);
                }

            }

        });
        
        // Now we add MouseListeners to that component
        cp.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) { }
            //Unused methods, but still need to override them
            @Override
            public void mouseEntered(MouseEvent e) { }
            @Override
            public void mouseExited(MouseEvent e) { }
            @Override
            public void mouseReleased(MouseEvent e) { }
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    selectBox(e.getX(), e.getY(), new Color(0,0,255), cp);
                } catch (IOException ex) {
                    System.err.println(ex);
                }
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
        return cp;
    }

    public void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 8001);
            fromServer =  new BufferedReader (new InputStreamReader(socket.getInputStream()));
            toServer =  new BufferedWriter (new OutputStreamWriter(socket.getOutputStream()));
            message = "Waiting for the other player... ";
            player_symbol = fromServer.readLine().charAt(0);

        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void readInput(Container cp) throws IOException{
        final String input = fromServer.readLine();
        num_of_last_moves = 0;
        if (input.charAt(0) == player_symbol) { 
            my_turn = false;
            hoverBox = null;
        } else {
            my_turn = true;
        }
        switch (input.charAt(1)) {
            case '1' :
                game_over = true;
                message = (player_symbol == 'X') ? "You Win!!!" : "You lose!";
                break;
            case '2' :
                game_over = true;
                message = (player_symbol == 'O') ? "You Win!!!" : "You loose!";
                break;
            case '3' :
                game_over = true;
                message = "It's a draw!";
                break;
            case '4' :
                game_over = false;
                message = (my_turn) ? "Your Turn, Player " + player_symbol : "Waiting for other player...";
                break;
        }
        for (int i=2;i<input.length();i=i+4) {
            num_of_last_moves++;
            if (input.charAt(i+2) != '0') {
                int a = input.charAt(i)-49;
                int b = input.charAt(i+1)-49;
                int c = input.charAt(i+2)-49;
                int d = input.charAt(i+3)-49;
                Box temp = boxes[a][b][c][d];
                temp.setTaken();
                if (input.charAt(0) == 'X') {
                    x_moves.add(new Line2D.Float(temp.getX()+5, temp.getY()+5, temp.getX()+temp.getWidth()-5, temp.getY()+temp.getHeight()-5));
                    x_moves.add(new Line2D.Float(temp.getX()+5, temp.getY()+temp.getHeight()-5, temp.getX()+temp.getWidth()-5, temp.getY()+5));
                } else {
                    o_moves.add(new Ellipse2D.Double(temp.getX()+5, temp.getY()+5, temp.getWidth()-10, temp.getHeight()-10));
                }
                
            } else {
                num_of_last_moves--;
                int a = input.charAt(i)-49;
                int b = input.charAt(i+1)-49;
                Box temp = big_boxes[a][b];
                big_box_move[a][b] = input.charAt(0);
                if (input.charAt(0) == 'X') {
                    big_x_moves.add(new Line2D.Float(temp.getX()+5, temp.getY()+5, temp.getX()+temp.getWidth()-5, temp.getY()+temp.getHeight()-5));
                    big_x_moves.add(new Line2D.Float(temp.getX()+5, temp.getY()+temp.getHeight()-5, temp.getX()+temp.getWidth()-5, temp.getY()+5));
                } else {
                    big_o_moves.add(new Ellipse2D.Double(temp.getX()+5, temp.getY()+5, temp.getWidth()-10, temp.getHeight()-10));
                }
            }
        }
        if (input.length() != 2) {
            row_to_play = input.charAt(4)-48;
            col_to_play = input.charAt(5)-48;
            availableToPlay.clear();
            if (big_box_move[row_to_play-1][col_to_play-1] == ' ') {
                if (my_turn) {
                    availableToPlay.add(big_boxes[row_to_play-1][col_to_play-1]);
                }
            } else {
                for (int i=0; i<3; i++) {
                    for (int j=0; j<3; j++){
                        if (big_box_move[i][j] == ' ' && my_turn) {
                            availableToPlay.add(big_boxes[i][j]);
                        }
                    }
                }
                row_to_play = 0;
                col_to_play = 0;
            }
        }
        cp.repaint();
    }


}
