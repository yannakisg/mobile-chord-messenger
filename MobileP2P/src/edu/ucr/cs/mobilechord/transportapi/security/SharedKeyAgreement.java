package edu.ucr.cs.mobilechord.transportapi.security;


import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apachemobile.commons.codec.binary.Base64;

import edu.ucr.cs.mobilechord.transportapi.client.ConnInfo;
import edu.ucr.cs.mobilechord.transportapi.client.HttpClient;
import edu.ucr.cs.mobilechord.transportapi.dhash.BigInt;
import edu.ucr.cs.mobilechord.transportapi.messages.DiffieHellmanMessage;


// Step 1: Generate a key pair (first of them)
// Step 2: Send the public key and the diffie-hellman key (first of them)
// Step 3: The other generates a key pair based on the received things
// Step 4a: The first performs the first phase of the protocol with her private key
// Step 4b: The first performs the second phase of the protocol with other's public key
// Step 4c: The first can generate the secret key
// Step 5a: The other uses his private key to perform the first phase
// Step 5b: The other uses the first's public key to perform the second phase
// Step 5c: The other can generate the secret key
// Step 6: The first/other can generate an AES key
// Step 7: The first can encrypt data with shared key
// Step 8: The other can decrypt data with private key
public class SharedKeyAgreement implements Runnable{
    private final HttpClient httpClient;
    private final boolean isInitiator;
    private SecretKeySpec sharedKey;
    private final String dstIP;
    private final int dstPort;
    private final String protocol;
    private final BlockingQueue<DiffieHellmanMessage> blockingQueue;
    private KeyPair keyPair;
    private boolean success;
    private final BigInt myID;
    private final String myIP;
    private final Integer myPort;
    private byte[] secret;
    
    public SharedKeyAgreement(BigInt myID, String myIP, Integer myPort, HttpClient httpClient, String dstIP, int dstPort, String protocol, boolean isInitiator) throws NoSuchAlgorithmException {
        this.httpClient = httpClient;
        this.isInitiator = isInitiator;
        this.blockingQueue = new LinkedBlockingDeque<DiffieHellmanMessage>();
        this.dstIP = dstIP;
        this.dstPort = dstPort;
        this.protocol = protocol;
        this.myIP = myIP;
        this.myPort = myPort;
        this.myID = myID;
        generateKeys();
    }
    
    private void generateKeys() throws NoSuchAlgorithmException {
        // Step 1
        System.out.println("Generating Keys for that session...");
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
        keyPairGen.initialize(1024);
        keyPair = keyPairGen.generateKeyPair();
    }
    
    
    @Override
    public void run() {
        if (performKeyAgreement()) {
            this.success = true;
        } else {
            this.success = false;
        }
    }
    
    public boolean success() {
        return this.success;
    }
    
    public boolean performKeyAgreement() {
        try {
            if (isInitiator) {
                
                // Step 2
                Class dhClass = Class.forName("javax.crypto.spec.DHParameterSpec");
                DHParameterSpec dhSpec = ((DHPublicKey)keyPair.getPublic()).getParams();
                
                // Step 4a
                KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");                
                keyAgreement.init(keyPair.getPrivate());
                
                System.out.println("Sending my public parameters");
                sendPublicParameters(keyPair.getPublic().getEncoded(), dhSpec);  
                System.out.println("Waiting for the public key");
                DiffieHellmanMessage reply = blockingQueue.take();
                System.out.println("Public key was received");
                 
                // Step 4b
                KeyFactory keyFactory = KeyFactory.getInstance("DH");
                X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(reply.getEncodedPublicKey());
                PublicKey publicKey = keyFactory.generatePublic(x509Spec);
                keyAgreement.doPhase(publicKey, true);
                
                // Step 4c
                secret = keyAgreement.generateSecret();
                
                // Step 6
                sharedKey = new SecretKeySpec(SecurityUtils.md5Bytes(secret), "AES");
                
                System.out.println("Shared Key: " + Base64.encodeBase64String(sharedKey.getEncoded()));
            } else {
                // Step 3
                System.out.println("Waiting for the public key");
                DiffieHellmanMessage reply = blockingQueue.take();
                System.out.println("Public key was received");
                Class dhClass = Class.forName("javax.crypto.spec.DHParameterSpec");
                DHParameterSpec dhSpec = new DHParameterSpec(reply.getP(), reply.getG(), reply.getL());
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
                kpg.initialize(dhSpec);
                
                KeyPair kp = kpg.generateKeyPair();
                System.out.println("Sending my public parameters");
                sendPublicParameters(kp.getPublic().getEncoded(), dhSpec);
                
                // Step 5a
                KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
                keyAgreement.init(kp.getPrivate());
                
                // Step 5b
                KeyFactory keyFactory = KeyFactory.getInstance("DH");
                X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(reply.getEncodedPublicKey());
                PublicKey publicKey = keyFactory.generatePublic(x509Spec);
                keyAgreement.doPhase(publicKey, true);
                
                // Step 5c
                secret = keyAgreement.generateSecret();
                
                // Step 6
                sharedKey = new SecretKeySpec(SecurityUtils.md5Bytes(secret), "AES");
                
                System.out.println("Shared Key: " + Base64.encodeBase64String(sharedKey.getEncoded()));
            }
            
            return true;
        } catch (Exception ex) {
            System.err.println(ex);
            return false;
        }
    }
    
    public void insert(DiffieHellmanMessage msg) {
        this.blockingQueue.offer(msg);
    }
    
    private void sendPublicParameters(byte[] encodingKey, DHParameterSpec dhSpec) {
        DiffieHellmanMessage msg = new DiffieHellmanMessage(myIP, myPort, myID, encodingKey, dhSpec.getP(), dhSpec.getG(), dhSpec.getL());
        ConnInfo connInfo = new ConnInfo(dstIP, dstPort, protocol, msg);
        httpClient.insert(connInfo);
    }
    
    public SecretKeySpec getSharedKey() {
        return this.sharedKey;
    }
    
    public byte[] getSecretKey() {
        return this.secret;
    }
}
