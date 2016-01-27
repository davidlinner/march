package org.march.server;

import java.util.HashMap;
import java.util.UUID;

import org.march.data.CommandException;
import org.march.data.ObjectException;
import org.march.server.message.CloseMessage;
import org.march.server.message.DataMessage;
import org.march.server.message.Message;
import org.march.server.message.OpenMessage;
import org.march.sync.Leader;
import org.march.sync.LeaderException;
import org.march.sync.endpoint.Bucket;
import org.march.sync.endpoint.BucketHandler;
import org.march.sync.endpoint.OutboundEndpoint;
import org.march.sync.transform.Transformer;

//TODO: transformer per schema, multiple connectors per schema

public class Server {
	
	private DatastoreConnector dataConnector;

	private MessageHandler messageHandler;
	
	private Transformer transformer;
	
	private HashMap<String, Leader> leaders = new HashMap<String, Leader>();
	
	public Server(){	
	}
	
	public Server(DatastoreConnector dataConnector) {
		this.dataConnector = dataConnector;
	}
	
	public DatastoreConnector getDataConnector() {
		return dataConnector;
	}

	public void setDataConnector(DatastoreConnector dataConnector) {
		this.dataConnector = dataConnector;
	}	
	
	public Transformer getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	public void onMessage(MessageHandler messageHandler){
		this.messageHandler = messageHandler;
	}
	
	public void receive(Message message) throws ServerException{
		if(message instanceof DataMessage){
			DataMessage dataMessage = (DataMessage)message;
			process(dataMessage.getScope(), dataMessage.getBucket());
		} else if(message instanceof OpenMessage){
			OpenMessage openMessage = (OpenMessage)message;
			open(openMessage.getScope(), openMessage.getMember());
		} else if(message instanceof CloseMessage){
			CloseMessage closeMessage = (CloseMessage)message;
			close(closeMessage.getScope(), closeMessage.getMember());
		} else {
			//TODO: throw unknown message exception
		}
	}	
	
	private void open(String scope, UUID member) throws ServerException {
		
		Leader leader = null;
		synchronized(leaders){
			leader = leaders.get(scope);
			if(leader == null){			
				
				leader = new Leader(this.transformer);
				
				try {
					//FIXME: this might be an expensive operation, a loader pool and a future-based solution could be a better solution here
					leader.setData(this.dataConnector.read(scope));
				} catch (ObjectException | CommandException | LeaderException e) {
					throw new ServerException(String.format("Cannot initiate scope '%s'.", scope), e);
				}
				
				leaders.put(scope, leader);
			}
		}
		
		try {
			leader.subscribe(member);
		} catch (LeaderException e) {
			throw new ServerException(e);
		}
		
		OutboundEndpoint endpoint = leader.getOutbound(member);
		
		endpoint.connectOutbound(new BucketHandler() {
			
			@Override
			public void handle(Bucket bucket) {
				//Server.this.messageHandler.send
				
			}
		});
		
		
		
	}

	private void close(String scope, UUID member){
		
	}

	private void process(String scope, Bucket bucket){
		
	}
	
	/*
	 * create(member, scope):session
	 * session.open((bucket) -> {})
	 * session.update(bucket)
	 * session.close()
	 *  
	 * 
	 * 
	 * */
}
