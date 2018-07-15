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

CREATE TABLE encryption_key (
	encryption_key_id serial NOT NULL,
	alias varchar(128) NOT NULL,
	encryption_key text NOT NULL,
	key_format varchar(128) NOT NULL,
	CONSTRAINT encryption_key_alias_key UNIQUE (alias),
	CONSTRAINT encryption_key_encryption_key_key UNIQUE (encryption_key),
	CONSTRAINT encryption_key_pkey PRIMARY KEY (encryption_key_id),
	CONSTRAINT encryption_key_key_format_fkey FOREIGN KEY (key_format) REFERENCES key_formats_cv(key_format)
)
WITH (
	OIDS=FALSE
) ;