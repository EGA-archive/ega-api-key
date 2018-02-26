/*
 * Copyright 2016 ELIXIR EGA
 * Copyright 2016 Alexander Senf
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import eu.elixir.ega.ebi.keyproviderservice.config.MyCipherConfig;
import eu.elixir.ega.ebi.keyproviderservice.domain.entity.FileKey;
import eu.elixir.ega.ebi.keyproviderservice.domain.repository.FileKeyRepository;
import eu.elixir.ega.ebi.keyproviderservice.service.KeyService;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public final class KeyServiceImplTest {

	@TestConfiguration
	static class KeyServiceImplTestContextConfiguration {

		@Bean
		public KeyService keyService() {
			return new KeyServiceImpl();
		}
	}

	@Autowired
	private KeyService keyService;

	@MockBean
	private MyCipherConfig myCipherConfig;

	@MockBean
	private FileKeyRepository fileKeyRepository;

	@Before
	public void setUp() {
		final FileKey fileKey = new FileKey("fileId", "encryption_key", "key_format");
		final Iterable<FileKey> outputFileKey = Arrays.asList(fileKey);
		when(fileKeyRepository.findByFileId("fileId")).thenReturn(
				outputFileKey);
		when(myCipherConfig.getAllKeys()).thenReturn(new String[]{"key1","key2"});
		when(myCipherConfig.getKeyPath("key")).thenReturn(new String[]{"keypath"});
	}

	@Test
	public void testGetFileKey() {
		assertThat(keyService.getFileKey("fileId"), equalTo("encryption_key"));
	}
	
	@Test
	public void testGetEncryptionFormats() {
		assertNotNull(keyService.getEncryptionFormats());
	}
	
	@Test
	public void testGetKeyPath() {
		assertNotNull(keyService.getKeyPath("key"));
	}

}