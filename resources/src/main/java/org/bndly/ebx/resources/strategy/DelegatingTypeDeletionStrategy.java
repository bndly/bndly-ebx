package org.bndly.ebx.resources.strategy;

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

import org.bndly.schema.api.DeletionStrategy;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.Type;
import java.util.HashMap;
import java.util.Map;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = {DelegatingTypeDeletionStrategy.class, DeletionStrategy.class}, immediate=true)
public class DelegatingTypeDeletionStrategy implements DeletionStrategy {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	private final Map<String, DeletionStrategy> strategiesByTypeName = new HashMap<>();

	@Override
	public void delete(Record record, Transaction transaction) {
		DeletionStrategy s = assertStrategyExists(record);
		s.delete(record, transaction);
	}

	public void registerStrategyForType(DeletionStrategy deletionStrategy, Type type) {
		strategiesByTypeName.put(type.getName(), deletionStrategy);
	}

	public void unregisterStrategyForType(DeletionStrategy deletionStrategy, Type type) {
		strategiesByTypeName.remove(type.getName());
	}

	private DeletionStrategy assertStrategyExists(Record record) {
		return assertStrategyExists(record.getType());
	}

	private DeletionStrategy assertStrategyExists(Type type) {
		return assertStrategyExists(type.getName());
	}

	private DeletionStrategy assertStrategyExists(String typeName) {
		if (!strategiesByTypeName.containsKey(typeName)) {
			strategiesByTypeName.put(typeName, schemaBeanFactory.getEngine().getAccessor());
		}
		return strategiesByTypeName.get(typeName);
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

}
