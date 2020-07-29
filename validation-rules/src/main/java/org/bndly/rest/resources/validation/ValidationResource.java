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
import org.bndly.ebx.validation.RuleSetProvider;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.atomlink.api.annotation.AtomLinkDescription;
import org.bndly.rest.atomlink.api.annotation.AtomLinks;
import org.bndly.rest.controller.api.GET;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.Response;
import org.bndly.rest.descriptor.DelegatingAtomLinkDescription;
import org.bndly.rest.entity.resources.EntityResource;
import org.bndly.rest.entity.resources.descriptor.ListLinkDescriptor;
import org.bndly.rest.entity.resources.descriptor.SelfLinkDescriptor;
import java.lang.reflect.Method;

@Path("")
public class ValidationResource {

	private final EntityResource entityResource;
    private final Class<?> restBeanClass;
    private final String segment;
    private RuleSetProvider ruleSetProvider;

	public ValidationResource(EntityResource entityResource) {
		this.entityResource = entityResource;
		this.restBeanClass = entityResource.getRestBeanType();
		this.segment = entityResource.getType().getSchema().getName()+"/"+entityResource.getType().getName();
	}

	public EntityResource getEntityResource() {
		return entityResource;
	}

    public String getSegment() {
        return segment;
    }

    public static class ListDescriptor extends ListLinkDescriptor {

		public ListDescriptor(ValidationResource validationResource) {
			super(validationResource.entityResource);
		}

		@Override
		public AtomLinkDescription getAtomLinkDescription(Object controller, Method method, AtomLink atomLink) {
			AtomLinkDescription d = super.getAtomLinkDescription(controller, method, atomLink);
			return new DelegatingAtomLinkDescription(d) {

				@Override
				public Class<?> getReturnType() {
					return RuleSetRestBean.class;
				}
				
			};
		}
    }

    public static class RestBeanDescriptor extends SelfLinkDescriptor {

		public RestBeanDescriptor(ValidationResource validationResource) {
			super(validationResource.entityResource);
		}

        @Override
        public AtomLinkDescription getAtomLinkDescription(Object controller, Method method, AtomLink atomLink) {
            AtomLinkDescription d = super.getAtomLinkDescription(controller, method, atomLink);
			return new DelegatingAtomLinkDescription(d) {

				@Override
				public Class<?> getReturnType() {
					return RuleSetRestBean.class;
				}
			};
        }
    }

    @GET
    @AtomLinks({
        @AtomLink(rel = "rules", descriptor = ListDescriptor.class),
        @AtomLink(rel = "rules", descriptor = RestBeanDescriptor.class)
    })
	@Path("rules")
    public Response getValidationRules() {
        RuleSetRestBean rs = ruleSetProvider.getRuleSet(restBeanClass);
        if (rs == null) {
            return Response.status(404);
        }
        return Response.ok(rs);
    }

    public void setRuleSetProvider(RuleSetProvider ruleSetProvider) {
        this.ruleSetProvider = ruleSetProvider;
    }
}
