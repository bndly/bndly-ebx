package org.bndly.ebx.profiler;

/*-
 * #%L
 * org.bndly.ebx.profiler
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

import org.bndly.common.osgi.util.DictionaryAdapter;
import org.bndly.common.osgi.util.ServiceRegistrationBuilder;
import org.bndly.ebx.model.ResourceMethodInvocationStatus;
import org.bndly.rest.api.Context;
import org.bndly.rest.api.NoOpResourceInterceptor;
import org.bndly.rest.api.ResourceInterceptor;
import org.bndly.rest.api.ResourceURI;
import org.bndly.rest.beans.ebx.ResourceMethodInvocationStatusRestBean;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.Transaction;
import org.bndly.schema.beans.ActiveRecord;
import org.bndly.schema.beans.SchemaBeanFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {}, immediate = true)
@Designate(ocd = ResourceMethodInvocationCounterFilter.Configuration.class)
public class ResourceMethodInvocationCounterFilter extends NoOpResourceInterceptor implements ResourceInterceptor {

	private ServiceRegistration<ResourceInterceptor> registration;

	@ObjectClassDefinition(
			name = "REST API Profiler"
	)
	public @interface Configuration {
		@AttributeDefinition(
				name = "Enabled",
				description = "If set to true, the profiling will be enabled."
		)
		boolean enabled() default false;
		
		@AttributeDefinition(
				name = "Watch Period Length",
				description = "The length of a watch period in milli seconds. The longer the period, the more invocations will be kept in memory before they are flushed to the persistence system."
		)
		long watchPeriod() default 30000;
		
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(ResourceMethodInvocationCounterFilter.class);

	private class MethodInvocationStopWatch {

		private final StopWatch stopWatch;
		private final ResourceURI uri;

		public MethodInvocationStopWatch(StopWatch stopWatch, ResourceURI uri) {
			this.stopWatch = stopWatch;
			this.uri = uri;
		}

		public StopWatch getStopWatch() {
			return stopWatch;
		}

		public ResourceURI getUri() {
			return uri;
		}

	}

	public ResourceMethodInvocationCounterFilter() {
		current = new Date();
		nextFlush = new Date(current.getTime() + watchPeriod);
		stopWatches = new ThreadLocal<>();
		enabled = false;
	}

	private final ThreadLocal<MethodInvocationStopWatch> stopWatches;
	private ExecutorService executor;
	@Reference(target = "(service.pid=org.bndly.schema.beans.SchemaBeanFactory.ebx)")
	private SchemaBeanFactory schemaBeanFactory;
	private Map<String, Long> invocations = new HashMap<>();
	private Map<String, Double> durations = new HashMap<>();
	private Date current;
	private Date nextFlush;
	private Long watchPeriod = 30 * 1000L; // 30 seconds
	private Boolean enabled;
	
	@Activate
	public void activate(ComponentContext componentContext) {
		DictionaryAdapter adapter = new DictionaryAdapter(componentContext.getProperties()).emptyStringAsNull();
		watchPeriod = adapter.getLong("watchPeriod", 30 * 1000L);
		enabled = adapter.getBoolean("enabled", false);
		if (enabled) {
			executor = Executors.newSingleThreadExecutor();
			// register self
			registration = ServiceRegistrationBuilder.newInstance(ResourceInterceptor.class, this).pid(getClass().getName() + ".service").register(componentContext.getBundleContext());
		}
	}

	@Deactivate
	public void deactivate() {
		if (registration != null) {
			registration.unregister();
			registration = null;
		}
		if (executor != null) {
			executor.shutdown();
		}
	}
	
	@Override
	public void doFinally(Context context) {
		try {
			MethodInvocationStopWatch methodInvocationStopWatch = stopWatches.get();
			if(methodInvocationStopWatch == null) {
				return;
			}
			StopWatch sw = methodInvocationStopWatch.getStopWatch();
			sw.stop();
			long millis = sw.getTotalTimeMillis();
			ResourceURI uri = methodInvocationStopWatch.getUri();
			increment(uri, millis);
		} catch (Exception e) {
			LOG.error("error while profiling resource resolution: " + e.getMessage(), e);
		}
	}

	@Override
	public void beforeResourceResolving(Context context) {
		StopWatch sw = new StopWatch();
		stopWatches.set(new MethodInvocationStopWatch(sw, context.getURI()));
		sw.start();
	}

	private synchronized void increment(ResourceURI uri, long millis) {
		String uriString = uri.asString();
		Long lng = invocations.get(uriString);
		if (lng == null) {
			lng = 0L;
		}
		lng++;
		invocations.put(uriString, lng);

		Double d = durations.get(uriString);
		if (d == null) {
			d = (double) millis;
		} else {
			d = (d * (lng - 1) + millis) / lng;
		}
		durations.put(uriString, d);
		flushIfRequired();
	}

	public List<ResourceMethodInvocationStatusRestBean> getInvocationStatusList() {
		List<ResourceMethodInvocationStatusRestBean> list = new ArrayList<>();
		for (Map.Entry<String, Long> entry : invocations.entrySet()) {
			String uriAsString = entry.getKey();
			Long count = entry.getValue();
			ResourceMethodInvocationStatusRestBean b = new ResourceMethodInvocationStatusRestBean();
			b.setMethodName(uriAsString);
			b.setInvocationCount(count);
			list.add(b);
		}
		return list;
	}

	private void flushIfRequired() {
		if (new Date().getTime() > nextFlush.getTime()) {
			final Map<String, Long> currentInvocations = invocations;
			final Map<String, Double> currentDurations = durations;
			final Date start = new Date(current.getTime());
			final Date end = new Date(nextFlush.getTime());
			executor.submit(new Runnable() {
				@Override
				public void run() {
					Transaction tx = schemaBeanFactory.getEngine().getQueryRunner().createTransaction();
					try {
						for (Map.Entry<String, Double> entry : currentDurations.entrySet()) {
							String uriString = entry.getKey();
							Double duration = entry.getValue();
							Long invocation = currentInvocations.get(uriString);
							RecordContext context = schemaBeanFactory.getEngine().getAccessor().buildRecordContext();
							ResourceMethodInvocationStatus rm = schemaBeanFactory.getSchemaBean(ResourceMethodInvocationStatus.class, context.create(ResourceMethodInvocationStatus.class.getSimpleName()));
							rm.setMethodName(uriString);
							rm.setStartDate(start);
							rm.setEndDate(end);
							rm.setInvocationCount(invocation);
							rm.setAverageDuration(duration.longValue());
							((ActiveRecord) rm).persist(tx);
						}
						tx.commit();
					} catch (Exception e) {
						LOG.error("exception while trying to flush profiling data: {}", e.getMessage(), e);
					}
				}
			});
			invocations = new HashMap<>();
			durations = new HashMap<>();
			current = nextFlush;
			nextFlush = new Date(current.getTime() + watchPeriod);
		}
	}

	public void setSchemaBeanFactory(SchemaBeanFactory schemaBeanFactory) {
		this.schemaBeanFactory = schemaBeanFactory;
	}

}
