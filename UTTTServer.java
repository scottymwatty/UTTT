/**
 *  Written by: Jason Salas
 *  Last edited: 11/9/16
 *  Assignment: Ultimate Tic-Tac-Toe - Server Side
 */

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class UTTTServer extends JFrame implements UTTTConstants
{   
   public static void main(String[] args)
   {
       UTTTServer frame = new UTTTServer();
   }
   
   public UTTTServer()
   {
       JTextArea jtaLog = new JTextArea();
       
       //Scroll Pane to scroll through text
       JScrollPane scrollPane = new JScrollPane(jtaLog);
       
       //implement scroll pane in the frame
       add(scrollPane, BorderLayout.CENTER);
       
       //Define parameters of GUI (close, size, title, visibility)
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       setSize(800,800);
       setTitle("Ultimate Tic Tac Toe Server");
       setVisible(true);
       
       try
       {
           //create server socket
           ServerSocket serverSocket = new ServerSocket(8000);
           jtaLog.append(new Date() + ": Server starts at socket 8000\n");
           
           //Session Number
           int SessionNum = 1;  //Can only be 1 (for now...)
           
           //Need to create session for 2 people
           while(true)
           {
               jtaLog.append(new Date() + 
                       "Waiting for players to join session number " + SessionNum + 'n');
               //connect to player 1
               Socket player1 = serverSocket.accept();
               
               jtaLog.append(new Date() + 
                       ": Player 1 has joined session number" + SessionNum + 'n');
               jtaLog.append("Player 1's IP is " + player1.getInetAddress().getHostAddress() + '\n');
               
               //Let the first player know they are player 1
               new DataOutputStream(player1.getOutputStream()).writeInt(Player1);
           
               //Connect to player 2
               Socket player2 = serverSocket.accept();
               
               jtaLog.append(new Date() + 
                       ": Player 2 has joined session number " + SessionNum + '\n');
           
               jtaLog.append("Player 2's IP is " + player2.getInetAddress().getHostAddress() + '\n');
               
               //Let the second player know they are player 2
               new DataOutputStream(player2.getOutputStream()).writeInt(Player2);
               
               //Display session and increment session #
               jtaLog.append(new Date() + ": Begin thred for session number " + SessionNum++ + '\n');
               
               //Create thread for this session of 2 players
               HandleASession task = new HandleASession(player1,player2);
               
               //Start the thread
               new Thread(task).start();
           }
           
       }
       
       catch(IOException ex)
       {
           System.err.println(ex);
       }
       
   }
}

class HandleASession implements Runnable
{
    private Socket player1, player2;
    
    //create and initialize cells (3 Dimensional Array)
    private char[][][] cell = new char[3][3][9];    //either [3][3][3] or [3][3][9]
    
    private DataInputStream fromPl;
    private DataOutputStream toP1;
    private DataInputStream fromP2;
    private DataOutputStream toP2;
    
    //Continue to play? Bool
    private boolean continueToPlay = true;
    
    //Construct thread
    public HandleASession(Socket player1, Socket player2)
    {
        this.player1 = player1;
        this.player2 = player2;
        
        //initialize cells (use cells from UTTT client)
        for (int i=0;i<3;i++)
        {
            for (int j=0;j<3;j++)
            {
                for (int k=0;k<3;k++)
                {
                    for (int l=0;l<3;l++)
                    {
                        cell[i][j][k] = ' ';
                    }
        }   }   }
    }
    
