/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ests.ma.multichatnewV2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author azrix
 */
public class ChatServerV2 {
    
   private final static int PORT = 9001;
   private static int nbreClient =0;
   private final static int MAXCLIENT = 30;
   private static ServerSocket listner;
   private static HashSet<String> names = new HashSet<String>();
   private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
   private static Thread CastListClient;
   public static void main(String [] Args) throws IOException{
       
       
       
       try{
           
           listner = new ServerSocket(PORT);
           
           System.out.println("Server Chat starting\n");
           System.out.println("PORT LISTENING "+PORT);
           while(true && nbreClient <=MAXCLIENT){
                   
                        Socket s = listner.accept();
                        nbreClient++;
                        System.out.println("Client has been connected..");
                        new handler(s).start();
                        
                       
                        CastListClient = new Thread(){
                            @Override
                            public void run(){
                                 int SizeNames = 0;
                               while(true){
                                     try {
                                         Thread.currentThread().sleep(5000);
                                     } catch (InterruptedException ex) {
                                         Logger.getLogger(ChatServerV2.class.getName()).log(Level.SEVERE, null, ex);
                                     }
                                     if(SizeNames != writers.size()){
                                       System.out.println("Send Update list Client NumberofClient "+SizeNames+" NumberofClientServer : "+writers.size());
                                        writers.forEach((writer) -> {    
                                                     writer.println("ListClient:"+names.toString());
                                        });
                                        SizeNames = writers.size();
                                     }else if(SizeNames == writers.size()){
                                         System.out.println("No Update Needed "+SizeNames+" NumberofClientServer : "+writers.size());
                                     }
                                }
                            }
                        };
           }

          
       
       }catch(IOException io){
           System.err.println(io.getMessage());
           
       }finally{
          listner.close();
       }
   }
   
   private static class handler extends Thread{
       private String name;
       private Socket socket;
       private BufferedReader in;
       private PrintWriter out;
       private Thread UpdateListClient;
       
        public handler(Socket socket) {
            this.socket = socket;
        }
        
       @Override
        public void run(){
            
           try {
               in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
               out = new PrintWriter(socket.getOutputStream(),true);
               
               while(true){
                  // System.out.println("Waiting for CLient Name...");
                   out.println("SUBMITNAME");
                   name = in.readLine();
                   if (name == null) {
                       return;
                   } 
                     synchronized(names){
                         if(!names.contains(name))
                             {
                                names.add(name);
                                out.println("NAMEACCEPTED");
                                writers.add(out);
                                UpdateListClient = CastListClient;
                                break;
                            }else{
                                out.println("NAMENOTACCEPTED");
                            }
                     }
               }
               
               UpdateListClient.start();
               //Update list client 
               
               System.out.println("Client Name has been saved : ");
               while(true){
                   String input = in.readLine();
                   System.out.println(input);
                   if(input==null){
                       return;
                                  }
                   writers.forEach((writer) -> {
                       writer.println(name+" : "+input);
                                  }
                   );
               }
           }catch(SocketException ex){
               writers.remove(out); 
               names.remove(name);
               writers.forEach((writer) -> {
                       writer.println(name+" : disconnected");
                   });
               Thread.currentThread().destroy();
           } catch (IOException ex) {
               Logger.getLogger(ChatServerV2.class.getName()).log(Level.SEVERE, null, ex);
           }   
        }   
   }
}
