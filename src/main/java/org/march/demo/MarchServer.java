package org.march.demo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.UUID;

import org.march.sync.Leader;
import org.march.sync.channel.OutboundChannel;
import org.march.sync.transform.Transformer;

public class MarchServer implements Runnable {

    private ServerSocket serverSocket;
    
    private Leader leader;

    public MarchServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(120000);
        
        leader = new Leader(new Transformer()); 
    }

    public void run() {
        System.out.println("Waiting for clients on port " + serverSocket.getLocalPort() + "...");

        while (true) {
            try {              
                Socket socket = serverSocket.accept();
                System.out.println("Client connected ...");
                
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());                
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                
                
                UUID id = (UUID)in.readObject();
                System.out.println("I am " + id);
                
                leader.subscribe(id);
                
                OutboundChannel channel = leader.getOutbound(id);
                
                (new Thread(new ChannelConnector(channel, in, out))).start(); 
                
                System.out.println("Accepted connection for: " + id);
              
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            Thread t = new Thread(new MarchServer(10101));
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
