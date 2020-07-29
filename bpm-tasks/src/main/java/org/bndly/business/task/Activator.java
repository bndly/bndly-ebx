package org.bndly.business.task;

/*-
 * #%L
 * org.bndly.ebx.bpm-tasks
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

import org.bndly.business.executor.AbstractSchemaRelatedTaskExecutor;
import org.bndly.business.executor.ContextResolverDependent;
import org.bndly.business.executor.MailerDependent;
import org.bndly.business.executor.PaymentProviderDependent;
import org.bndly.common.bpm.api.ContextResolver;
import org.bndly.common.bpm.api.TaskExecutor;
import org.bndly.common.mail.api.Mailer;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = {Activator.class, ExecutorInitializer.class}, immediate = true)
@Designate(ocd = Activator.Configuration.class)
public class Activator implements ExecutorInitializer {

	@ObjectClassDefinition
	public @interface Configuration {

		@AttributeDefinition(description = "The base url to the payment provider that will be used to perform payment subprocesses.")
		String paymentProviderUrl() default "http://change.me";
	}

	private static Activator INSTANCE;

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	private String paymentProviderUrl;

	@Reference
	private Mailer mailer;
	
	@Reference
	private ContextResolver contextResolver;

	@Activate
	public void activate() {
		INSTANCE = this;
	}

	public static Activator getInstance() {
		return INSTANCE;
	}

	@Override
	public void initialize(TaskExecutor executor) {
		if (AbstractSchemaRelatedTaskExecutor.class.isInstance(executor)) {
			AbstractSchemaRelatedTaskExecutor asrte = (AbstractSchemaRelatedTaskExecutor) executor;
			asrte.setEngine(schemaBeanFactory.getEngine());
			asrte.setJsonSchemaBeanFactory(schemaBeanFactory.getJsonSchemaBeanFactory());
			asrte.setSchemaBeanFactory(schemaBeanFactory);
		}
		if (PaymentProviderDependent.class.isInstance(executor)) {
			PaymentProviderDependent paymentProviderDependent = (PaymentProviderDependent) executor;
			paymentProviderDependent.setPaymentProviderUrl(paymentProviderUrl);
		}
		if (MailerDependent.class.isInstance(executor)) {
			MailerDependent mailerDependent = (MailerDependent) executor;
			mailerDependent.setMailer(mailer);
		}
		if(ContextResolverDependent.class.isInstance(executor)) {
			ContextResolverDependent contextResolverDependent = (ContextResolverDependent) executor;
			contextResolverDependent.setContextResolver(contextResolver);
		}
	}
}
