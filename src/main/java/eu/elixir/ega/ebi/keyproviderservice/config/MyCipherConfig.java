/*
 * Copyright 2018 ELIXIR EGA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.elixir.ega.ebi.keyproviderservice.config;

import eu.elixir.ega.ebi.keyproviderservice.dto.KeyPath;
import eu.elixir.ega.ebi.keyproviderservice.dto.PublicKey;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import org.bouncycastle.openpgp.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * @author asenf
 */
public class MyCipherConfig {
    
    @Autowired
    private RestTemplate restTemplate;

    // Actual Key objects (not sure if this is necessary)
    private HashMap<Long, PGPPrivateKey>  pgpPriv;
    
    // Paths
    private HashMap<Long, KeyPath> keyPaths;
    
    // Public Key URL
    // https://github.com/mailvelope/keyserver/blob/master/README.md
    private String publicKeyUrl;
    
    // Load (1) Private Key
    public MyCipherConfig(String[] keyPath, String[] keyPassPath, String publicKeyUrl) {
        this.publicKeyUrl = publicKeyUrl;
        
        if (keyPath==null) return;
        
        // Instantiate Data Structures
        pgpPriv = new HashMap<>();
        keyPaths = new HashMap<>();

        // Get Key ID and store both Key paths and Key objects in a Hash Map
        for (int i=0; i< keyPath.length; i++) {
            try {
                PGPPrivateKey pgpPriv_ = extractKey(readFileAsString(keyPath[i]), readFileAsString(keyPassPath[i]));
                long keyId = pgpPriv_.getKeyID();
                pgpPriv.put(keyId, pgpPriv_);
                keyPaths.put(keyId, new KeyPath(keyPath[i], keyPassPath[i]));
            } catch (IOException ex) {
                Logger.getLogger(MyCipherConfig.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /*
     * Accessors - Private Key, and Paths - Used by REST endpoint
     */
    public PGPPrivateKey getPrivateKey(Long keyId) {
        return this.pgpPriv.get(keyId);
    }   
    public PGPPublicKey getPublicKey(Long keyId) {
        PGPPublicKey key = null;
        try {
            key = new PGPPublicKey(this.pgpPriv.get(keyId).getPublicKeyPacket(), new JcaKeyFingerprintCalculator());
        } catch (PGPException ex) {
            Logger.getLogger(MyCipherConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        return key;
    }
    
    public KeyPath getKeyPaths(Long keyId) {
        return this.keyPaths.get(keyId);
    }
    
    public Set<Long> getKeyIDs() {
        return this.pgpPriv.keySet();
    }
    
    // https://github.com/mailvelope/keyserver/blob/master/README.md
    public String getPublicKeyById(String id) {
        ResponseEntity<PublicKey> publicKey = restTemplate.getForEntity(this.publicKeyUrl + "?id=" + id, PublicKey.class);
        return publicKey.getBody().getPublicKeyArmored();
    }

    // https://github.com/mailvelope/keyserver/blob/master/README.md
    public String getPublicKeyByEmail(String email) {
        ResponseEntity<PublicKey> publicKey = restTemplate.getForEntity(this.publicKeyUrl + "?email=" + email, PublicKey.class);
        return publicKey.getBody().getPublicKeyArmored();
    }
    
    /*
     * Utility Functions
     */
    public PGPPrivateKey extractKey(String sKey, String sPass) {
        PGPPrivateKey key = null;
        
        try {
            PGPSecretKey secretKey = getSecretKey(sKey);

            // Extract Private Key from Secret Key
            PGPDigestCalculatorProvider digestCalc = new JcaPGPDigestCalculatorProviderBuilder().build();
            PBESecretKeyDecryptor decryptor = new JcePBESecretKeyDecryptorBuilder(digestCalc)
                .build(sPass.toCharArray());

            key = secretKey.extractPrivateKey(decryptor);
        } catch (IOException | PGPException ex) {
            System.out.println(ex.toString());
        }
        
        return key;
    }
    
    // Return the contents of a file as String
    public String readFileAsString(String filePath) throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
    
    /*
     * Helper Functins
     */
    // Extract Secret Key from File
    private PGPSecretKey getSecretKey(String privateKeyData) throws IOException, PGPException {
        PGPPrivateKey privKey = null;
        try (InputStream privStream = new ArmoredInputStream(new ByteArrayInputStream(privateKeyData.getBytes("UTF-8")))) {
            PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(privStream), new JcaKeyFingerprintCalculator());
            Iterator keyRingIter = pgpSec.getKeyRings();
            while (keyRingIter.hasNext()) {
                PGPSecretKeyRing keyRing = (PGPSecretKeyRing)keyRingIter.next();
                Iterator keyIter = keyRing.getSecretKeys();
                while (keyIter.hasNext()) {
                    PGPSecretKey key = (PGPSecretKey)keyIter.next();

                    if (key.isSigningKey()) {
                        return key;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Can't find signing key in key ring.");
    }

}
