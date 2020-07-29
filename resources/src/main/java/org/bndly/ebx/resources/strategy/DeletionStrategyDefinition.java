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

import org.bndly.ebx.model.Cart;
import org.bndly.ebx.model.Person;
import org.bndly.ebx.model.Product;
import org.bndly.ebx.model.ShipmentOfferRequest;
import org.bndly.schema.api.AttributeMediator;
import org.bndly.schema.api.DeletionStrategy;
import org.bndly.schema.api.MediatorRegistry;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.api.db.AttributeColumn;
import org.bndly.schema.api.db.TypeTable;
import org.bndly.schema.api.listener.SchemaDeploymentListener;
import org.bndly.schema.api.query.Query;
import org.bndly.schema.api.query.QueryContext;
import org.bndly.schema.api.services.Engine;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.Attribute;
import org.bndly.schema.model.Mixin;
import org.bndly.schema.model.NamedAttributeHolder;
import org.bndly.schema.model.NamedAttributeHolderAttribute;
import org.bndly.schema.model.Schema;
import org.bndly.schema.model.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(service = DeletionStrategyDefinition.class, immediate = true)
public class DeletionStrategyDefinition implements SchemaDeploymentListener {

	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;

	@Reference
	private DelegatingTypeDeletionStrategy delegatingTypeDeletionStrategy;
	private MediatorRegistry mediatorRegistry;
	private Engine engine;
	private Map<String, List<AttributeAssociation>> attributeReferences;
	private final List<RegisteredStrategy> registeredStrategies = new ArrayList<>();

	private static class RegisteredStrategy {

		private final Type type;
		private final DeletionStrategy deletionStrategy;

		public RegisteredStrategy(Type type, DeletionStrategy deletionStrategy) {
			this.type = type;
			this.deletionStrategy = deletionStrategy;
		}

		public DeletionStrategy getDeletionStrategy() {
			return deletionStrategy;
		}

		public Type getType() {
			return type;
		}

	}

	@Activate
	public void activate() {
		engine = schemaBeanFactory.getEngine();
		mediatorRegistry = engine.getMediatorRegistry();
	}

	@Deactivate
	public void deactivate() {
		for (RegisteredStrategy registeredStrategy : registeredStrategies) {
			delegatingTypeDeletionStrategy.unregisterStrategyForType(registeredStrategy.getDeletionStrategy(), registeredStrategy.getType());
		}
		registeredStrategies.clear();
		mediatorRegistry = null;
		engine = null;
	}

	@Override
	public void schemaDeployed(Schema deployedSchema, final Engine engine) {
		if (!"ebx".equals(deployedSchema.getName())) {
			return;
		}

		attributeReferences = new HashMap<>();
		index(deployedSchema.getTypes());
		index(deployedSchema.getMixins());

		List<Type> types = deployedSchema.getTypes();
		for (Type type : types) {
			if (Product.class.getSimpleName().equals(type.getName())) {
				List<AttributeAssociation> referencedBy = collectAttributeReferencesForType(type);
				DeletionStrategy s = buildCascadingDeletionStrategy(type, referencedBy);
				delegatingTypeDeletionStrategy.registerStrategyForType(s, type);
				registeredStrategies.add(new RegisteredStrategy(type, s));

			} else if (Person.class.getSimpleName().equals(type.getName())) {
				PersonDeletionStrategy s = new PersonDeletionStrategy();
				s.setDelegatingTypeDeletionStrategy(delegatingTypeDeletionStrategy);
				s.setEngine(engine);
				s.setReferencedBy(collectAttributeReferencesForType(type));
				delegatingTypeDeletionStrategy.registerStrategyForType(s, type);
				registeredStrategies.add(new RegisteredStrategy(type, s));

			} else if (Cart.class.getSimpleName().equals(type.getName())) {
				final TypeTable t = engine.getTableRegistry().getTypeTableByType(ShipmentOfferRequest.class.getSimpleName());
				AttributeColumn tmp = null;
				for (AttributeColumn attributeColumn : t.getColumns()) {
					if (attributeColumn.getAttribute().getName().equals("cart")) {
						tmp = attributeColumn;
						break;
					}
				}
				final AttributeColumn col = tmp;
				DeletionStrategy s = new DeletionStrategy() {
					@Override
					public void delete(Record record, Transaction transaction) {
						QueryContext qc = engine.getQueryContextFactory().buildQueryContext();
						Attribute att = col.getAttribute();
						AttributeMediator<Attribute> m = mediatorRegistry.getMediatorForAttribute(att);
						qc.update().table(t.getTableName()).setNull(col.getColumnName(), m.columnSqlType(att))
								.where().expression().and().criteria()
								.field(col.getColumnName()).equal().value(m.createPreparedStatementValueProvider(att, record.getId()));
						Query q = qc.build(record.getContext());
						transaction.getQueryRunner().run(q);
						engine.getAccessor().delete(record, transaction);
					}
				};
				delegatingTypeDeletionStrategy.registerStrategyForType(s, type);
				registeredStrategies.add(new RegisteredStrategy(type, s));
			}
		}
	}

