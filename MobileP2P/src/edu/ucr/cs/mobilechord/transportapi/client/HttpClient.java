package edu.ucr.cs.mobilechord.transportapi.client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apachemobile.http.HttpHost;
import org.apachemobile.http.HttpResponse;
import org.apachemobile.http.client.protocol.RequestExpectContinue;
import org.apachemobile.http.concurrent.FutureCallback;
import org.apachemobile.http.config.ConnectionConfig;
import org.apachemobile.http.impl.nio.DefaultHttpClientIODispatch;
import org.apachemobile.http.impl.nio.pool.BasicNIOConnPool;
import org.apachemobile.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apachemobile.http.message.BasicHttpRequest;
import org.apachemobile.http.nio.protocol.BasicAsyncRequestProducer;
import org.apachemobile.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apachemobile.http.nio.protocol.HttpAsyncRequestExecutor;
import org.apachemobile.http.nio.protocol.HttpAsyncRequester;
import org.apachemobile.http.nio.reactor.ConnectingIOReactor;
import org.apachemobile.http.nio.reactor.IOEventDispatch;
import org.apachemobile.http.nio.reactor.IOReactorException;
import org.apachemobile.http.protocol.HttpCoreContext;
import org.apachemobile.http.protocol.HttpProcessor;
import org.apachemobile.http.protocol.HttpProcessorBuilder;
import org.apachemobile.http.protocol.RequestConnControl;
import org.apachemobile.http.protocol.RequestContent;
import org.apachemobile.http.protocol.RequestTargetHost;
import org.apachemobile.http.protocol.RequestUserAgent;

import android.util.Base64;

public class HttpClient implements Runnable {
    
    private final BlockingQueue<ConnInfo> blockingQueue;
    private HttpProcessor httpProc;
    private BasicNIOConnPool pool;
    
    public HttpClient() throws IOReactorException {
        this.blockingQueue = new LinkedBlockingQueue<ConnInfo>();
        
        createHttpClient();
    }
    
    public void insert(ConnInfo connInfo) {
        if (!this.blockingQueue.offer(connInfo)) {
            System.err.println("Something happened");
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                ConnInfo connInfo = blockingQueue.take();
                execute(connInfo);
            } catch (InterruptedException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
    
    private void execute(ConnInfo connInfo) {
        HttpAsyncRequester requester = new HttpAsyncRequester(httpProc);
        final HttpHost httpHost = new HttpHost(connInfo.getIP(), connInfo.getPort(), connInfo.getProtocol());
        
        String base64Str = Base64.encodeToString(connInfo.getMessage().toByteArray(), Base64.DEFAULT);
        System.out.println("Sending to " + httpHost + " -> " + base64Str);
        BasicHttpRequest request = new BasicHttpRequest("POST", base64Str);
        HttpCoreContext coreContext = HttpCoreContext.create();
        
        System.out.println("Total Bytes: " + request.toString().length() + " \n\tText: " + request.toString());
        FutureCallbackClass futureCallback = new FutureCallbackClass(httpHost);
        
        requester.execute(new BasicAsyncRequestProducer(httpHost, request), new BasicAsyncResponseConsumer(), pool, coreContext, 
                futureCallback);
        
        System.out.println("FutureCallBack: " + futureCallback.getResult());
        
    }
    
    
    private void createHttpClient() throws IOReactorException {
        // HTTP processing chain
        httpProc = HttpProcessorBuilder.create()
                .add(new RequestContent())
                .add(new RequestTargetHost())
                .add(new RequestConnControl())
                .add(new RequestUserAgent())
                .add(new RequestExpectContinue()).build();
        
        // HTTP protocol handler
        HttpAsyncRequestExecutor protocolHandler = new HttpAsyncRequestExecutor();
        
        // client-side I/O event dispatch
        final IOEventDispatch ioEventDispatch = new DefaultHttpClientIODispatch(protocolHandler, ConnectionConfig.DEFAULT);
        
        // client-side I/O reactor
        final ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        
        // HTTP connection pool
        pool = new BasicNIOConnPool(ioReactor, ConnectionConfig.DEFAULT);
        pool.setDefaultMaxPerRoute(1);
        pool.setMaxTotal(5);
        
        Thread t = new Thread (new Runnable() {

            @Override
            public void run() {
                try {
                    ioReactor.execute(ioEventDispatch);
                } catch (InterruptedIOException ex) {
                    System.err.println(ex.getMessage());
                } catch (IOException ex) {
                     System.err.println(ex.getMessage());
                }
                
                System.out.println("Shutdown...");
            }
        });
        t.start();        
    }
    
    private class  FutureCallbackClass implements FutureCallback<HttpResponse> {
        
        private boolean result;
        private final HttpHost httpHost;
        
        protected FutureCallbackClass(HttpHost httpHost) {
            this.result = true;
            this.httpHost = httpHost;
        }
        
        protected boolean getResult() {
            return result;
        }
        
        @Override
        public void completed(HttpResponse response) {
            System.out.println(httpHost + " -> " + response.getStatusLine());
        }

        @Override
        public void failed(Exception ex) {
            System.out.println(httpHost + " -> " + ex);
            this.result = false;
        }

        @Override
        public void cancelled() {
             System.out.println(httpHost + " cancelled");
        }
        
    }
}
