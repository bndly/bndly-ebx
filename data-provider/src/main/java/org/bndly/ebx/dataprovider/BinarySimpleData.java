package org.bndly.ebx.dataprovider;

/*-
 * #%L
 * org.bndly.ebx.data-provider
 * %%
 * Copyright (C) 2013 - 2020 Cybercon GmbH
 * %%
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
 * #L%
 */

import org.bndly.common.data.api.SimpleData;
import org.bndly.common.data.io.ReplayableInputStream;
import org.bndly.ebx.model.BinaryData;
import java.util.Date;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class BinarySimpleData extends SimpleData {
	
	private final BinaryData binaryData;

	public BinarySimpleData(BinaryData binaryData, LazyLoader lazyLoader) {
		super(lazyLoader);
		this.binaryData = binaryData;
	}

	@Override
	public void setInputStream(ReplayableInputStream inputStream) {
		super.setInputStream(inputStream);
		binaryData.setBytes(inputStream);
	}

	@Override
	public void setContentType(String contentType) {
		super.setContentType(contentType);
		binaryData.setContentType(contentType);
	}

	@Override
	public void setCreatedOn(Date createdOn) {
		super.setCreatedOn(createdOn);
		binaryData.setCreatedOn(createdOn);
	}

	@Override
	public void setUpdatedOn(Date updatedOn) {
		super.setUpdatedOn(updatedOn);
		binaryData.setUpdatedOn(updatedOn);
	}

	public BinaryData getBinaryData() {
		return binaryData;
	}
	
}
