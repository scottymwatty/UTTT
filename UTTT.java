import java.awt.*;
import java.awt.geom.Line2D;
import javax.swing.*;

public class UTTT {
 
    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;
    public static final String TITLE = "Ultamite Tic Tac Toe!";

    public static void main(String[] args){
        JFrame jf = new JFrame(TITLE);
        Container cp = jf.getContentPane();
        cp.add(new JComponent() {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                int x_start = 45;
                int y_start = 45;
                g2.setStroke(new BasicStroke(10));
                g2.draw(new Line2D.Float(x_start+223, y_start+5,   x_start+223, y_start+670));
                g2.draw(new Line2D.Float(x_start+453, y_start+5,   x_start+453, y_start+670));
                g2.draw(new Line2D.Float(x_start+5,   y_start+223, x_start+670, y_start+223));
                g2.draw(new Line2D.Float(x_start+5,   y_start+453, x_start+670, y_start+453));
                g2.setStroke(new BasicStroke(2));
                int y_offset = 230;
                int x_offset = 230;
                for (int i=0;i<3;i++){
                    for (int j=0;j<3;j++){
                        g2.draw(new Line2D.Float(x_start+79+(j*x_offset),  y_start+28+(i*y_offset),  x_start+79+(j*x_offset),  y_start+186+(i*y_offset)));
                        g2.draw(new Line2D.Float(x_start+134+(j*x_offset), y_start+28+(i*y_offset),  x_start+134+(j*x_offset), y_start+186+(i*y_offset)));
                        g2.draw(new Line2D.Float(x_start+28+(j*x_offset),  y_start+80+(i*y_offset),  x_start+186+(j*x_offset), y_start+80+(i*y_offset)));
                        g2.draw(new Line2D.Float(x_start+28+(j*x_offset),  y_start+135+(i*y_offset), x_start+186+(j*x_offset), y_start+135+(i*y_offset)));
                    }
                }
                for (int i=0;i<3;i++){
                    for (int j=0;j<3;j++){
                        for (int k=0;k<3;k++){
                            for (int l=0;l<3;l++){
                                g.setColor(new Color((50*i)+50,(50*j)+50,(100)));
                                g2.fillRect(x_start+27+(55*i + l*x_offset), y_start+27+(55*j + k*y_offset), 50, 50);
                            }
                        }
                        /*g.setColor(new Color(75,75*i,75*j));
                        /*g2.fillRect(x_start+(j*x_offset),y_start+(i*y_offset),215,215);*/
                    }
                }
                
                /*g.setColor(Color.RED);
                g2.fillRect(114, 74, 50, 50);
                g.setColor(Color.YELLOW);
                g2.fillRect(169, 74, 50, 50);*/
            }
        });
        jf.setSize(WIDTH, HEIGHT);
        jf.setVisible(true);
    }   
}