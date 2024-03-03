package bg.sofia.uni.fmi.mjt.space.algorithm;

import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RijndaelTest {
    @Test
    void testEncryptionWorksCorrectly() {
        try {
            byte[] text = "Encryption works correctly!".getBytes();
            var inputStream = new ByteArrayInputStream(text);
            var outputStream = new ByteArrayOutputStream();

            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            SecretKey secretKey = keyGenerator.generateKey();

            Rijndael test = new Rijndael(secretKey);

            test.encrypt(inputStream, outputStream);

            byte[] res = outputStream.toByteArray();
            var inputStreamRes = new ByteArrayInputStream(res);
            var outputStreamRes =  new ByteArrayOutputStream();

            test.decrypt(inputStreamRes, outputStreamRes);

            assertEquals("Encryption works correctly!", outputStreamRes.toString());
        } catch (NoSuchAlgorithmException | CipherException e) {
            throw new RuntimeException(e);
        }
    }
}
