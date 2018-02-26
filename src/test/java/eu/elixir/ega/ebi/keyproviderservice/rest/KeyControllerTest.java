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
package eu.elixir.ega.ebi.keyproviderservice.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.elixir.ega.ebi.keyproviderservice.service.KeyService;

@RunWith(SpringRunner.class)
@WebMvcTest(KeyController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public final class KeyControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private KeyService keyService;

	@Test
	public void testGetFormats() throws Exception {
		when(keyService.getEncryptionFormats()).thenReturn(new String[] { "key1", "key2" });
		final MockHttpServletResponse response = mockMvc.perform(get("/keys/formats").accept(APPLICATION_JSON))
				.andReturn().getResponse();
		assertThat(response.getStatus(), equalTo(OK.value()));
	}

	@Test
	public void testGetFileKey() throws Exception {
		when(keyService.getFileKey("file_id")).thenReturn("filekey");
		final MockHttpServletResponse response = mockMvc
				.perform(get("/keys/filekeys/file_id").accept(APPLICATION_JSON)).andReturn().getResponse();
		assertThat(response.getStatus(), equalTo(OK.value()));
		assertThat(response.getContentAsString(), equalTo("filekey"));
	}

	@Test
	public void testGetKeyPath() throws Exception {
		final String keypaths[] = { "keypath1", "keypath2" };
		when(keyService.getKeyPath("file_stable_id")).thenReturn(keypaths);
		final MockHttpServletResponse response = mockMvc
				.perform(get("/keys/paths/file_stable_id").accept(APPLICATION_JSON)).andReturn()
				.getResponse();
		String expectedJson = mapToJson(keypaths);
		assertThat(response.getStatus(), equalTo(OK.value()));
		assertThat(response.getContentAsString(), equalTo(expectedJson));
	}
	
	/**
	 * Maps an object into JSON string. Uses a Jackson ObjectMapper.
	 * @param object
	 * @return {@link String}
	 * @throws JsonProcessingException
	 */
	private String mapToJson(Object object) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(object);
	}
}
