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

import eu.elixir.ega.ebi.keyproviderservice.service.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author asenf
 */
@RestController
@RequestMapping("/keys")
public class KeyController {

    @Autowired
    private KeyService keyService;

    @GetMapping(value = "/formats")
    @ResponseBody
    public String[] getFormats() {
        return keyService.getEncryptionFormats();
    }

    @GetMapping(value = "/filekeys/{file_id}")
    @ResponseBody
    public String getFileKey(@PathVariable String file_id) {
        return keyService.getFileKey(file_id);
    }

    @GetMapping(value = "/paths/{file_stable_id}")
    @ResponseBody
    public String[] getKeyPath(@PathVariable String file_stable_id) {
        return keyService.getKeyPath(file_stable_id);
    }

}
