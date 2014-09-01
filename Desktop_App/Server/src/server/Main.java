package server;

import org.apache.http.nio.reactor.IOReactorException;

public class Main {
    
    private static void usage() {
        System.err.println("Usage: java server.Main <port>");
        System.exit(1);
    }
    
    public static void main(String[] args) throws InterruptedException, IOReactorException {
        int port = 8080;
        
        if (args != null && args.length != 0) {
            if (args.length != 1) {
                usage();
            } else {
                port = Integer.parseInt(args[0]);
            }
        } 
        ServerComponent serverComponent = new ServerComponent(port, true);
        serverComponent.setIP("192.168.1.2");
        Thread t = new Thread(serverComponent);
        t.start();
        t.join();
    }
}
