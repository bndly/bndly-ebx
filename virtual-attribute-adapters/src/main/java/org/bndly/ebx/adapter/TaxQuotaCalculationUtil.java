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

import org.bndly.ebx.model.Currency;
import org.bndly.ebx.model.QuantifiedItem;
import org.bndly.ebx.model.TaxQuota;
import org.bndly.ebx.model.TaxRelatedItem;
import org.bndly.ebx.model.TaxRelatedItemsContainer;
import org.bndly.ebx.model.TaxableItem;
import org.bndly.ebx.model.TaxedItem;
import org.bndly.ebx.model.ValueAddedTax;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = TaxQuotaCalculationUtil.class, immediate = true)
public class TaxQuotaCalculationUtil {

	private static final Logger LOG = LoggerFactory.getLogger(TaxQuotaCalculationUtil.class);
	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	private static final int SCALE = 5;
	private static final RoundingMode ROUNDMODE = RoundingMode.HALF_UP;

	public List<Currency> getUsedCurrencies(TaxRelatedItemsContainer container) {
		Map<String, Currency> currencies = new HashMap<>();
		List<Currency> used = new ArrayList<>();
		Collection<? extends TaxRelatedItem> items = container.getTaxableItems();
		if (items != null) {
			for (TaxRelatedItem item : items) {
				if (item == null) {
					continue;
				}
				Currency c = item.getTaxRelatedItemCurrency();
				if (currencies.get(c.getCode()) == null) {
					currencies.put(c.getCode(), c);
					used.add(c);
				}
			}
		}

		return used;
	}

	public BigDecimal getMerchandiseValue(TaxRelatedItemsContainer container) {
		BigDecimal merchandiseValue = new BigDecimal("0");

		Collection<? extends TaxRelatedItem> items = container.getTaxableItems();
		if (items != null) {
			for (TaxRelatedItem item : items) {
				if (item == null) {
					continue;
				}
				BigDecimal itemValue = getItemTotal(item);
				if (itemValue != null) {
					merchandiseValue = merchandiseValue.add(itemValue);
				}
			}
		}

		return merchandiseValue.stripTrailingZeros();
	}

	public BigDecimal getMerchandiseValueGross(TaxRelatedItemsContainer container) {
		BigDecimal merchandiseValueGross = new BigDecimal("0");

		Collection<? extends TaxRelatedItem> items = container.getTaxableItems();
		if (items != null) {
			for (TaxRelatedItem item : items) {
				if (item == null) {
					continue;
				}
				BigDecimal itemGross = getItemTotalGross(item);
				if (itemGross != null) {
					merchandiseValueGross = merchandiseValueGross.add(itemGross);
				}
			}
		}

		return merchandiseValueGross.stripTrailingZeros();
	}

	public BigDecimal getItemTotal(TaxRelatedItem item) {
		BigDecimal unitPrice = getSingleItemValue(item);
		BigDecimal quantity = BigDecimal.ONE;

		if (QuantifiedItem.class.isAssignableFrom(item.getClass())) {
			Long q = ((QuantifiedItem) item).getQuantifiedItemQuantity();
			if (q != null) {
				quantity = new BigDecimal(q);
			}
		}

		if (unitPrice == null || quantity == null) {
			return null;
		}

		BigDecimal total = unitPrice.multiply(quantity);

		total = total.setScale(SCALE, ROUNDMODE);
		total = total.stripTrailingZeros();

		return total;
	}

	public BigDecimal getSingleItemValueGross(TaxRelatedItem item) {
		if (TaxedItem.class.isAssignableFrom(item.getClass())) {
			return ((TaxedItem) item).getTaxedItemPriceGross();
		} else if (TaxableItem.class.isAssignableFrom(item.getClass())) {
			BigDecimal unitPrice = ((TaxableItem) item).getTaxableItemPriceNetto();
			BigDecimal taxRate = item.getTaxRelatedItemTaxRate();
			BigDecimal multiplier = taxRate.divide(new BigDecimal("100")).add(BigDecimal.ONE);
			BigDecimal unitPriceGross = unitPrice.multiply(multiplier);
			return unitPriceGross.setScale(SCALE, ROUNDMODE);
		} else {
			LOG.error(item.getClass().getSimpleName() + " did not implement either " + TaxedItem.class.getSimpleName() + " or " + TaxableItem.class.getSimpleName());
			return null;
		}
	}

	public BigDecimal getSingleItemValue(TaxRelatedItem item) {
		if (TaxableItem.class.isAssignableFrom(item.getClass())) {
			return ((TaxableItem) item).getTaxableItemPriceNetto();
		} else if (TaxedItem.class.isAssignableFrom(item.getClass())) {
			TaxedItem taxedItem = (TaxedItem) item;
			BigDecimal rate = item.getTaxRelatedItemTaxRate();
			rate = rate.add(new BigDecimal("100"));
			rate = rate.setScale(2);
			rate = rate.divide(new BigDecimal("100"), ROUNDMODE);
			return taxedItem.getTaxedItemPriceGross().divide(rate, ROUNDMODE);
		} else {
			LOG.error(item.getClass().getSimpleName() + " did not implement either " + TaxedItem.class.getSimpleName() + " or " + TaxableItem.class.getSimpleName());
			return null;
		}
	}

