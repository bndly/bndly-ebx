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

import org.bndly.ebx.model.QuantifiedItem;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.VirtualAttributeAdapter;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.bndly.schema.model.DecimalAttribute;
import org.bndly.schema.model.NamedAttributeHolder;
import java.math.BigDecimal;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public abstract class AbstractMerchandiseValueGrossAdapter<C, I> implements VirtualAttributeAdapter<DecimalAttribute> {
	
	protected static final String ATTRIBUTE_MERCHANDISE_VALUE = "merchandiseValue";
	protected static final String ATTRIBUTE_MERCHANDISE_VALUE_GROSS = "merchandiseValueGross";
	
	protected abstract SchemaBeanFactory getSchemaBeanFactory();
	protected abstract Iterable<I> getMerchandiseItemsFromContainer(C container);
	protected abstract PriceCallback<I> getPriceCallback(DecimalAttribute attribute, Record record);
	
	private final String supportedEntityTypeName;

	public AbstractMerchandiseValueGrossAdapter(String supportedEntityTypeName) {
		this.supportedEntityTypeName = supportedEntityTypeName;
	}
	
	@Override
	public final boolean supports(DecimalAttribute attribute, NamedAttributeHolder holder) {
		if (!attribute.getName().equals(ATTRIBUTE_MERCHANDISE_VALUE_GROSS) && !attribute.getName().equals(ATTRIBUTE_MERCHANDISE_VALUE)) {
			return false;
		}
		return SchemaInspectionUtil.isAssignableTo(holder, supportedEntityTypeName);
	}

	@Override
	public final Object read(final DecimalAttribute attribute, Record r) {
		C container = (C) getSchemaBeanFactory().getSchemaBean(r);
		Iterable<I> merchandiseItems = getMerchandiseItemsFromContainer(container);
		return getPriceGross(merchandiseItems, getPriceCallback(attribute, r));
	}

	@Override
	public final void write(DecimalAttribute attribute, Record r, Object value) {
	}
	
	protected final <E> BigDecimal getPriceGross(Iterable<E> items, PriceCallback<E> cb) {
		BigDecimal v = BigDecimal.ZERO;
		if (items != null) {
			for (E item : items) {
				Long quantity = null;
				if (QuantifiedItem.class.isInstance(item)) {
					quantity = ((QuantifiedItem) item).getQuantifiedItemQuantity();
				}
				if (quantity == null) {
					quantity = 1L;
				}
				BigDecimal pg = cb.getPrice(item);
				if (pg != null) {
					v = v.add(new BigDecimal(quantity).multiply(pg));
				}
			}
		}
		return v;
	}

	public static interface PriceCallback<E> {

		BigDecimal getPrice(E item);
	}
}
