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
package eu.elixir.ega.ebi.keyproviderservice.rest;

import eu.elixir.ega.ebi.keyproviderservice.dto.KeyPath;
import eu.elixir.ega.ebi.keyproviderservice.service.KeyService;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author asenf
 */
@RestController
@RequestMapping("/keys")
public class KeyController {

    @Autowired
    private KeyService keyService;

    @GetMapping(value = "/filekeys/{fileId}")
    @ResponseBody
    public String getFileKey(@PathVariable String fileId) {
        return keyService.getFileKey(fileId);
    }

    @GetMapping(value = "/retrieve/{keyType}/{keyId}/public/user")
    @ResponseBody
    public String getPublicKey(@PathVariable String keyType, @PathVariable String keyId) {
        return keyService.getPublicKey(keyType, keyId);
    }

    @GetMapping(value = "/retrieve/{keyType}/{keyId}/public")
    @ResponseBody
    public PGPPublicKey getPublicKeyFromPrivate(@PathVariable String keyType, @PathVariable String keyId) {
        return keyService.getPublicKeyFromPrivate(keyType, keyId);
    }

    @GetMapping(value = "/retrieve/{keyType}/{keyId}/private")
    @ResponseBody
    public PGPPrivateKey getPrivateKey(@PathVariable String keyType, @PathVariable String keyId) {
        return keyService.getPrivateKey(keyType, keyId);
    }

    @GetMapping(value = "/retrieve/{keyType}/{keyId}/private/path")
    @ResponseBody
    public KeyPath getPrivateKeyPath(@PathVariable String keyType, @PathVariable String keyId) {
        return keyService.getPrivateKeyPath(keyType, keyId);
    }

    @GetMapping(value = "/retrieve/{keyType}/ids")
    @ResponseBody
    public Set<Long> getPublicKey(@PathVariable String keyType) {
        return keyService.getKeyIDs(keyType);
    }

}
