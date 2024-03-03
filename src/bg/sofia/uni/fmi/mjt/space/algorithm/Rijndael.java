package bg.sofia.uni.fmi.mjt.space.algorithm;

import bg.sofia.uni.fmi.mjt.space.exception.CipherException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Rijndael implements SymmetricBlockCipher {
    private static final String CIPHER_EXCEPTION_MESSAGE = "encrypt operation cannot be completed successfully";
    private static final int KILOBYTE = 1024;
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private final SecretKey secretKey;

    public Rijndael(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public void encrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        try {
            encryptData(inputStream, outputStream);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new CipherException(CIPHER_EXCEPTION_MESSAGE, e);
        }
    }

    private void encryptData(InputStream inputStream, OutputStream outputStream)
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        manipulateData(inputStream, outputStream, Cipher.ENCRYPT_MODE);
    }

    @Override
    public void decrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        try {
            decryptData(inputStream, outputStream);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new CipherException(CIPHER_EXCEPTION_MESSAGE, e);
        }
    }

    private void decryptData(InputStream inputStream, OutputStream outputStream)
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        manipulateData(inputStream, outputStream, Cipher.DECRYPT_MODE);
    }

    private void manipulateData(InputStream inputStream, OutputStream outputStream, int mode)
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(mode, secretKey);

        try (var outputStreamCipher = new CipherOutputStream(outputStream, cipher)) {
            byte[] buffer = new byte[KILOBYTE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStreamCipher.write(buffer, 0, bytesRead);
            }
        }
    }
}
