/*
 * Copyright 2017 ELIXIR EGA
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
package eu.elixir.ega.ebi.keyproviderservice.service.internal;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import eu.elixir.ega.ebi.keyproviderservice.config.MyCipherConfig;
import eu.elixir.ega.ebi.keyproviderservice.domain.entity.FileKey;
import eu.elixir.ega.ebi.keyproviderservice.domain.repository.FileKeyRepository;
import eu.elixir.ega.ebi.keyproviderservice.service.KeyService;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author asenf
 */
@Service
@EnableDiscoveryClient
public class KeyServiceImpl implements KeyService {

    @Autowired
    private MyCipherConfig myCipherConfig;

    @Autowired
    private FileKeyRepository fileKeyRepository;
    
    @Override
    @HystrixCommand
    @ResponseBody
    public String getFileKey(String fileId) {
        String key = null;
/*
        if (fileId.equalsIgnoreCase("AES") || fileId.equalsIgnoreCase("GPG"))
            key = myCipherConfig.getAESKey(fileId.toUpperCase());
        else {
            // TODO
            // Get Key for File ID from source

            // Until then: Use the same key as a patch
            key = myCipherConfig.getAESKey("AES");
        }
*/
        Iterable<FileKey> findByFileId = fileKeyRepository.findByFileId(fileId);
        Iterator<FileKey> iterator = findByFileId.iterator();
        if (iterator.hasNext()) {
            FileKey next = iterator.next();
            key = next.getEncryptionKey();
        }
        
        return key;
    }

    // Downstream Helper Function - List supported ReEncryption Formats
    public String[] getEncryptionFormats() {
        return myCipherConfig.getAllKeys();
    }

    @Override
    public String[] getKeyPath(String key) {
        return myCipherConfig.getKeyPath(key);
    }
}
