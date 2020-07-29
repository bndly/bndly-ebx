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

import org.bndly.ebx.model.Product;
import org.bndly.ebx.model.Purchasable;
import org.bndly.ebx.model.StockItem;
import org.bndly.ebx.model.Variant;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.listener.MergeListener;
import org.bndly.schema.api.listener.PersistListener;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(service = {PurchasableToStockItemListener.class, PersistListener.class, MergeListener.class}, immediate = true)
public class PurchasableToStockItemListener implements PersistListener, MergeListener {

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

	private boolean isPurchasable(Record record) {
		String tN = record.getType().getName();
		return Variant.class.getSimpleName().equals(tN) || Product.class.getSimpleName().equals(tN);
	}

	@Override
	public void onPersist(Record record) {
		if (isPurchasable(record)) {
			assertStockItemExistsForPurchasable(record);
		}
	}

	private void assertStockItemExistsForPurchasable(Record record) {
		Purchasable p = schemaBeanFactory.getSchemaBean(Purchasable.class, record);
		RecordContext context = record.getContext();
		if (p.getSku() != null) {
			Record stockItem = schemaBeanFactory.getEngine().getAccessor().queryByExample(StockItem.class.getSimpleName(), context).attribute("sku", p.getSku()).single();
			if (stockItem == null) {
				StockItem si = schemaBeanFactory.getSchemaBean(StockItem.class, context.create(StockItem.class.getSimpleName()));
				si.setSku(p.getSku());
				si.setStock(0L);
				((ActiveRecord) si).persist();
			}
		}
	}

	@Override
	public void onMerge(Record record) {
		if (isPurchasable(record)) {
			assertStockItemExistsForPurchasable(record);
		}
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}
}
