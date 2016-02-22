package org.march.demo;

public class MarchServer {

//    private ServerSocket serverSocket;
//
//    private Master leader;
//
//    public MarchServer(int port) throws IOException {
//        serverSocket = new ServerSocket(port);
//        serverSocket.setSoTimeout(120000);
//
//        leader = new Master(new Transformer());
//    }
//
//    public void run() {
//        System.out.println("Waiting for clients on port " + serverSocket.getLocalPort() + "...");
//
//        while (true) {
//            try {
//                Socket socket = serverSocket.receive();
//                System.out.println("Client connected ...");
//
//                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//
//
//                UUID id = (UUID)in.readObject();
//                System.out.println("I am " + id);
//
//                leader.register(id);
//
////                OutboundEndpoint endpoint = leader.getOutbound(id);
////
////                (new Thread(new ChannelConnector(endpoint, in, out))).start();
////
////                System.out.println("Accepted connection for: " + id);
//
//            } catch (SocketTimeoutException s) {
//                System.out.println("Socket timed out!");
//                break;
//            } catch (IOException e) {
//                e.printStackTrace();
//                break;
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//                break;
//            } catch (MasterException e) {
//				e.printStackTrace();
//				break;
//			}
//        }
//    }
//
//    public static void main(String[] args) {
//        try {
//            Thread t = new Thread(new MarchServer(10101));
//            t.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
