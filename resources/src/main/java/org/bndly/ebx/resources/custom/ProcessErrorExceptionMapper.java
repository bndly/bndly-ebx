package org.bndly.ebx.resources.custom;

/*-
 * #%L
 * org.bndly.ebx.resources
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

import org.bndly.common.bpm.exception.ProcessErrorException;
import org.bndly.rest.common.beans.error.ErrorRestBean;
import org.bndly.rest.common.beans.util.ExceptionMessageUtil;
import org.bndly.rest.controller.api.ExceptionMapper;
import org.bndly.rest.controller.api.Response;
import org.osgi.service.component.annotations.Component;

@Component(service = ExceptionMapper.class, immediate = true)
public class ProcessErrorExceptionMapper implements ExceptionMapper<ProcessErrorException> {

	@Override
	public Response toResponse(ProcessErrorException e) {
		ErrorRestBean msg = new ErrorRestBean();
		msg.setName("ProcessError");
		ExceptionMessageUtil.createKeyValue(msg, "errorCode", e.getErrorCode());
		ExceptionMessageUtil.createKeyValue(msg, "processName", e.getProcessName());
		ExceptionMessageUtil.createKeyValue(msg, "processId", e.getProcessId());
		return Response.status(400).entity(msg);
	}

}
