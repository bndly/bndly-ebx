package org.bndly.business.executor;

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

import org.bndly.common.bpm.annotation.ProcessVariable;
import org.bndly.common.bpm.api.TaskExecutor;
import org.bndly.common.mail.api.EmailAddress;
import org.bndly.common.mail.api.Mail;
import org.bndly.common.mail.api.MailContent;
import org.bndly.common.mail.api.MailException;
import org.bndly.common.mail.api.MailImpl;
import org.bndly.common.mail.api.MailTemplate;
import org.bndly.common.mail.api.Mailer;
import org.bndly.ebx.model.CheckoutRequest;
import org.bndly.ebx.model.Person;
import org.bndly.ebx.model.PurchaseOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotifyCustomerOfOrderTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor, MailerDependent {

	private static final Logger LOG = LoggerFactory.getLogger(NotifyCustomerOfOrderTaskExecutorImpl.class);
	private Mailer mailer;

	@ProcessVariable(ProcessVariable.Access.READ)
	private CheckoutRequest checkoutRequest;
	@ProcessVariable(ProcessVariable.Access.READ)
	private String senderAddress;
	@ProcessVariable(ProcessVariable.Access.READ)
	private String subject;

	@Override
	public void run() {
		PurchaseOrder order = checkoutRequest.getOrder();
		MailImpl mail = new MailImpl();
		mail.setContent(new ArrayList<MailContent>());
		List<EmailAddress> listOfEmailAddresses = new ArrayList<>();
		Person orderer = order.getOrderer();
		String email = orderer.getEmail();
		listOfEmailAddresses.add(new EmailAddress(email));
		mail.setTo(listOfEmailAddresses);
		if (senderAddress != null && !senderAddress.isEmpty()) {
			mail.setSender(new EmailAddress(senderAddress));
		}
		if (subject != null && !subject.isEmpty()) {
			mail.setSubject(subject);
		}

		try {
			MailTemplate template = buildTemplateToRender(order, mail, false);
			MailContent renderResult = mailer.renderTemplate(template);
			if (renderResult != null) {
				mail.getContent().add(renderResult);
			}

			template = buildTemplateToRender(order, mail, true);
			renderResult = mailer.renderTemplate(template);
			if (renderResult != null) {
				mail.getContent().add(renderResult);
			}

			mailer.send(template.getMail());
		} catch (MailException e) {
			LOG.warn("failed to notify customer: " + e.getMessage(), e);
		}
	}

	private MailTemplate buildTemplateToRender(PurchaseOrder order, Mail mail, boolean asHtml) {
		MailTemplate template = new MailTemplate();
		template.setEntity(order);
		template.setTemplateName("ebx/orderOrderedConfimationMailTemplate.vm");
		if (asHtml) {
			template.setContentType("text/html");
		} else {
			template.setContentType("text/plain");
		}
		template.setMail(mail);
		Map<String, Object> ctx = new HashMap<>();
		ctx.put("asHtml", asHtml);
		template.setContextData(ctx);
		template.setLocale(Locale.GERMAN);
		return template;
	}

	@Override
	public void setMailer(Mailer mailer) {
		this.mailer = mailer;
	}
}
