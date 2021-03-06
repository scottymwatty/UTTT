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
       setSize(550,650);
       setTitle("Ultimate Tic Tac Toe Server");
       setVisible(true);
       
       try
       {
           //create server socket
           ServerSocket serverSocket = new ServerSocket(8001);
           jtaLog.append(new Date() + ": Server starts at socket 8001\n");
           
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
               BufferedWriter temp  = new BufferedWriter (new OutputStreamWriter(player1.getOutputStream()));
               temp.write(Player1);
               temp.newLine();
               temp.flush();
               //Connect to player 2
               Socket player2 = serverSocket.accept();
               
               jtaLog.append(new Date() + 
                       ": Player 2 has joined session number " + SessionNum + '\n');
           
               jtaLog.append("Player 2's IP is " + player2.getInetAddress().getHostAddress() + '\n');
               
               //Let the second player know they are player 2
               temp = new BufferedWriter (new OutputStreamWriter(player2.getOutputStream()));
               temp.write(Player2);
               temp.newLine();
               temp.flush();

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

class HandleASession implements Runnable, UTTTConstants
{
    private Socket player1, player2;
    private BufferedReader fromPlayer1, fromPlayer2;
    private BufferedWriter toPlayer1, toPlayer2;

    //create and initialize cells (3 Dimensional Array)
    private char[][][][] cell = new char[3][3][3][3]; 
    private char[][] bigBox = new char[3][3];
    
    private BufferedReader fromPl;
    private DataOutputStream toP1;
    private BufferedReader fromP2;
    private DataOutputStream toP2;
    
    //Continue to play? Bool
    private boolean continueToPlay = true;
    
    //Construct thread
    public HandleASession(Socket player1, Socket player2)  throws IOException
    {
        this.player1 = player1;
        this.player2 = player2;
        
        //create data input and output streams for player1
        fromPlayer1 = new BufferedReader (new InputStreamReader(player1.getInputStream()));
        toPlayer1 = new BufferedWriter (new OutputStreamWriter(player1.getOutputStream()));
        //create data input and output streams for player 2
        fromPlayer2 = new BufferedReader (new InputStreamReader(player2.getInputStream()));
        toPlayer2 = new BufferedWriter (new OutputStreamWriter(player2.getOutputStream()));
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
                }
                bigBox[i][j] = ' ';
            }
        }
    }
    
    public void run()
    {
     while(true)
       {
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
                }
                bigBox[i][j] = ' ';
            }
        }
           
        try
        {   
            //Notify player1 to start
            sendMove("O4"); //Send O, kinda like if O had just played, and continue
            
            while(true)
            {
                
                //get move from P1
               String input[] = fromPlayer1.readLine().split(",");
               int bigRow = Integer.parseInt(input[0]);
               int bigColumn = Integer.parseInt(input[1]);
               int smallRow = Integer.parseInt(input[2]);
               int smallColumn = Integer.parseInt(input[3]);
               cell[bigRow-1][bigColumn-1][smallRow-1][smallColumn-1] = 'X';
               
               //Reparse the cell array back into a string to be sent
               String output1[] = new String[4];
               output1[0] = Integer.toString(bigRow);
               output1[1] = Integer.toString(bigColumn);
               output1[2] = Integer.toString(smallRow);
               output1[3] = Integer.toString(smallColumn);
               
                String fileOutput1 = output1[0] + output1[1] + output1[2] + output1[3];
               
               String won;
                if ((won = sGridIsWon('X')) != null)
                {
                  fileOutput1 = fileOutput1 + won + cascadeMoves('X', won);
                  
                   if(matchIsWon('X'))
                {
                    fileOutput1 = "X" + Player1_Won + fileOutput1;
                    sendMove(fileOutput1);
                    break;
                }
                   else
                   {
                       fileOutput1 = "X" + Continue + fileOutput1;
                       sendMove(fileOutput1);
                   }
                } 
                
                else if (isFull())
                {
                    fileOutput1 = "X" + Draw + fileOutput1;
                    sendMove(fileOutput1);
                    break;
                }
                else 
                {
                    fileOutput1 = "X" + Continue + fileOutput1;
                    sendMove(fileOutput1);
                }
                
                //Get player 2's move
                //Unpack the string into an Integer Array (cell) to be utilized for logic
                String input2[] = fromPlayer2.readLine().split(",");
                int bigRow2 = Integer.parseInt(input2[0]);
                int bigColumn2 = Integer.parseInt(input2[1]);
                int smallRow2 = Integer.parseInt(input2[2]);
                int smallColumn2 = Integer.parseInt(input2[3]);
                cell[bigRow2-1][bigColumn2-1][smallRow2-1][smallColumn2-1] = 'O';
                
                //Reparse the entire cell variable back into a string seperated by 4 commas to be sent 
                String output2[] = new String[4];
                output2[0] = Integer.toString(bigRow2);
                output2[1] = Integer.toString(bigColumn2);
                output2[2] = Integer.toString(smallRow2);
                output2[3] = Integer.toString(smallColumn2);
                
                
                String fileOutput2 = output2[0] + output2[1] + output2[2]+ output2[3];   //need to add commas in between numbers
                
                if ((won = sGridIsWon('O')) != null)
                {
                  fileOutput2 = fileOutput2 + won + cascadeMoves('O', won);
                  
                  if(matchIsWon('O'))
                {
                    fileOutput2 = "O" + Player2_Won + fileOutput2;
                    sendMove(fileOutput2);
                    break;
                }
                  else
                  {
                      fileOutput2 = "O" + Continue + fileOutput2;
                      sendMove(fileOutput2);
                  }
                
                } 
                else
                {
                    fileOutput2 = "O" + Continue + fileOutput2;
                    sendMove(fileOutput2);
                }
            }
          String p1Again = fromPlayer1.readLine();
          String p2Again = fromPlayer2.readLine();
          
          if((!p1Again.equals("again")) || (!p2Again.equals("again")))
          {
              break;
          }
          
        }
        catch(IOException ex)
        {
            System.err.println(ex);
            break;
        }  
        
        
      }
    }
    
    
    
    
    
    
        //send the move as a string to parsed by client
        private void sendMove(String output) throws IOException
    {
            toPlayer1.write(output);
            toPlayer1.newLine();
            toPlayer1.flush();
            toPlayer2.write(output);
            toPlayer2.newLine();
            toPlayer2.flush();
    }
        
        //find out if all the cells are taken
        private boolean isFull()
        {
            for (int i = 0; i<3; i++)
                for(int j = 0; j<3; j++)
                    for (int k = 0; k<3; k++)
                        for(int l=0; l<3; l++)
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
            for(int i = 0; i<3; i++)
            {
                if((bigBox[0][i]==gridToken) && (bigBox[1][i]==gridToken) && (bigBox[2][i]==gridToken))
                {return true;}
            }
                        
            //Check big grid columns for 3
            for(int i = 0; i<3; i++)
            {
                if((bigBox[i][0]==gridToken) && (bigBox[i][1]==gridToken) && (bigBox[i][2]==gridToken))
                {return true;}
            }
            
            //Check big grid major diagonal
            for(int i =0; i<3; i++)
            {
                if((bigBox[0][0]==gridToken) && (bigBox[1][1]==gridToken) && (bigBox[2][2]==gridToken))
                {return true;}
            }
            
            //Check big grid subdiagonal
            for(int i =0; i<3; i++)
            {
                if((bigBox[0][2]==gridToken) && (bigBox[1][1]==gridToken) && (bigBox[2][0]==gridToken))
                {return true;}
            }
            
            return false;
        }
        
        //Find out if a player has won a small grid
        private String sGridIsWon(char gridToken)
        {
            String bigCoor, ZeroZero;
            ZeroZero = "00";
            
            //Check small grid rows and return the x,y coordinate that is won
            for(int i = 0; i<3; i++)
                for(int x = 0; x<3; x++)
                    for(int y = 0; y<3; y++)
                if((cell[x][y][i][0]==gridToken) && (cell[x][y][i][1]==gridToken) && (cell[x][y][i][2]==gridToken))
                    {
                      if (bigBox[x][y]==' ') {
                        String xCoord = Integer.toString(x+1);
                        String yCoord = Integer.toString(y+1);
                        bigCoor = xCoord.concat(yCoord);
                        bigBox[x][y] = gridToken;
                        return bigCoor.concat(ZeroZero);
                      }
                    }
            
            //Check small grid columns and return the x,y coordinate that is won
            for(int j = 0; j<3; j++)
                for(int x = 0; x<3; x++)
                    for(int y = 0; y<3; y++)
                if((cell[x][y][0][j]==gridToken) && (cell[x][y][1][j]==gridToken) && (cell[x][y][2][j]==gridToken))
                    {
                      if (bigBox[x][y]==' ') {
                        String xCoord = Integer.toString(x+1);
                        String yCoord = Integer.toString(y+1);
                        bigCoor = xCoord.concat(yCoord);
                        bigBox[x][y] = gridToken;
                        return bigCoor.concat(ZeroZero);
                      }
                    }
            
            //Check major diagonal of small grid and return the x,y coordinate that is won
            for(int x = 0; x<3; x++)
                for(int y = 0; y<3; y++)
                    if((cell[x][y][0][0]==gridToken) && (cell[x][y][1][1]==gridToken) && (cell[x][y][2][2]==gridToken))
                {
                  if (bigBox[x][y]==' ') {
                    String xCoord = Integer.toString(x+1);
                    String yCoord = Integer.toString(y+1);
                    bigCoor = xCoord.concat(yCoord);
                    bigBox[x][y] = gridToken;
                    return bigCoor.concat(ZeroZero);
                  }
                }
            
            //Check subdiagonal of small grid
            for(int x =0; x<3; x++)
                for(int y = 0; y<3; y++)
                    if((cell[x][y][0][2]==gridToken) && (cell[x][y][1][1]==gridToken) && (cell[x][y][2][0]==gridToken))
                {
                  if (bigBox[x][y]==' ') {
                    String xCoord = Integer.toString(x+1);
                    String yCoord = Integer.toString(y+1);
                    bigCoor = xCoord.concat(yCoord);
                    bigBox[x][y] = gridToken;
                    return bigCoor.concat(ZeroZero);
                  }
                }
            
            return null;
            
        }

        private String cascadeMoves(char player, String bigMove) {
          String output = "";
          int row = Character.getNumericValue(bigMove.charAt(0));
          int col = Character.getNumericValue(bigMove.charAt(1));
          row = row - 1;
          col = col - 1;

          for(int x = 0; x<3; x++) 
          {
            for(int y = 0; y<3; y++) 
            {
              if (cell[x][y][row][col] == ' ') 
              {
                cell[x][y][row][col] = player;
                output = output + (x+1) + (y+1) + (row+1) + (col+1);
              }
            }
          }
          String won;
          if((won = sGridIsWon(player)) != null)
          {
              output = output + won + cascadeMoves(player, won);
          }
          return output;
        }
            
}