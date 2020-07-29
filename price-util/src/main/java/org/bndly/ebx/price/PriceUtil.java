package org.bndly.ebx.price;

/*-
 * #%L
 * org.bndly.ebx.price-util
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

import org.bndly.ebx.model.Country;
import org.bndly.ebx.model.Price;
import org.bndly.ebx.model.PriceConstraint;
import org.bndly.ebx.model.PriceModel;
import org.bndly.ebx.model.SimplePrice;
import org.bndly.ebx.model.StaggeredPrice;
import org.bndly.ebx.model.StaggeredPriceItem;
import org.bndly.ebx.model.UserPrice;
import org.bndly.ebx.price.impl.EndDatePriceConstraintEvaluator;
import org.bndly.ebx.price.impl.StartDatePriceConstraintEvaluator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PriceUtil {
    public static Price getLowestPrice(List<Price> prices, long quantity) {
        if (prices == null || prices.isEmpty()) {
            return null;
        }
        BigDecimal lowestValue = null;
        Price lowestPrice = null;
        for (Price price : prices) {
            BigDecimal v = getNetValueFromPrice(price, quantity);
            if (lowestPrice == null || (v != null && lowestValue.compareTo(v) > 0)) {
                lowestPrice = price;
                lowestValue = v;
            }
        }
        return lowestPrice;
    }

    public static BigDecimal getNetValueFromPrice(Price price, long quantity) {
        if (SimplePrice.class.isInstance(price)) {
            return SimplePrice.class.cast(price).getNetValue();
        } else if (UserPrice.class.isInstance(price)) {
            return UserPrice.class.cast(price).getDiscountedNetValue();
        } else if (StaggeredPrice.class.isInstance(price)) {
            StaggeredPrice sp = StaggeredPrice.class.cast(price);
            StaggeredPriceItem item = getStaggeredPriceItemForQuantity(quantity, sp);
            if (item == null) {
                return null;
            }
            return item.getNetValue();
        } else {
            throw new IllegalArgumentException("unsupported price type");
        }
    }

    public static StaggeredPriceItem getStaggeredPriceItemForQuantity(long quantity, StaggeredPrice sp) {
        List<StaggeredPriceItem> items = sp.getItems();
        if (items == null) {
            return null;
        }
        StaggeredPriceItem r = null;
        for (StaggeredPriceItem item : items) {
            if (quantityIsInBounds(quantity, item)) {
                r = item;
            }
        }
        return r;
    }

    public static StaggeredPriceItem getLowestStaggeredPriceItemForQuantity(long quantity, StaggeredPrice sp) {
        List<StaggeredPriceItem> items = sp.getItems();
        if (items == null || items.isEmpty()) {
            return null;
        }

        List<StaggeredPriceItem> priceItemsInBound = new ArrayList<>();

        for (StaggeredPriceItem item : items) {
            if (quantityIsInBounds(quantity, item)) {
                priceItemsInBound.add(item);
            }
        }

        if (priceItemsInBound.isEmpty()) {
            return null;
        }

        StaggeredPriceItem r = null;

        for (StaggeredPriceItem item : priceItemsInBound) {
            if (r == null) {
                r = item;
            } else if (item.getNetValue().compareTo(r.getNetValue()) < 0) {
                r = item;
            }
        }

        return r;
    }

    public static boolean quantityIsInBounds(long quantity, StaggeredPriceItem item) {
        Long minq = item.getMinQuantity();
        Long maxq = item.getMaxQuantity();
        boolean result = true;
        if (minq != null) {
            result = result && minq <= quantity;
        }
        if (maxq != null) {
            result = result && maxq >= quantity;
        }
        return result;
    }

    public static List<Price> getValidPrices(PriceModel priceModel) {
        if (priceModel == null) {
            return null;
        }
        List<Price> prices = priceModel.getPrices();
        if (prices == null) {
            return null;
        }
        List<Price> validPrices = new ArrayList<>();
        for (Price price : prices) {
            if (checkConstraints(price)) {
                validPrices.add(price);
            }
        }
        if (validPrices.isEmpty()) {
            return null;
        }
        return validPrices;
    }

    public static boolean checkConstraints(Price price) {
        List<PriceConstraint> constraints = price.getConstraints();
        if (constraints != null) {
            for (PriceConstraint priceConstraint : constraints) {
                if (!checkConstraint(priceConstraint)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkConstraint(PriceConstraint priceConstraint) {
        if (!checkStartDate(priceConstraint)) {
            return false;
        }
        if (!checkEndDate(priceConstraint)) {
            return false;
        }
        if (!checkCountry(priceConstraint)) {
            return false;
        }
        return true;
    }

    public static boolean checkCountry(PriceConstraint priceConstraint) {
        Country c = priceConstraint.getCountry();
        if (c != null) {
            // TODO: get a hold of the country that shall be checked against this price constraint
        }
        return true;
    }

    public static boolean checkStartDate(PriceConstraint priceConstraint) {
        return StartDatePriceConstraintEvaluator.checkStartDate(priceConstraint, new Date());
    }

    public static boolean checkEndDate(PriceConstraint priceConstraint) {
        return EndDatePriceConstraintEvaluator.checkEndDate(priceConstraint, new Date());
    }
}
