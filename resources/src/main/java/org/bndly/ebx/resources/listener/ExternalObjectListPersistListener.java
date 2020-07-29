package org.bndly.ebx.resources.listener;

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

import org.bndly.ebx.model.ExternalObjectCentricExternalObjectList;
import org.bndly.ebx.model.ExternalObjectList;
import org.bndly.ebx.model.ExternalObjectListAssocation;
import org.bndly.ebx.model.UserCentricExternalObjectList;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.listener.MergeListener;
import org.bndly.schema.api.listener.PersistListener;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.Date;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(service = {ExternalObjectListPersistListener.class, PersistListener.class, MergeListener.class}, immediate = true)
public class ExternalObjectListPersistListener implements PersistListener, MergeListener {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	@Activate
	public void activate() {
		schemaBeanFactory.getEngine().addListener(this);
	}

	@Deactivate
	public void deactivate() {
		schemaBeanFactory.getEngine().removeListener(this);
	}

	@Override
	public void onPersist(Record record) {
		synchronizeAssociations(record);
	}

	@Override
	public void onMerge(Record record) {
		synchronizeAssociations(record);
	}

	private void synchronizeAssociations(final Record record) {
		String tn = record.getType().getName();
		if (UserCentricExternalObjectList.class.getSimpleName().equals(tn) || ExternalObjectCentricExternalObjectList.class.getSimpleName().equals(tn)) {
			new ListSynchronization(schemaBeanFactory).synchronize(record, "associations", "list", ExternalObjectListAssocation.class.getSimpleName(), new ListSynchronization.TypeSpecificCallback<Long, ExternalObjectListAssocation, ExternalObjectListAssocation>() {

				@Override
				public Long getKey(ExternalObjectListAssocation value) {
					return ((ActiveRecord) value).getId();
				}

				@Override
				public Long getKeyFromValue(ExternalObjectListAssocation value) {
					return getKey(value);
				}

				@Override
				public void applyValuesToNewObject(ExternalObjectListAssocation value, ExternalObjectListAssocation a) {
					if (value.getCreatedOn() == null) {
						value.setCreatedOn(new Date());
					} else if (value.getUpdatedOn() == null) {
						value.setUpdatedOn(new Date());
					}
					value.setExternalObject(a.getExternalObject());
					value.setQuantity(a.getQuantity());
					ExternalObjectList list = (ExternalObjectList) schemaBeanFactory.getSchemaBean(record);
					value.setList(list);
				}
			});
		}
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

}
