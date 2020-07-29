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
import org.bndly.common.bpm.annotation.ProcessVariable.Access;
import org.bndly.common.bpm.api.TaskExecutor;
import org.bndly.ebx.price.PriceUtil;
import org.bndly.ebx.model.Currency;
import org.bndly.ebx.model.Price;
import org.bndly.ebx.model.PriceRequest;
import org.bndly.ebx.model.Purchasable;
import org.bndly.ebx.model.UserPrice;
import org.bndly.ebx.model.ValueAddedTax;
import org.bndly.ebx.model.Variant;
import org.bndly.schema.api.Record;
import org.bndly.schema.beans.ActiveRecord;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;

public class WriteProductPriceToPriceRequestTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor {

    @ProcessVariable
    private PriceRequest priceRequest;
    @ProcessVariable(Access.READ)
    private Purchasable purchasableItem;
    @ProcessVariable(Access.READ)
    private BigDecimal discount;

    @Override
    public void run() {
        int precision = 5;
        long quantity = priceRequest.getQuantity() == null ? 1 : priceRequest.getQuantity();
        Purchasable pi = purchasableItem;
        if (Variant.class.isInstance(purchasableItem)) {
            Variant v = (Variant) purchasableItem;
            pi = schemaBeanFactory.getEclipseChainWithNullSkipping(Purchasable.class, v, v.getProduct());
        }

        Currency currency = null;
        BigDecimal p = null;
        BigDecimal pGross = null;
        ValueAddedTax vat = null;

        List<Price> prices = PriceUtil.getValidPrices(pi.getPriceModel());
        Price price = PriceUtil.getLowestPrice(prices, quantity);
        if(price != null) {
            currency = schemaBeanFactory.convertToActiveRecord(price.getCurrency());
            vat = schemaBeanFactory.convertToActiveRecord(price.getTaxModel());
			if(vat == null) {
				// lookup the VAT via the rate
				BigDecimal rate = price.getTaxModel().getValue();
				if(rate != null) {
					Iterator<Record> res = schemaBeanFactory.getEngine().getAccessor().query("PICK "+ValueAddedTax.class.getSimpleName()+" v IF v.value=? LIMIT ?", rate, 1);
					if(res.hasNext()) {
						vat = schemaBeanFactory.getSchemaBean(ValueAddedTax.class, res.next());
					}
				}
			}
            p = PriceUtil.getNetValueFromPrice(price, quantity);
            pGross = p;
        }

        if (currency != null && currency.getDecimalPlaces() != null) {
            precision = (int) (long) currency.getDecimalPlaces();
        }
		if(ActiveRecord.class.isInstance(currency) && currency != null) {
			Long id = ((ActiveRecord)currency).getId();
			if(id == null) {
				Iterator<Record> res = schemaBeanFactory.getEngine().getAccessor().query("PICK "+Currency.class.getSimpleName()+" c IF c.code=? LIMIT ?", currency.getCode(), 1);
				if(res.hasNext()) {
					currency = schemaBeanFactory.getSchemaBean(Currency.class, res.next());
				}
			}
		}
        priceRequest.setCurrency(currency);

        if (vat != null && p != null) {
            BigDecimal rate = vat.getValue();
            if (rate != null) {
                rate = new BigDecimal("100").add(rate);
                rate = rate.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                pGross = p.multiply(rate);
            }
        }

        if (discount == null) {
            discount = BigDecimal.ZERO;
        }

        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(discount.setScale(precision, RoundingMode.HALF_UP));

        UserPrice up = jsonSchemaBeanFactory.getSchemaBean(UserPrice.class);
        up.setTaxModel(vat);
        up.setCurrency(currency);
        if (pGross != null) {
            up.setGrossValue(pGross.setScale(precision, RoundingMode.HALF_UP));
            up.setDiscountedGrossValue(up.getGrossValue().multiply(discountMultiplier).setScale(precision, RoundingMode.HALF_UP));
        }

        if (p != null) {
            up.setNetValue(p.setScale(precision, RoundingMode.HALF_UP));
            up.setDiscountedNetValue(up.getNetValue().multiply(discountMultiplier).setScale(precision, RoundingMode.HALF_UP));
        }
        priceRequest.setPrice(up);
        ((ActiveRecord) (priceRequest)).update();
    }

}
