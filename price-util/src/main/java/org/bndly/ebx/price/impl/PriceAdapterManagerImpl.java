package org.bndly.ebx.price.impl;

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

import org.bndly.common.lang.FilteringIterator;
import org.bndly.ebx.price.api.ComputedPrice;
import org.bndly.ebx.model.Price;
import org.bndly.ebx.model.PriceConstraint;
import org.bndly.ebx.model.PriceModel;
import org.bndly.ebx.price.api.PriceAdapter;
import org.bndly.ebx.price.api.PriceAdapterManager;
import org.bndly.ebx.price.api.PriceConstraintEvaluator;
import org.bndly.ebx.price.api.PriceContext;
import org.bndly.ebx.price.api.PriceDataInspector;
import org.bndly.ebx.price.exception.NoSuitablePriceAdapterAvailableException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = PriceAdapterManager.class)
public class PriceAdapterManagerImpl implements PriceAdapterManager {
	private static final Logger LOG = LoggerFactory.getLogger(PriceAdapterManagerImpl.class);
	
	private Map<String, PriceAdapter> priceAdapters;
	private final ReadWriteLock priceAdaptersLock = new ReentrantReadWriteLock();
	
	private List<PriceConstraintEvaluator> priceConstraintEvaluators;
	private final ReadWriteLock priceConstraintEvaluatorsLock = new ReentrantReadWriteLock();
	
	@Reference
	private PriceDataInspector priceDataInspector;

	@Reference(
			cardinality = ReferenceCardinality.MULTIPLE,
			service = PriceAdapter.class,
			bind = "bind",
			unbind = "unbind",
			policy = ReferencePolicy.DYNAMIC
	)
	protected void bind(PriceAdapter priceAdapter) {
		priceAdaptersLock.writeLock().lock();
		try {
			if (this.priceAdapters == null) {
				this.priceAdapters = new HashMap<>();
			}

			String key = priceAdapter.getApplicableSchemaType().getSimpleName();
			if (this.priceAdapters.containsKey(key)) {
				LOG.warn("overwriting price adapter for {}", key);
			}
			this.priceAdapters.put(key, priceAdapter);
		} finally {
			priceAdaptersLock.writeLock().unlock();
		}
	}

	protected void unbind(PriceAdapter priceAdapter) {
		priceAdaptersLock.writeLock().lock();
		try {
			String key = priceAdapter.getApplicableSchemaType().getSimpleName();
			PriceAdapter removed = this.priceAdapters.remove(key);
			if (removed != priceAdapter) {
				LOG.warn("failed to remove price adapter for {}, because the adapter instance was not identical", key);
				this.priceAdapters.put(key, removed);
			}
		} finally {
			priceAdaptersLock.writeLock().unlock();
		}
	}
	
	@Reference(
			cardinality = ReferenceCardinality.MULTIPLE,
			service = PriceConstraintEvaluator.class,
			bind = "bindPriceConstraintEvaluator",
			unbind = "unbindPriceConstraintEvaluator",
			policy = ReferencePolicy.DYNAMIC
	)
	protected void bindPriceConstraintEvaluator(PriceConstraintEvaluator priceConstraintEvaluator) {
		priceConstraintEvaluatorsLock.writeLock().lock();
		try {
			if (this.priceConstraintEvaluators == null) {
				this.priceConstraintEvaluators = new ArrayList<>();
			}

			this.priceConstraintEvaluators.add(priceConstraintEvaluator);
		} finally {
			priceConstraintEvaluatorsLock.writeLock().unlock();
		}
	}
	
	protected void unbindPriceConstraintEvaluator(PriceConstraintEvaluator priceConstraintEvaluator) {
		priceConstraintEvaluatorsLock.writeLock().lock();
		try {
			if (this.priceConstraintEvaluators != null) {
				Iterator<PriceConstraintEvaluator> iterator = this.priceConstraintEvaluators.iterator();
				while (iterator.hasNext()) {
					if (iterator.next() == priceConstraintEvaluator) {
						iterator.remove();
					}
				}
			}
		} finally {
			priceConstraintEvaluatorsLock.writeLock().unlock();
		}
	}

