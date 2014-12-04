package org.march.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import org.march.data.Command;
import org.march.data.CommandException;
import org.march.data.Constant;
import org.march.data.Data;
import org.march.data.ObjectException;
import org.march.data.Pointer;
import org.march.data.command.Construct;
import org.march.data.command.Insert;
import org.march.data.command.Set;
import org.march.data.command.Type;
import org.march.sync.CommandHandler;
import org.march.sync.Member;
import org.march.sync.MemberException;
import org.march.sync.channel.OutboundChannel;
import org.march.sync.transform.Transformer;

public class MarchClient {
    
    String host;

    /**
     * @param args
     */
    public static void main(String[] args) {
       (new MarchClient()).start(args.length > 0 ? args[0] : "localhost");
    }
    
    private void start(String serverName){
        int port = 10101;
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);

            Socket client = new Socket(serverName, port);
            System.out.println("Connection established...");
            
            
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(client.getInputStream());

            System.out.println("Create member ...");
            
            UUID id = UUID.randomUUID();
            
            
            Member member = new Member(id, new Transformer());
            
            final OutboundChannel channel = member.getOutbound();
                       
            out.writeObject(id);
            out.flush();
                        
            (new Thread(new ChannelConnector(channel, in, out))).start();
            
            final List<Pointer> addresses = Collections.synchronizedList(new ArrayList<Pointer>(1));
                        
            addresses.add(null);
            
            member.onCommand(new CommandHandler() {                
                @Override
                public void handleCommand(Pointer pointer, Command command) {
                    //System.out.println(String.format("The command %s was applied to object %s.", command, pointer));
                    
                    if(command instanceof Construct){
                        addresses.add(pointer);
                        
                        if(((Construct) command).getType() == Type.SEQUENCE)
                            System.out.println("New sequence linked to local address: " + addresses.indexOf(pointer));
                        else 
                            System.out.println("New hash linked to local address: " + addresses.indexOf(pointer));
                    } else {
                        System.out.println(String.format("Applied command '%s' to object at '%s'.", command, addresses.indexOf(pointer)));
                    }
                                        
                }
            });
            

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            
            while((line = reader.readLine()) != null){
                StringTokenizer tokenizer = new StringTokenizer(line, " ");
                
                String command = tokenizer.nextToken();
                
                
                if("exit".equals(command)){
                    break;
                } else if ("set".equals(command) || "ins".equals(command)){
                    int address         = Integer.parseInt(tokenizer.nextToken());
                    String identifier   = tokenizer.nextToken();
                    
                    String raw          = tokenizer.nextToken();
                    
                    Pointer pointer     = addresses.get(address);
                    Data data;
                    
                    if(raw.startsWith("*")){
                        data = addresses.get(Integer.parseInt(raw.substring(1)));
                    } else if(isNumeric(raw)){
                        data = new Constant(new Double(Double.parseDouble(raw)));
                    } else {
                        data = new Constant(raw);
                    }
                    
                    try {
                        if("set".equals(command)){
                            member.apply(pointer, new Set(identifier, data));
                        } else {
                            member.apply(pointer, new Insert(Integer.parseInt(identifier), data));
                        }
                    } catch (MemberException e) {
                        e.printStackTrace();
                        break;
                    }
                } else if ("new".equals(command)){                    
                    String type         = tokenizer.nextToken();
                                        
                    Pointer pointer = Pointer.uniquePointer();
                    addresses.add(pointer);                    
                    
                    try {
                        if("seq".equals(type)){
                            member.apply(pointer, new Construct(Type.SEQUENCE));
                            System.out.println("Created sequence at local address: " + addresses.indexOf(pointer));
                        } else {
                            member.apply(pointer, new Construct(Type.HASH));
                            System.out.println("Created hash at local address: " + addresses.indexOf(pointer));
                        }
                    } catch (MemberException e) {
                        e.printStackTrace();
                        break;
                    }
                } else if("out".equals(command)){
                    int address         = Integer.parseInt(tokenizer.nextToken());
                    
                    String identifier   = tokenizer.nextToken();
                    
                    Pointer pointer     = addresses.get(address);
                    
                    try {
                        Data d;
                        if (isNumeric(identifier)) {
                            d = member.find(pointer, Integer.parseInt(identifier));
                        } else {
                            d = member.find(pointer, identifier);
                        }
                        
                        System.out.println(d instanceof Pointer ? addresses.indexOf(d): d);
                    } catch (NumberFormatException | ObjectException
                            | CommandException e) {
                        e.printStackTrace();
                    }        
                    
                }
            }
                        
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
