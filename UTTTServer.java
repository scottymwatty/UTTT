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
                       "Waiting for players to join session number " + SessionNum + '\n');
               //connect to player 1
               Socket player1 = serverSocket.accept();
               
               jtaLog.append(new Date() + 
                       ": Player 1 has joined session number" + SessionNum + '\n');
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
               jtaLog.append(new Date() + ": Begin thread for session number " + SessionNum++ + '\n');
               
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
    private char[][][][] cell = new char[3][3][3][3];    
    
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
                        cell[i][j][k][l] = ' ';
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
               String input[] = fromPlayer1.readLine().split(",");
               int bigRow = Integer.parseInt(input[0]);
               int bigColumn = Integer.parseInt(input[1]);
               int smallRow = Integer.parseInt(input[2]);
               int smallColumn = Integer.parseInt(input[3]);
  
               cell[bigRow][bigColumn][smallRow][smallColumn] = 'X';
               
               //Reparse the cell array back into a string to be sent
               String output1[] = new String[4];
               output1[0] = Integer.toString(bigRow);
               output1[1] = Integer.toString(bigColumn);
               output1[2] = Integer.toString(smallRow);
               output1[3] = Integer.toString(smallColumn);
               
               String fileOutput1 = output1[0].concat(output1[1].concat(output1[2]).concat(output1[3]));
                
                if(matchIsWon('X'))
                {
                    toPlayer1.writeInt(Player1_Won);
                    toPlayer2.writeInt(Player1_Won);
                    sendMove(toPlayer2, fileOutput1);
                    break;
                }
                else if (isFull())
                {
                    toPlayer1.writeInt(Draw);
                    toPlayer2.writeInt(Draw);
                    sendMove(toPlayer2, fileOutput1);
                    break;
                }
                else 
                {
                    //Tell player 2 its their turn
                    toPlayer2.writeInt(Continue);
                    //Send player 1's row, column, and grid
                    sendMove(toPlayer2, fileOutput1);
                }
                
                //Get player 2's move
                //Unpack the string into an Integer Array (cell) to be utilized for logic
                String input2[] = fromPlayer2.readLine().split(",");
                int bigRow2 = Integer.parseInt(input2[0]);
                int bigColumn2 = Integer.parseInt(input2[1]);
                int smallRow2 = Integer.parseInt(input2[2]);
                int smallColumn2 = Integer.parseInt(input[3]);
                cell[bigRow][bigColumn][smallRow][smallColumn] = 'O';
                
                //Reparse the entire cell variable back into a string seperated by 4 commas to be sent 
                String output2[] = new String[4];
                output2[0] = Integer.toString(bigRow2);
                output2[1] = Integer.toString(bigColumn2);
                output2[2] = Integer.toString(smallRow2);
                output2[3] = Integer.toString(smallColumn2);
                
                String fileOutput2 = output2[0].concat(output2[1].concat(output2[2]).concat(output2[3]));
                
                if(matchIsWon('O'))
                {
                    toPlayer1.writeInt(Player2_Won);
                    toPlayer2.writeInt(Player2_Won);
                    sendMove(toPlayer1, fileOutput2);
                    break;
                }
                else
                {
                    //Notify player 1 it's their turn
                    toPlayer1.writeInt(Continue);
                    //Send player 2's row, column, and grid
                    sendMove(toPlayer1, fileOutput2);
                }
            }
        }
        catch(IOException ex)
        {
            System.err.println(ex);
        }        
    }
        //TODO change this to only send a string to the client
        private void sendMove(DataOutputStream out, String output) throws IOException
    {
            out.writeChars(output);
    }
        
        //find out if all the cells are taken
        private boolean isFull()
        {
            for (int i = 1; i<4; i++)
                for(int j = 1; j<4; j++)
                    for (int k = 1; k<4; k++)
                        for(int l=1; l<4; l++)
                        if(cell[i][j][k][l] == ' ') //if any cells or grids are filled
            {
                return false;
            }
            return true;
        }
        
        //Find out if a player has won the entire match
        private boolean matchIsWon(char gridToken)
        {
            
            //Check big grid rows for 3
            for(int i = 1; i<4; i++)
                for(int x = 1; x<4; x++)
                    for(int y = 1; y<4; y++)
                        if((cell[1][i][x][y]==gridToken) && (cell[2][i][x][y]==gridToken) && (cell[3][i][x][y]==gridToken))
                            {return true;}
                        
            //Check big grid columns for 3
            for(int j = 1; j<4; j++)
                for(int x = 1; x<4; x++)
                    for(int y =1; y<4; y++)
                        if((cell[j][1][x][y]==gridToken) && (cell[j][2][x][y]==gridToken) && (cell[j][3][x][y])==gridToken)
                            {return true;}
            
            //Check big grid major diagonal
            for(int x = 1; x<4; x++)
                for(int y = 1; y<4; y++)
                    if((cell[1][1][x][y]==gridToken) && (cell[2][2][x][y]==gridToken) && (cell[3][3][x][y]==gridToken))
                        {return true;}
            
            //Check big grid subdiagonal
            for(int x = 1; x<4; x++)
                for(int y = 1; y<4; y++)
                    if((cell[1][3][x][y]==gridToken) && (cell[2][2][x][y]==gridToken) && (cell[3][1][x][y]==gridToken))
                        {return true;}
            
            return false;
        }
        
        //Find out if a player has won a small grid
        private boolean sGridisWon(char gridToken)
        {
            //Check small grid rows
            for(int i = 1; i<4; i++)
                for(int x = 1; x<4; x++)
                    for(int y = 1; y<4; y++)
                if((cell[x][y][i][1]==gridToken) && (cell[x][y][i][2]==gridToken) && (cell[x][y][i][3]==gridToken))
                    {return true;}
            
            //Check small grid columns
            for(int j = 1; j<4; j++)
                for(int x = 1; x<4; x++)
                    for(int y = 1; y<4; y++)
                if((cell[x][y][1][j]==gridToken) && (cell[x][y][2][j]==gridToken) && (cell[x][y][3][j]==gridToken))
                    {return true;}
            
            //Check major diagonal of small grid
            for(int x = 1; x<4; x++)
                for(int y = 1; y<4; y++)
                    if((cell[x][y][1][1]==gridToken) && (cell[x][y][2][2]==gridToken) && (cell[x][y][3][3]==gridToken))
                {return true;}
            
            //Check subdiagonal of small grid
            for(int x =1; x<4; x++)
                for(int y = 1; y<4; y++)
                    if((cell[x][y][1][3]==gridToken) && (cell[x][y][2][2]==gridToken) && (cell[x][y][3][1]==gridToken))
                {return true;}
            
            //Everything's checked, but no winners yet
            return false;
            
        }
            
}