    public void run()
    {
        try
        {   
            //create data input and output streams for player1
            DataInputStream fromPlayer1 = new DataInputStream(player1.getInputStream());
            DataOutputStream toPlayer1 = new DataOutputStream(player1.getOutputStream());
            //create data input and output streams for player 2
            DataInputStream fromPlayer2 = new DataInputStream(player2.getInputStream());
            DataOutputStream toPlayer2 = new DataOutputStream(player2.getOutputStream());
            
            //Notify player1 to start
            toPlayer1.writeInt(1);
            
            while(true)
            {
                //Global variables (aren't transferring in from UTTTConstants file, so have to declare here...redundant)
                int Player1 = 1;  //Player 1 represented as 1
                int Player2 = 2;  //Player 2 represented as 2
                int Player1_Won = 1;  //Player 1 winning is 1
                int Player2_Won = 2;  //Player 2 winning is 2
                int Draw = 3;     //Draw is represented as 3
                int Continue = 4; //Continue state is represented as 4
                char[] xCharArray = new char['X'];
                char[] oCharArray = new char['O'];
                
                //get move from P1
                int bigRow = fromPlayer1.readInt();
                int bigColumn = fromPlayer1.readInt();
                int smallGrid = fromPlayer1.readInt();
                
                cell[bigRow][bigColumn][smallGrid] = 'X';
                
                if(isWon(xCharArray, 'X'))
                {
                    toPlayer1.writeInt(Player1_Won);
                    toPlayer2.writeInt(Player1_Won);
                    sendMove(toPlayer2, bigRow, bigColumn, smallGrid);
                    break;
                }
                else if (isFull())
                {
                    toPlayer1.writeInt(Draw);
                    toPlayer2.writeInt(Draw);
                    sendMove(toPlayer2, bigRow, bigColumn, smallGrid);
                    break;
                }
                else 
                {
                    //Tell player 2 its their turn
                    toPlayer2.writeInt(Continue);
                    //Send player 1's row, column, and grid
                    sendMove(toPlayer2, bigRow, bigColumn, smallGrid);
                }
                
                //Get player 2's move
                bigRow = fromPlayer2.readInt();
                bigColumn = fromPlayer2.readInt();
                smallGrid = fromPlayer2.readInt();
                cell[bigRow][bigColumn][smallGrid] = 'O';
                
                if(isWon(oCharArray,'O'))
                {
                    toPlayer1.writeInt(Player2_Won);
                    toPlayer2.writeInt(Player2_Won);
                    sendMove(toPlayer1, bigRow, bigColumn, smallGrid);
                    break;
                }
                else
                {
                    //Notify player 1 it's their turn
                    toPlayer1.writeInt(Continue);
                    //Send player 2's row, column, and grid
                    sendMove(toPlayer1, bigRow, bigColumn, smallGrid);
                }
            }
        }
        catch(IOException ex)
        {
            System.err.println(ex);
        }        
    }
        private void sendMove(DataOutputStream out, int row, int column, int grid) throws IOException
    {
            out.writeInt(row);
            out.writeInt(column);
            out.writeInt(grid);
    }
        
        //find out if all the cells are taken
        private boolean isFull()
        {
            for (int i = 0; i<3; i++)
                for(int j = 0; j<3; j++)
                    for (int k = 0; k<3; k++)
                        if(cell[i][j][k] == ' ') //if any cells are empty
            {
                return false;
            }
            return true;
        }
        
        //Find out which player's token won
        private boolean isWon (char[] token, char gridToken)
        {
            //Check rows
            for(int i = 0; i<3; i++)
                if((cell[i][0]==token) && (cell[i][1]==token) && (cell[i][2]==token))
                    {return true;}
            //Check columns
            for(int j = 0; j<3; j++)
                if((cell[0][j]==token) && (cell[1][j]==token) && (cell[2][j]==token))
                    {return true;}
            /*Check small grids are filled
            for(int k = 0; k<9; k++)
                if((cell[0][0][k]==gridToken) && (cell[1][1][k]==gridToken) && (cell[2][2][k]==gridToken))
                    {return true;}
            */
            //Check major diagonal of small grid
            if((cell[0][0]==token) && (cell[1][1]==token) && (cell[2][2]==token))
                {return true;}
            //Check subdiagonal of small grid
            if((cell[0][2]==token) && (cell[1][1]==token) && (cell[2][0]==token))
                {return true;}
            
            //Check big grid rows for 3
            for(int x = 0; x<3; x++)
                for(int z = 0; z<9; z++)
                    if((cell[x][0][z]==gridToken) && (cell[x][1][z]==gridToken) && (cell[x][2][z]==gridToken))
                        {return true;}
                        
            //Check big grid columns for 3
            for(int y = 0; y<3; y++)
                for(int z =0; z<9; z++)
                    if((cell[0][y][z]==gridToken) && (cell[1][y][z]==gridToken) && (cell[2][y][z])==gridToken)
                        {return true;}
            
            //Check big grid major diagonal
            if((cell[0][0][0]==gridToken) && (cell[1][1][1]==gridToken) && (cell[2][2][2]==gridToken))
                {return true;}
            
            //Check big grid subdiagonal
            if((cell[0][0][2]==gridToken) && (cell[1][1][1]==gridToken) && (cell[2][0][0]==gridToken))
                {return true;}
            
            //Everything's checked, but no winners yet
            return false;
            
            //TODO create logic for forcing player to play in specific small grids
            
        }
            
}