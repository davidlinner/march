package org.march.server;

//TODO: transformer per schema, multiple connectors per schema

public class Server {
	
//	private DatastoreConnector dataConnector;
//
//	private MessageHandler messageHandler;
//
//	private Transformer transformer;
//
//	private HashMap<String, Master> leaders = new HashMap<String, Master>();
//
//	public Server(){
//	}
//
//	public Server(DatastoreConnector dataConnector) {
//		this.dataConnector = dataConnector;
//	}
//
//	public DatastoreConnector getDataConnector() {
//		return dataConnector;
//	}
//
//	public void setDataConnector(DatastoreConnector dataConnector) {
//		this.dataConnector = dataConnector;
//	}
//
//	public Transformer getTransformer() {
//		return transformer;
//	}
//
//	public void setTransformer(Transformer transformer) {
//		this.transformer = transformer;
//	}
//
//	public void onMessage(MessageHandler messageHandler){
//		this.messageHandler = messageHandler;
//	}
//
//	public void update(Message message) throws ServerException{
//		if(message instanceof DataMessage){
//			DataMessage dataMessage = (DataMessage)message;
//			send(dataMessage.getScope(), dataMessage.getChangeSet());
//		} else if(message instanceof OpenMessage){
//			OpenMessage openMessage = (OpenMessage)message;
//			activate(openMessage.getScope(), openMessage.getReplicaName());
//		} else if(message instanceof CloseMessage){
//			CloseMessage closeMessage = (CloseMessage)message;
//			deactivate(closeMessage.getScope(), closeMessage.getReplicaName());
//		} else {
//			//TODO: throw unknown message exception
//		}
//	}
//
//	private void activate(String scope, UUID member) throws ServerException {
//
//		Master leader = null;
//		synchronized(leaders){
//			leader = leaders.get(scope);
//			if(leader == null){
//
//				leader = new Master(this.transformer);
//
//				try {
//					//FIXME: this might be an expensive operation, a loader pool and a future-based solution could be a better solution here
//					leader.setData(this.dataConnector.read(scope));
//				} catch (ObjectException | CommandException | MasterException e) {
//					throw new ServerException(String.format("Cannot initiate scope '%s'.", scope), e);
//				}
//
//				leaders.put(scope, leader);
//			}
//		}
//
//		try {
//			leader.register(member);
//		} catch (MasterException e) {
//			throw new ServerException(e);
//		}
//
//		OutboundEndpoint endpoint = leader.getOutbound(member);
//
//		endpoint.connectOutbound(new ChannelListener() {
//
//			@Override
//			public void send(ChangeSet bucket) {
//				//Server.this.messageHandler.append
//
//			}
//		});
//
//
//
//	}
//
//	private void deactivate(String scope, UUID member){
//
//	}
//
//	private void send(String scope, ChangeSet bucket){
//
//	}
	
	/*
	 * create(member, scope):session
	 * session.activate((bucket) -> {})
	 * session.update(bucket)
	 * session.deactivate()
	 *  
	 * 
	 * 
	 * */
}