	public BigDecimal getItemTotalGross(TaxRelatedItem item) {
		BigDecimal unitPrice = getSingleItemValueGross(item);
		BigDecimal quantity = BigDecimal.ONE;

		if (QuantifiedItem.class.isAssignableFrom(item.getClass())) {
			Long q = ((QuantifiedItem) item).getQuantifiedItemQuantity();
			if (q != null) {
				quantity = new BigDecimal(q);
			}
		}

		if (unitPrice == null || quantity == null) {
			return null;
		}

		BigDecimal total = unitPrice.multiply(quantity);

		total = total.setScale(SCALE, ROUNDMODE);
		total = total.stripTrailingZeros();

		return total;
	}

	public BigDecimal getItemTotalTax(TaxRelatedItem item) {
		BigDecimal tg = getItemTotalGross(item);
		BigDecimal t = getItemTotal(item);
		if (tg == null || t == null) {
			return null;
		}

		BigDecimal totalTax = tg.subtract(t);
		totalTax = totalTax.setScale(SCALE, ROUNDMODE);

		return totalTax.stripTrailingZeros();
	}

	/**
	 * TODO: this method does not respect currency conversions yet. hence the biggest number for tax quota will win.
	 *
	 * @param container the container of the taxable items
	 * @param context the record context in which the taxe related items container lives
	 * @return the tax rate that shall be used on side costs.
	 */
	public BigDecimal getSideCostsTaxRate(TaxRelatedItemsContainer container, RecordContext context) {
		Collection<TaxQuota> quotas = getTaxQuotasFor(container, context);
		BigDecimal highestTaxValue = null;

		BigDecimal highestValue = new BigDecimal(0);
		for (TaxQuota taxQuota : quotas) {
			BigDecimal v = taxQuota.getMerchandiseQuota();
			if (v == null) {
				LOG.error("taxRate " + taxQuota.getValueAddedTax() + " did not provide any value while calculating the side costs tax rate.");
				return null;
			}
			if (highestValue.compareTo(v) < 0) {
				highestValue = v;
				highestTaxValue = taxQuota.getValueAddedTax().getValue();
			}
		}

		return highestTaxValue;
	}

	public Collection<TaxQuota> getTaxQuotasFor(TaxRelatedItemsContainer container, RecordContext context) {
		// currency code -> vat rate -> tax quota
		Map<String, Map<BigDecimal, TaxQuota>> highestTaxValuesPerCurrency = new HashMap<>();
		Collection<? extends TaxRelatedItem> items = container.getTaxableItems();
		if (items != null && !items.isEmpty()) {
			List<TaxQuota> result = new ArrayList<>();
			List<Currency> currencies = getUsedCurrencies(container);
			for (Currency currency : currencies) {
				Map<BigDecimal, TaxQuota> highestTaxValues = highestTaxValuesPerCurrency.get(currency.getCode());
				if (highestTaxValues == null) {
					highestTaxValues = new HashMap<>();
					highestTaxValuesPerCurrency.put(currency.getCode(), highestTaxValues);
				}

				for (TaxRelatedItem item : items) {
					if (item.getTaxRelatedItemCurrency().getCode().equals(currency.getCode())) {
						BigDecimal taxRate = item.getTaxRelatedItemTaxRate();
						BigDecimal netto = getItemTotal(item);
						BigDecimal brutto = getItemTotalGross(item);

						if (taxRate == null) {
							LOG.error("taxRate for TaxRelatedItem " + item + " was null");
							continue;
						}
						if (netto == null) {
							LOG.error("netto for TaxRelatedItem " + item + " was null");
							continue;
						}
						if (brutto == null) {
							LOG.error("brutto for TaxRelatedItem " + item + " was null");
							continue;
						}

						BigDecimal taxesApplied = brutto.subtract(netto);

						TaxQuota quota = highestTaxValues.get(taxRate);
						if (quota == null) {
							quota = schemaBeanFactory.getSchemaBean(TaxQuota.class, context.create(TaxQuota.class.getSimpleName()));
							Iterator<Record> res = schemaBeanFactory.getEngine().getAccessor()
									.query("PICK " + ValueAddedTax.class.getSimpleName() + " v IF v.value=? LIMIT ?", context, null, taxRate, 1);
							if (res.hasNext()) {
								ValueAddedTax vat = schemaBeanFactory.getSchemaBean(ValueAddedTax.class, res.next());
								quota.setValueAddedTax(vat);
								quota.setQuota(taxesApplied);
								quota.setCurrency(currency);
								quota.setMerchandiseQuota(netto);
								highestTaxValues.put(taxRate, quota);
								result.add(quota);
							} else {
								LOG.error("could not find ValueAddedTax for rate of " + taxRate);
							}
						} else {
							BigDecimal q = quota.getQuota();
							if (q == null) {
								q = taxesApplied;
							} else {
								q = q.add(taxesApplied);
							}
							quota.setQuota(q);

							BigDecimal mq = quota.getMerchandiseQuota();
							if (mq == null) {
								mq = netto;
							} else {
								mq = mq.add(netto);
							}
							quota.setMerchandiseQuota(mq);
						}
					}
				}
			}
			return result;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

}