	@Override
	public ComputedPrice getPrice(Price price, PriceContext priceContext) throws NoSuitablePriceAdapterAvailableException {
		PriceAdapter priceAdapter;
		priceAdaptersLock.readLock().lock();
		try {
			if (priceAdapters == null) {
				throw new NoSuitablePriceAdapterAvailableException();
			}
			String key = priceDataInspector.getKey(price);
			priceAdapter = priceAdapters.get(key);
		} finally {
			priceAdaptersLock.readLock().unlock();
		}

		if (priceAdapter == null) {
			throw new NoSuitablePriceAdapterAvailableException();
		}

		return priceAdapter.getPrice(price, priceContext);

	}

	@Override
	public List<ComputedPrice> getPrices(PriceModel priceModel, PriceContext priceContext) throws NoSuitablePriceAdapterAvailableException {
		List<Price> prices = priceModel.getPrices();
		List<ComputedPrice> computedPrices = new ArrayList<>();


		for (Price price : prices) {
			computedPrices.add(getPrice(price, priceContext));
		}

		return computedPrices;
	}

	@Override
	public List<ComputedPrice> getValidPrices(PriceModel priceModel, PriceContext priceContext) throws NoSuitablePriceAdapterAvailableException {
		final List<Price> prices = priceModel.getPrices();
		if (prices == null || prices.isEmpty()) {
			return Collections.EMPTY_LIST;
		}

		return getValidPrices(prices, priceContext);
	}

	@Override
	public List<ComputedPrice> getValidPrices(Iterable<Price> prices, PriceContext priceContext) throws NoSuitablePriceAdapterAvailableException {
		priceConstraintEvaluatorsLock.readLock().lock();
		try {
			List<ComputedPrice> computedPrices = new ArrayList<>();
			Iterator<Price> it = getValidPricesIterator(prices, priceContext);
			while (it.hasNext()) {
				computedPrices.add(getPrice(it.next(), priceContext));

			}

			return computedPrices;
		} finally {
			priceConstraintEvaluatorsLock.readLock().unlock();
		}
	}
	
	private Iterator<Price> getValidPricesIterator(Iterable<Price> prices, final PriceContext priceContext) {
		return new FilteringIterator<Price>(prices.iterator()) {
			@Override
			protected boolean isAccepted(Price toCheck) {
				List<PriceConstraint> constraints = toCheck.getConstraints();
				if (constraints == null || constraints.isEmpty()) {
					return true;
				}
				if (priceConstraintEvaluators != null) {
					for (PriceConstraint constraint : constraints) {
						for (PriceConstraintEvaluator priceConstraintEvaluator : priceConstraintEvaluators) {
							if (!priceConstraintEvaluator.constraintApplies(toCheck, constraint, priceContext)) {
								return false;
							}
						}
					}
				}
				return true;
			}
			
		};
	}

	public void setPriceAdapters(Iterable<PriceAdapter> priceAdapters) {
		// in the Spring framework you may want to use a setter for the adapters
		priceAdaptersLock.writeLock().lock();
		try {
			if (this.priceAdapters != null) {
				this.priceAdapters.clear();
			}
			for (PriceAdapter priceAdapter : priceAdapters) {
				bind(priceAdapter);
			}
		} finally {
			priceAdaptersLock.writeLock().unlock();
		}
	}

	public void setPriceDataInspector(PriceDataInspector priceDataInspector) {
		this.priceDataInspector = priceDataInspector;
	}

	public void setPriceConstraintEvaluators(List<PriceConstraintEvaluator> priceConstraintEvaluators) {
		// in the Spring framework you may want to use a setter for the evaluators
		priceConstraintEvaluatorsLock.writeLock().lock();
		try {
			if (this.priceConstraintEvaluators != null) {
				this.priceConstraintEvaluators.clear();
			}
			for (PriceConstraintEvaluator priceConstraintEvaluator : priceConstraintEvaluators) {
				bindPriceConstraintEvaluator(priceConstraintEvaluator);
			}
		} finally {
			priceConstraintEvaluatorsLock.writeLock().unlock();
		}
	}
	
}
