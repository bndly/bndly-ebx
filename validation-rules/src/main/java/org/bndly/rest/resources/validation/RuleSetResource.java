package org.bndly.rest.resources.validation;

/*-
 * #%L
 * org.bndly.ebx.validation-rules
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

import org.bndly.common.service.validation.RuleSetRestBean;
import org.bndly.common.service.validation.RuleSetsRestBean;
import org.bndly.ebx.validation.RuleSetProvider;
import org.bndly.rest.api.Context;
import org.bndly.rest.api.ResourceURI;
import org.bndly.rest.api.ResourceURIBuilder;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.atomlink.api.annotation.AtomLinks;
import org.bndly.rest.atomlink.api.annotation.Parameter;
import org.bndly.rest.cache.api.CacheTransaction;
import org.bndly.rest.cache.api.CacheTransactionFactory;
import org.bndly.rest.common.beans.Services;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.controller.api.GET;
import org.bndly.rest.controller.api.Meta;
import org.bndly.rest.controller.api.POST;
import org.bndly.rest.controller.api.PUT;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.PathParam;
import org.bndly.rest.controller.api.Response;
import java.util.List;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Path("validationRules")
@Component(service = RuleSetResource.class, immediate = true)
public class RuleSetResource {

	@Reference
	private RuleSetProvider ruleSetProvider;
	@Reference
	private ControllerResourceRegistry controllerResourceRegistry;
	@Reference
	private CacheTransactionFactory cacheTransactionFactory;

	@Activate
	public void activate() {
		controllerResourceRegistry.deploy(this);
	}

	@Deactivate
	public void deactivate() {
		controllerResourceRegistry.undeploy(this);
	}

	@GET
	@AtomLinks({
		@AtomLink(rel = "validationRules", target = Services.class),
		@AtomLink(rel = "list", target = RuleSetRestBean.class),
		@AtomLink(target = RuleSetsRestBean.class)
	})
	public Response list() {
		List<RuleSetRestBean> l = ruleSetProvider.list();
		RuleSetsRestBean all = new RuleSetsRestBean();
		if (l != null) {
			for (RuleSetRestBean ruleSetRestBean : l) {
				all.add(ruleSetRestBean);
			}
		}
		return Response.ok(all);
	}

	@POST
	@AtomLink(target = RuleSetsRestBean.class)
	public Response create(RuleSetRestBean rs, @Meta Context context) {
		ruleSetProvider.saveRuleSet(rs);
		ResourceURIBuilder builder = context.createURIBuilder();
		ResourceURI uri = builder.pathElement("validationRules").pathElement(rs.getName()).build();
		try (CacheTransaction cacheTransaction = cacheTransactionFactory.createCacheTransaction()) {
			cacheTransaction.flush("/validationRules");
		}
		return Response.created(uri.asString());
	}

	@GET
	@Path("{ruleSetName}")
	@AtomLink(target = RuleSetRestBean.class, parameters = {
		@Parameter(name = "ruleSetName", expression = "${this.getName()}")
	})
	public Response read(@PathParam("ruleSetName") String ruleSetName) {
		RuleSetRestBean rs = ruleSetProvider.getRuleSet(ruleSetName);
		if (rs == null) {
			return Response.status(404);
		}
		return Response.ok(rs);
	}

	@PUT
	@Path("{ruleSetName}")
	@AtomLink(target = RuleSetRestBean.class, parameters = {
		@Parameter(name = "ruleSetName", expression = "${this.getName()}")
	})
	public Response update(@PathParam("ruleSetName") String ruleSetName, RuleSetRestBean ruleSetRestBean, @Meta Context context) {
		ruleSetRestBean.setName(ruleSetName);
		ruleSetProvider.saveRuleSet(ruleSetRestBean);
		try (CacheTransaction cacheTransaction = cacheTransactionFactory.createCacheTransaction()) {
			cacheTransaction.flush(context.getURI().pathAsString());
		}
		return Response.NO_CONTENT;
	}

	public void setRuleSetProvider(RuleSetProvider ruleSetProvider) {
		this.ruleSetProvider = ruleSetProvider;
	}

}
