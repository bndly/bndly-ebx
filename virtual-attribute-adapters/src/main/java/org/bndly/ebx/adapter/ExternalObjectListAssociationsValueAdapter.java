package org.bndly.ebx.adapter;

/*-
 * #%L
 * org.bndly.ebx.virtual-attribute-adapters
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

import org.bndly.ebx.model.ExternalObjectList;
import org.bndly.ebx.model.ExternalObjectListAssocation;
import org.bndly.schema.api.QueryBasedRecordListInitializer;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.RecordList;
import org.bndly.schema.api.VirtualAttributeAdapter;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.InverseAttribute;
import org.bndly.schema.model.NamedAttributeHolder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(service = VirtualAttributeAdapter.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class ExternalObjectListAssociationsValueAdapter implements VirtualAttributeAdapter<InverseAttribute> {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	@Activate
	public void activate() {
		schemaBeanFactory.getEngine().getVirtualAttributeAdapterRegistry().register(this);
	}

	@Deactivate
	public void deactivate() {
		schemaBeanFactory.getEngine().getVirtualAttributeAdapterRegistry().unregister(this);
	}

	@Override
	public boolean supports(InverseAttribute attribute, NamedAttributeHolder holder) {
		if (!"associations".equals(attribute.getName())) {
			return false;
		}
		return SchemaInspectionUtil.isAssignableTo(holder, ExternalObjectList.class.getSimpleName());
	}

	@Override
	public Object read(InverseAttribute attribute, Record r) {
		if (r.isAttributePresent(attribute.getName())) {
			return r.getAttributeValue(attribute.getName());
		}
		RecordContext.RecordListInitializer initializer = new QueryBasedRecordListInitializer(schemaBeanFactory.getEngine(), "PICK " + ExternalObjectListAssocation.class.getSimpleName() + " a IF a.list.id=? AND a.list TYPED ?", r.getContext(), r.getId(), r.getType().getName());
		RecordList list = r.getContext().createList(initializer, r, attribute);
		r.setAttributeValue(attribute.getName(), list);
		return list;
	}

	@Override
	public void write(InverseAttribute attribute, Record r, Object value) {
		r.setAttributeValue(attribute.getName(), value);
	}
}
