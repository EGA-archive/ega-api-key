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
package eu.elixir.ega.ebi.keyproviderservice.domain.repository;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import eu.elixir.ega.ebi.keyproviderservice.domain.entity.FileKey;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations="classpath:application-test.properties")
public class FileKeyRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private FileKeyRepository fileKeyRepository;
	private FileKey fileKey;
	private Iterable<FileKey> fileKeyOutput;

	@Test
	public void testFindByFileId() {
		givenFileKey();
		whenFindByFileIdRequested();
		thenVerifyFileKey();
	}

	private void givenFileKey() {
		fileKey = new FileKey("fileId", "encryption_key", "key_format");
		entityManager.persist(fileKey);
		entityManager.flush();
	}

	private void whenFindByFileIdRequested() {
		fileKeyOutput = fileKeyRepository.findByFileId("fileId");
	}

	private void thenVerifyFileKey() {
		final FileKey resultKeyOutput = fileKeyOutput.iterator().next();
		assertThat(resultKeyOutput,equalTo(fileKey));
	}
}
