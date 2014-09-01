package security;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class SecurityUtils {
    
    private final static String TRANSORMATION = "AES/ECB/PKCS5Padding";
    private final static String ENCR_ALGO = "AES";
    private final static String SHA_1 = "SHA-1";
    private final static String SHA2_256 = "SHA-256";
    private final static String MD5 = "MD5";
    
    public static byte[] encrypt(String key, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        return encrypt(key.getBytes(), data);
    }
    
    public static byte[] encrypt(byte[] key, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        final SecretKeySpec secretKey = new SecretKeySpec(md5Bytes(key), ENCR_ALGO);
        return encrypt(secretKey, data);
    }
    
    public static byte[] encrypt(SecretKeySpec secretKey, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(TRANSORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
    
    public static byte[] decrypt(String key, byte[] encryptedData) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        final SecretKeySpec secretKey = new SecretKeySpec(md5Bytes(key), ENCR_ALGO);
        return decrypt(secretKey, encryptedData);
    }
    
    public static byte[] decrypt(SecretKeySpec secretKey, byte[] encryptedData) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(TRANSORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedData);
    }
    
    public static byte[] sha1Bytes(String data) throws NoSuchAlgorithmException {
        return sha1Bytes(data.getBytes());
    }
    
    public static byte[] sha1Bytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SHA_1);
        md.update(data);
        return md.digest();
    }
    
    public static byte[] md5Bytes(String data) throws NoSuchAlgorithmException {
        return md5Bytes(data.getBytes());
    }
    
    public static byte[] md5Bytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(MD5);
        md.update(data);
        return md.digest();
    }
    
    public static byte[] sha256Bytes(String data) throws NoSuchAlgorithmException  {
        return sha256Bytes(data.getBytes());
    }
    
    public static byte[] sha256Bytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SHA2_256);
        md.update(data);
        return md.digest();
    }
    
    public static String sha256HexString(String data) throws NoSuchAlgorithmException {
        return sha256HexString(data.getBytes());
    }
    
    public static String sha256HexString(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SHA2_256);
        md.update(data);
        
        byte[] hBytes = md.digest();
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hBytes.length; i++) {
            sb.append(Integer.toString((hBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        
        return sb.toString();        
    }
}