	@Override
	public void schemaUndeployed(Schema deployedSchema, Engine engine) {
		if (!"ebx".equals(deployedSchema.getName())) {
			return;
		}
		for (RegisteredStrategy registeredStrategy : registeredStrategies) {
			delegatingTypeDeletionStrategy.unregisterStrategyForType(registeredStrategy.getDeletionStrategy(), registeredStrategy.getType());
		}
		registeredStrategies.clear();
	}

	private CascadingDeletionStrategy buildCascadingDeletionStrategy(Type type, List<AttributeAssociation> referencedBy) {
		CascadingDeletionStrategy strategy = new CascadingDeletionStrategy(type, referencedBy);
		strategy.setEngine(engine);
		strategy.setDelegatingTypeDeletionStrategy(delegatingTypeDeletionStrategy);
		return strategy;
	}

	private NullDeletionStrategy buildNullDeletionStrategy(Type type, List<AttributeAssociation> referencedBy) {
		NullDeletionStrategy strategy = new NullDeletionStrategy(type, referencedBy);
		strategy.setEngine(engine);
		strategy.setMediatorRegistry(mediatorRegistry);
		return strategy;
	}

	private void index(List<? extends NamedAttributeHolder> attributeHolders) {
		for (NamedAttributeHolder h : attributeHolders) {
			List<Attribute> attributes = h.getAttributes();
			if (attributes != null) {
				for (Attribute attribute : attributes) {
					if (NamedAttributeHolderAttribute.class.isInstance(attribute)) {
						NamedAttributeHolderAttribute naha = NamedAttributeHolderAttribute.class.cast(attribute);
						if (Mixin.class.isInstance(h)) {
							Mixin m = (Mixin) h;
							if (m.getMixedInto() != null) {
								for (Type type : m.getMixedInto()) {
									addToIndex(naha, type);
								}
							}
						} else if (Type.class.isInstance(h)) {
							addToIndex(naha, (Type) h);
						}
					}
				}
			}
		}
	}

	private void addToIndex(NamedAttributeHolderAttribute naha, Type type) {
		NamedAttributeHolder holder = naha.getNamedAttributeHolder();
		List<AttributeAssociation> tmp = attributeReferences.get(holder.getName());
		if (tmp == null) {
			tmp = new ArrayList<>();
			attributeReferences.put(holder.getName(), tmp);
		}
		tmp.add(new AttributeAssociation(naha, type));
	}

	public void setDelegatingTypeDeletionStrategy(DelegatingTypeDeletionStrategy delegatingTypeDeletionStrategy) {
		this.delegatingTypeDeletionStrategy = delegatingTypeDeletionStrategy;
	}

	public void setSchemaDeploymentListeners(List schemaDeploymentListeners) {
		schemaDeploymentListeners.add(this);
	}

	public List<AttributeAssociation> collectAttributeReferencesForType(Type type) {
		List<AttributeAssociation> referencedBy = new ArrayList<>();
		Type t = type;
		while (t != null) {
			List<AttributeAssociation> l = attributeReferences.get(t.getName());
			if (l != null) {
				referencedBy.addAll(l);
			}
			t = t.getSuperType();
		}
		return referencedBy;
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

}
