import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private final ArrayList<ConnectionHandler> connections;
    private ServerSocket server;//NONCOMPLIANT(same name with the class)
    private boolean done;
    


    

    @Override
    public void run() {
        try {
            ExecutorService pool;
            server = new ServerSocket(3030);
            pool = Executors.newCachedThreadPool();
            while(!done){
            Socket client = server.accept();
            ConnectionHandler handler = new ConnectionHandler(client);
            connections.add(handler);
            pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
        
    }

    public void broadcast(String message){
        for(ConnectionHandler ch : connections){
            if (ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown(){
        try {
            done =  true;
            if(!server.isClosed()){
                server.close();
            }
            for (ConnectionHandler ch : connections){
                ch.shutdown();
            }
        } catch (IOException e) {
            //ignore
        }
        
        

    }

    class ConnectionHandler implements Runnable{

        final private Socket client;
        
        private BufferedReader in;
        private PrintWriter out;
        


        public ConnectionHandler(Socket client){
            this.client=client;

            
        }


        @Override
        public void run() {
            
            try {
                
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String nickname;
                out.println("Please enter a nickname:");
                nickname = in.readLine();
                System.out.println(nickname + " connected");//NONCOMPLIANT(use Logger)
                broadcast("WELCOME TO BASHFUL");
                broadcast(nickname + " joined the chat!");

                //Chatter level commands

                String message;
                while ((message = in.readLine()) != null) {
                    String[] messageSplit = message.split(" ", 2);//message splitter: TO DISTINGUISH COMMANDS FROM MESSAGES with slashes"/"
                    if (message.startsWith("/nick")){

                        if (messageSplit.length == 2  & messageSplit[1] != null& messageSplit[1].equals("")){
                            System.out.println(nickname + "'s nickname has been changed into " + messageSplit[1]);//NONCOMPLIANT(use Logger)
                            broadcast(nickname + "'s nickname has been changed into " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname to " + nickname);

                        } else {
                            out.println("no nickname provided");
                        }

                    } else broadcast(nickname+ ": " + message);


                }
            } catch (IOException e) {
                shutdown();
            }

            

        }
        public void sendMessage(String message){
            out.println(message);
        }
        public void shutdown(){
            out.println("The Server have gone closed");
            try {
            in.close();
            out.close();
            if (!client.isClosed()){
                client.close();
            }
            } catch (IOException e) {
            
            }
            
            
        }

    }
        public Server(){
        done = false;
        connections = new ArrayList<>();
        
    }


    public static void main(String[] args) {
        System.out.println("BASHFUL SERVER HAS BEEN ONLINE");//NONCOMPLIANT(use Logger)
        Server server = new Server();
        
        server.run();
        
    }
}

