package org.bndly.ebx.client.service.impl;

/*-
 * #%L
 * org.bndly.ebx.client.generated-service
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

import org.bndly.common.service.model.api.CollectionIndexer;
import org.bndly.common.service.model.api.FieldIndexerFunction;
import org.bndly.common.service.model.api.IndexerFunction;
import org.bndly.common.service.model.api.ReferenceBuildingException;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.common.service.shared.api.ServiceReference;
import org.bndly.ebx.client.service.api.UserIDProvider;
import org.bndly.ebx.model.PaymentDetailAssignment;
import org.bndly.ebx.model.PaymentDetails;
import org.bndly.ebx.model.Person;
import org.bndly.ebx.model.impl.PaymentDetailAssignmentImpl;
import org.bndly.ebx.model.impl.PaymentDetailsImpl;
import org.bndly.ebx.model.impl.PersonImpl;
import org.bndly.ebx.client.service.api.CustomPaymentDetailAssignmentService;
import org.bndly.ebx.client.service.api.PaymentDetailAssignmentService;
import org.bndly.ebx.client.service.api.PersonService;
import org.bndly.rest.client.exception.ClientException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by alexp on 18.06.15.
 */
public class CustomPaymentDetailAssignmentServiceImpl
		implements CustomPaymentDetailAssignmentService, ProxyAware<PaymentDetailAssignmentService> {

	private UserIDProvider userIDProvider;
	private PersonService personService;

	private PaymentDetailAssignmentService thisProxy;

	@Override
	public boolean removePaymentDetailsOfCurrentUser(long id) throws ClientException {
		PaymentDetailAssignment findPda = new PaymentDetailAssignmentImpl();
		try {
			findPda.setPerson(thisProxy.modelAsReferableResource(personService.assertCurrentUserExistsAsPerson()).buildReference());
		} catch (ReferenceBuildingException e) {
			return false;
		}
		Collection<PaymentDetailAssignment> paymentDetailsAssignments = thisProxy.findAllLike(findPda);
		if (paymentDetailsAssignments != null) {
			for (PaymentDetailAssignment pda : paymentDetailsAssignments) {
				if (getId(pda.getPaymentDetail()) == id) {
					return thisProxy.delete(pda);
				}
			}
		}

		return false;
	}

	@Override
	public PaymentDetails addPaymentDetailsToCurrentUser(PaymentDetails paymentDetails) throws ClientException {
		if (getId(paymentDetails) == null) {
			// create
			PaymentDetailAssignment tmp = new PaymentDetailAssignmentImpl();
			try {
				tmp.setPerson(thisProxy.modelAsReferableResource(personService.assertCurrentUserExistsAsPerson()).buildReference());
			} catch (ReferenceBuildingException e) {
				return paymentDetails;
			}
			tmp.setPaymentDetail(paymentDetails);
			tmp = thisProxy.create(tmp);
			return tmp.getPaymentDetail();

		} else {
			// update
			List<PaymentDetailAssignment> assignments = getPaymentDetailsAssignmentsOfCurrentUser();
			Map<Long, PaymentDetailAssignment> index = new CollectionIndexer<Long, PaymentDetailAssignment>().index(assignments, new IndexerFunction<Long, PaymentDetailAssignment>() {
				@Override
				public Long index(PaymentDetailAssignment o) {
					return getId(o.getPaymentDetail());
				}
			});
			PaymentDetailAssignment assignment = index.get(getId(paymentDetails));
			assignment.setPaymentDetail(paymentDetails);
			return thisProxy.update(assignment).getPaymentDetail();
		}
	}

	@Override
	public List<PaymentDetails> getPaymentDetailsOfCurrentUser() throws ClientException {
		List<PaymentDetails> paymentDetails = new ArrayList<>();
		Collection<PaymentDetailAssignment> l = getPaymentDetailsAssignmentsOfCurrentUser();
		if (l != null) {
			for (PaymentDetailAssignment addressAssignment : l) {
				paymentDetails.add(addressAssignment.getPaymentDetail());
			}
		}
		return paymentDetails;
	}

	private List<PaymentDetailAssignment> getPaymentDetailsAssignmentsOfCurrentUser() throws ClientException {
		PaymentDetailAssignment tmp = getCurrentUserAssignmentPrototype();
		return thisProxy.findAllLike(tmp, ArrayList.class);
	}

	@Override
	public PaymentDetails getPaymentDetailsOfCurrentUserById(long id) throws ClientException {
		Map<Long, PaymentDetails> index = new CollectionIndexer<Long, PaymentDetails>().index(getPaymentDetailsOfCurrentUser(), new FieldIndexerFunction<Long, PaymentDetails>("id"));
		return index.get(id);
	}

	private PaymentDetailAssignment getCurrentUserAssignmentPrototype() {
		PaymentDetailAssignment tmp = new PaymentDetailAssignmentImpl();
		tmp.setPerson(getCurrentUserPrototype());
		return tmp;
	}

	private Long getId(PaymentDetails model) {
		if (PaymentDetailsImpl.class.isInstance(model)) {
			return ((PaymentDetailsImpl) model).getId();
		}
		return null;
	}

	private Person getCurrentUserPrototype() {
		Person p = new PersonImpl();
		p.setExternalUserId(userIDProvider.getCurrentUserID());
		return p;
	}

	public void setUserIDProvider(UserIDProvider userIDProvider) {
		this.userIDProvider = userIDProvider;
	}

	@ServiceReference
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	@Override
	public void setThisProxy(PaymentDetailAssignmentService serviceProxy) {
		thisProxy = serviceProxy;
	}
}
