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

import org.bndly.ebx.model.WishList;
import org.bndly.ebx.model.WishListPrivacy;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.listener.DeleteListener;
import org.bndly.schema.api.listener.MergeListener;
import org.bndly.schema.api.listener.PersistListener;
import org.bndly.schema.api.services.Accessor;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
@Component(service = {SearchQueryCustomizer.class, MergeListener.class, PersistListener.class, DeleteListener.class}, immediate = true)
public class WishListSearchQueryCustomizer extends AbstractRecordObserver implements SearchQueryCustomizer {
	
	private static final String WHISTLISTPRIVACYTYPENAME = WishListPrivacy.class.getSimpleName();
	
	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	private RecordContext recordContext;
	private List<Long> allowedPrivaciesInSearch;

	@Activate
	public void activate() {
		recordContext = schemaBeanFactory.getEngine().getAccessor().buildRecordContext();
		initWithQuery("PICK "+WHISTLISTPRIVACYTYPENAME+" w IF w.appearsInSearchIndex=?", true);
	}
	
	@Deactivate
	public void deactivate() {
		recordContext = null;
	}

	@Override
	public final RecordContext getRecordContext() {
		return recordContext;
	}

	@Override
	protected final Accessor getAccessor() {
		return schemaBeanFactory.getEngine().getAccessor();
	}
	
	
	@Override
	protected final boolean isRelevantRecord(Record record) {
		Type rt = record.getType();
		if(rt.getSchema() != schemaBeanFactory.getEngine().getDeployer().getDeployedSchema()) {
			return false;
		}
		if(!WHISTLISTPRIVACYTYPENAME.equals(rt.getName())) {
			return false;
		}
		return true;
	}

	@Override
	protected synchronized void contextHasChanged() {
		Iterator<Record> iter = getRecordContext().listPersistedRecordsOfType(WHISTLISTPRIVACYTYPENAME);
		List<Long> privacies = null;
		while (iter.hasNext()) {
			Record next = iter.next();
			WishListPrivacy wlp = schemaBeanFactory.getSchemaBean(WishListPrivacy.class, next);
			Boolean appearsInSearch = wlp.getAppearsInSearchIndex();
			if(appearsInSearch == null) {
				appearsInSearch = false;
			}
			if(appearsInSearch) {
				if(privacies == null) {
					privacies = new ArrayList<>();
				}
				privacies.add(next.getId());
			}
		}
		allowedPrivaciesInSearch = privacies;
	}

	@Override
	public String customizeQuery(String query, Type targetType) {
		if(WishList.class.getSimpleName().equals(targetType.getName())) {
			Iterator<Record> iter = getRecordContext().listPersistedRecordsOfType(WHISTLISTPRIVACYTYPENAME);
			if(allowedPrivaciesInSearch != null) {
				StringBuffer sb = null;
				for (Long privacyId : allowedPrivaciesInSearch) {
					if(sb == null) {
						sb = new StringBuffer("(");
					} else {
						sb.append(" OR ");
					}
					sb.append("WishList_privacy:").append(privacyId.toString());
				}
				if(sb != null) {
					sb.append(")");
					if(query == null) {
						query = sb.toString();
					} else {
						query += " AND "+sb.toString();
					}
				}
			}
		}
		
		return query;
	}
	
}
