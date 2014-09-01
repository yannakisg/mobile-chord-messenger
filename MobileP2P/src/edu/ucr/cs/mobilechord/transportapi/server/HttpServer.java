package edu.ucr.cs.mobilechord.transportapi.server;




import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyStore;
import java.util.Locale;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apachemobile.http.HttpException;
import org.apachemobile.http.HttpRequest;
import org.apachemobile.http.HttpResponse;
import org.apachemobile.http.MethodNotSupportedException;
import org.apachemobile.http.config.ConnectionConfig;
import org.apachemobile.http.impl.nio.DefaultHttpServerIODispatch;
import org.apachemobile.http.impl.nio.DefaultNHttpServerConnection;
import org.apachemobile.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apachemobile.http.impl.nio.SSLNHttpServerConnectionFactory;
import org.apachemobile.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apachemobile.http.impl.nio.reactor.IOReactorConfig;
import org.apachemobile.http.nio.NHttpConnectionFactory;
import org.apachemobile.http.nio.NHttpServerConnection;
import org.apachemobile.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apachemobile.http.nio.protocol.BasicAsyncResponseProducer;
import org.apachemobile.http.nio.protocol.HttpAsyncExchange;
import org.apachemobile.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apachemobile.http.nio.protocol.HttpAsyncRequestHandler;
import org.apachemobile.http.nio.protocol.HttpAsyncService;
import org.apachemobile.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
import org.apachemobile.http.nio.reactor.IOEventDispatch;
import org.apachemobile.http.nio.reactor.IOReactorException;
import org.apachemobile.http.nio.reactor.ListeningIOReactor;
import org.apachemobile.http.protocol.HttpContext;
import org.apachemobile.http.protocol.HttpCoreContext;
import org.apachemobile.http.protocol.HttpProcessor;
import org.apachemobile.http.protocol.HttpProcessorBuilder;
import org.apachemobile.http.protocol.ResponseConnControl;
import org.apachemobile.http.protocol.ResponseContent;
import org.apachemobile.http.protocol.ResponseDate;
import org.apachemobile.http.protocol.ResponseServer;



import android.util.Base64;


import edu.ucr.cs.mobilechord.transportapi.client.HttpClient;
import edu.ucr.cs.mobilechord.transportapi.messages.DiffieHellmanMessage;
import edu.ucr.cs.mobilechord.transportapi.messages.EncryptedMessage;
import edu.ucr.cs.mobilechord.transportapi.messages.ErrorMessage;
import edu.ucr.cs.mobilechord.transportapi.messages.LoginMessage;
import edu.ucr.cs.mobilechord.transportapi.messages.Message;
import edu.ucr.cs.mobilechord.transportapi.messages.MessageTypes;
import edu.ucr.cs.mobilechord.transportapi.messages.RegisterMessage;
import edu.ucr.cs.mobilechord.transportapi.messages.SuccessMessage;
import edu.ucr.cs.mobilechord.transportapi.messages.SuccessorPredecessorInfo;
import edu.ucr.cs.mobilechord.transportapi.messages.TextMessage;
import edu.ucr.cs.mobilechord.transportapi.messages.chord.ChordMessage;
import edu.ucr.cs.mobilechord.transportapi.network_component.NetworkComponent;

public class HttpServer implements Runnable {
    private final int port;
    private final HttpClient httpClient;
    private final boolean isCentralServer;
    private final NetworkComponent component;
    
    public HttpServer(NetworkComponent component, int port ,HttpClient httpClient, boolean isCentralServer) {
        this.port = port;
        this.httpClient = httpClient;
        this.isCentralServer = isCentralServer;
        this.component = component;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public boolean IsCentralServer() {
        return this.isCentralServer;
    }

    @Override
    public void run() {
        
        
        // HTTP Processing chain
        HttpProcessor httpProc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer())
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();
        
        // Request Handler Registry
        UriHttpAsyncRequestHandlerMapper registry = new UriHttpAsyncRequestHandlerMapper();
        
        // Register the default handler for all URIs
        registry.register("*", new HttpMessageHandler(component, port, httpClient, isCentralServer));
        
        // Server-Side HTTP Handler
        HttpAsyncService protocolHandler = new HttpAsyncService(httpProc, registry) {
            
            @Override
            public void connected(final NHttpServerConnection conn) {
                System.out.println(conn + ": connection established");
                super.connected(conn);
            }
            
            @Override
            public void closed(final NHttpServerConnection conn) {
                System.out.println(conn + ": connection closed");
                super.closed(conn);
            }
        };
        
        NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory = null;
        // SSL
        if (port == 8443) {
            // Initialize SSL context
            //ClassLoader cl = server.HttpServer.class.getClassLoader();
            
            try {
                URL url = new File("my.keystore").toURI().toURL();//cl.getResource("my.keystore");
                if (url == null) {
                    System.err.println("Keystore not found");
                    System.exit(1);
                }
                KeyStore keyStore = KeyStore.getInstance("jks");
                keyStore.load(url.openStream(), "ucrucr".toCharArray());
                KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmFactory.init(keyStore, "ucrucr".toCharArray());
                KeyManager[] keyManagers = kmFactory.getKeyManagers();
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(keyManagers, null, null);
                connFactory = new SSLNHttpServerConnectionFactory(sslContext, null, ConnectionConfig.DEFAULT);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
                System.exit(1);
            } 
        } else {
            connFactory = new DefaultNHttpServerConnectionFactory(ConnectionConfig.DEFAULT);
        }
        
        // Server-side I/O event dispatch
        IOEventDispatch ioEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, connFactory);
        
