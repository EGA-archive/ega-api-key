/*
 * Copyright 2016 ELIXIR EGA
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

import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author asenf
 */
public class MyCipherConfig {
    
    // Encryption/Decryption Keys
    private static HashMap<String, String[]> keys = new HashMap<>(); // Tag->Key Location
    private static HashMap<String, PGPPublicKey> gpgpublickeys = new HashMap<>(); // For mirroring

    // Updated BC libraries
    private static KeyFingerPrintCalculator fingerPrintCalculater = new BcKeyFingerprintCalculator();
    
    public MyCipherConfig(String cipherConfigPath) {
        HashMap<String, ArrayList<String>> gpgconfig = readGpgXML(new File(cipherConfigPath));
        keys = new HashMap<>();

        Set<String> keySet = gpgconfig.keySet();
        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            String tag = iter.next(); // "AES" or "SymmetricGPG" or "PrivateGPG_{org}" or "PublicGPG_{org}"
            String[] vals = gpgconfig.get(tag).toArray(new String[gpgconfig.get(tag).size()]);
            keys.put(tag, vals);
            
            if (tag.toLowerCase().startsWith("publicgpg_")) {
                String organization = tag.substring(tag.indexOf('_')+1).trim();
                
                try { // Attempt to read the key, and put it in hash table
                    PGPPublicKey key = getKey(organization, vals); // vals[0] = path, [1][2] n/a
                    gpgpublickeys.put(organization, key);
                    gpgpublickeys.put(tag, key); // "backup"
                } catch (IOException ex) {
                }
            }
        }        
    }
    
    public HashMap<String, ArrayList<String>> readGpgXML(File xmlFile) {
        HashMap<String, ArrayList<String>> resources_temp = new HashMap<>();

        try {
            
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (xmlFile);
            doc.getDocumentElement ().normalize ();
            NodeList listOfKeys = doc.getElementsByTagName("Key");
            for(int s=0; s<listOfKeys.getLength() ; s++){
                Node nNode = listOfKeys.item(s);
                
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    
                    String type = eElement.getElementsByTagName("Type").item(0).getTextContent();
                    
                    // Add server type to local HashMap, if not already present
                    if (!resources_temp.containsKey(type))
                        resources_temp.put(type, new ArrayList<>());
                    
                    // Add server to list for that type
                    String keyPath = eElement.getElementsByTagName("KeyPath").item(0).getTextContent();
                    String keyFile = eElement.getElementsByTagName("KeyFile").item(0).getTextContent();
                    String keyKey = eElement.getElementsByTagName("KeyKey").item(0).getTextContent();
                    
                    resources_temp.get(type).add(keyPath);
                    resources_temp.get(type).add(keyFile);
                    resources_temp.get(type).add(keyKey);
                }
            }
            
        } catch (ParserConfigurationException | org.xml.sax.SAXException | IOException ex) {
        }
        return resources_temp;
    }
    
    /*
     * *************************************************************************
     * Helper Functions to perform Database Queries and set up Cache Structures
     * *************************************************************************
     */

    public String[] getAllKeys() {
        Set<String> keySet = keys.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    public String[] getKeyPath(String tag) {
        return keys.get(tag);
    }
    
    // -------------------------------------------------------------------------

    public PGPPublicKey getKey(String organization, String[] vals) throws IOException {
        PGPPublicKey pgKey = null;
        Security.addProvider(new BouncyCastleProvider());
        
        // Paths (file containing the key - no paswords for public GPG Keys)
        String path = vals[0];
        InputStream in = new FileInputStream(path);
        
        if (organization.toLowerCase().contains("ebi")) { // "pubring.gpg"
            try {
                pgKey = readPublicKey(in);
            } catch (IOException | PGPException ex) {;}
        } else { //if (organization.toLowerCase().contains("crg")) { // "/exported.gpg"
            try {
                pgKey = getEncryptionKey(getKeyring(in));
            } catch (IOException ex) {;}
        }
        in.close();

        return pgKey;
    }
    private static PGPPublicKey readPublicKey(InputStream in)
            throws IOException, PGPException {
        in = PGPUtil.getDecoderStream(in);

        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in, fingerPrintCalculater);

        //
        // we just loop through the collection till we find a key suitable for encryption, in the real
        // world you would probably want to be a bit smarter about this.
        //
        PGPPublicKey key = null;

        //
        // iterate through the key rings.
        //
        Iterator rIt = pgpPub.getKeyRings();

        while (key == null && rIt.hasNext()) {
            PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
            Iterator kIt = kRing.getPublicKeys();
            boolean encryptionKeyFound = false;

            while (key == null && kIt.hasNext()) {
                PGPPublicKey k = (PGPPublicKey) kIt.next();

                if (k.isEncryptionKey()) {
                    key = k;
                }
            }
        }

        if (key == null) {
            throw new IllegalArgumentException("Can't find encryption key in key ring.");
        }

        return key;
    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    private PGPPublicKeyRing getKeyring(InputStream keyBlockStream) throws IOException {
        // PGPUtil.getDecoderStream() will detect ASCII-armor automatically and decode it,
        // the PGPObject factory then knows how to read all the data in the encoded stream
        PGPObjectFactory factory = new PGPObjectFactory(PGPUtil.getDecoderStream(keyBlockStream), fingerPrintCalculater);

        // these files should really just have one object in them,
        // and that object should be a PGPPublicKeyRing.
        Object o = factory.nextObject();
        if (o instanceof PGPPublicKeyRing) {
            return (PGPPublicKeyRing)o;
        }
        throw new IllegalArgumentException("Input text does not contain a PGP Public Key");
    }
    private PGPPublicKey getEncryptionKey(PGPPublicKeyRing keyRing) {
        if (keyRing == null)
            return null;

        // iterate over the keys on the ring, look for one
        // which is suitable for encryption.
        Iterator keys = keyRing.getPublicKeys();
        PGPPublicKey key = null;
        while (keys.hasNext()) {
            key = (PGPPublicKey)keys.next();
            if (key.isEncryptionKey()) {
                return key;
            }
        }
        return null;
    }    

    public String getAESKey(String key) {
        String encryptionKeyPath = getKeyPath(key)[0];
        String encryptionKey = null;
        try {
            encryptionKey = Files.readFirstLine(new File(encryptionKeyPath), Charset.defaultCharset());
        } catch (IOException ex) {
            Logger.getLogger(MyCipherConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        return encryptionKey;
    }
}
