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
/**
 * Author:  asenf
 * Created: 17-Feb-2017
 */

CREATE TABLE file_key_test.file_key (
	file_id varchar(15) NOT NULL,
	encryption_key varchar(256) NOT NULL,
	key_format varchar(128) NULL,
	CONSTRAINT file_key_pkey PRIMARY KEY (file_id),
	CONSTRAINT file_key_key_format_fk FOREIGN KEY (key_format) REFERENCES file_key_test.key_format_cv(key_format)
)
WITH (
	OIDS=FALSE
);