        // I/O reactor defaults
        IOReactorConfig config = IOReactorConfig.custom()
                .setIoThreadCount(5)
                .setSoTimeout(3000)
                .setConnectTimeout(3000)
                .build();
        try {
            // Create Server-side I/O reactor
            ListeningIOReactor ioReactor = new DefaultListeningIOReactor(config);
            
            ioReactor.listen(new InetSocketAddress(port));
            ioReactor.execute(ioEventDispatch);
        } catch (IOReactorException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        } catch (InterruptedIOException ex) {
            System.err.println("Interrupted");
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
        }
        
        System.out.println("Shutdown");
    }
   
    
  static class HttpMessageHandler implements HttpAsyncRequestHandler<HttpRequest> {
        private final HttpClient httpClient;
        private final int port;
        private final String protocol;
        private final boolean isCentralServer;
        private final NetworkComponent component;
        
        public HttpMessageHandler(NetworkComponent component, int port, HttpClient httpClient, boolean isCentralServer) {
            this.httpClient = httpClient;
            this.port = port;
            this.isCentralServer = isCentralServer;
            this.component = component;
            if (port == 8443) {
                protocol = "https";
            } else {
                protocol = "http";
            }
        }
      
        @Override
        public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request, HttpContext context) throws HttpException, IOException {
            // Buffer request content in memory
            return new BasicAsyncRequestConsumer();
        }

        @Override
        public void handle(HttpRequest request, HttpAsyncExchange httpExchange, HttpContext context) throws HttpException, IOException {
            
            HttpResponse response = httpExchange.getResponse();
            handleInternal(request, response, context);
            httpExchange.submitResponse(new BasicAsyncResponseProducer(response));
        }
        
        private void handleInternal(final HttpRequest request, final HttpResponse response, final HttpContext context) throws MethodNotSupportedException {
            HttpCoreContext coreContext = HttpCoreContext.adapt(context);
            
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            
            /*NHttpConnection conn = coreContext.getConnection(NHttpConnection.class);
            HttpInetConnection connection = (HttpInetConnection) conn;
            InetAddress remoteAddr = connection.getRemoteAddress();
            String rcvdIP = remoteAddr.toString().substring(1);*/
            String rcvd = request.getRequestLine().getUri();
            System.out.println("Received (Base64 Representation): " + rcvd);      
            
            byte[] bRcvd = Base64.decode(rcvd, Base64.DEFAULT);
            byte messageType = bRcvd[0];
            Message msg;
            
            switch (messageType) {
                case MessageTypes.LOGIN_MESSAGE_TYPE:
                    System.out.println("Received: LOGIN MESSAGE");
                    if (!isCentralServer) {
                        System.err.println("I am not the central server. Throw it");
                        break;
                    }
                    msg = LoginMessage.parseArray(bRcvd);        
                    component.handleLogin((LoginMessage)msg);
                    break;
                case MessageTypes.REGISTER_MESSAGE_TYPE:    
                    System.out.println("Received: REGISTER MESSAGE");
                    if (!isCentralServer) {
                        System.err.println("I am not the central server. Throw it");
                        break;
                    }
                    msg = RegisterMessage.parseArray(bRcvd);
                    component.handleRegister((RegisterMessage)msg);
                    break;
                case MessageTypes.SUCCESSOR_PREDECESSOR_INFO:
                    System.out.println("Received: SUCCESSOR_PREDECESSOR INFO MESSAGE");
                    if (isCentralServer) {
                        System.err.println("I am the central server. Throw it");
                        break;
                    }
                    msg = SuccessorPredecessorInfo.parseArray(bRcvd);
                    component.handleSuccessorPredecessorInfo((SuccessorPredecessorInfo) msg);
                    break;
                case MessageTypes.DIFFIE_HELLMAN_MESSAGE:
                    System.out.println("Received: DIFFIE HELLMAN MESSAGE");
                    if (isCentralServer) {
                        System.err.println("I am the central server. Throw it");
                        break;
                    }
                    msg = DiffieHellmanMessage.parseArray(bRcvd);
                    component.handleDiffieHellman((DiffieHellmanMessage) msg);
                    break;
                case MessageTypes.ERROR_MESSAGE:
                    System.out.println("Received: ERROR MESSAGE");
                    msg = ErrorMessage.parseArray(bRcvd);
                    component.handleError((ErrorMessage) msg);
                    break;
                case MessageTypes.SUCCESS_MESSAGE:
                    System.out.println("Received: SUCCESS MESSAGE");
                    msg = SuccessMessage.parseArray(bRcvd);
                    component.handleSuccess((SuccessMessage) msg);
                    break;
                case MessageTypes.TEXT_MESSAGE:
                    System.out.println("Received: TEXT MESSAGE");
                    if (isCentralServer) {
                        System.err.println("I am the central server. Throw it");
                        break;
                    }
                    msg = TextMessage.parseArray(bRcvd);
                    component.handleTextMessage((TextMessage) msg);
                    break;
                case MessageTypes.CHORD_MESSAGE:
                    System.out.println("Received: CHORD MESSAGE");
                    if (isCentralServer) {
                        System.err.println("I am the central server. Throw it");
                        break;
                    }
                    msg = ChordMessage.parseArray(bRcvd);
                    System.out.println("HandleChordMessage");
                    component.handleChordMessage((ChordMessage) msg);
                    break;
                case MessageTypes.ENCRYPTED_MESSAGE:
                    System.out.println("Received: ENCRYPTED MESSAGE");
                    if (isCentralServer) {
                        System.err.println("I am the central server. Throw it");
                        break;
                    }
                    msg = EncryptedMessage.parseArray(bRcvd);
                    component.handleEncryptedMessage((EncryptedMessage) msg);
                    break;
                default:
                    System.err.println("Unknown Message: " + messageType);
            }
        }
      
  }
    
}
