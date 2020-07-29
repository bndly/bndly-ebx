package org.bndly.business.task;

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

import org.bndly.business.api.Task;
import org.bndly.business.api.TaskDelegationRegistry;
import org.bndly.common.bpm.annotation.ProcessVariable;
import org.bndly.common.bpm.api.TaskExecutor;
import org.bndly.common.reflection.FieldBeanPropertyAccessor;
import org.bndly.common.reflection.ReflectionUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.el.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IOCContainerAwareTaskImpl implements Task {
	private static final Logger LOG = LoggerFactory.getLogger(IOCContainerAwareTaskImpl.class);
	private boolean didInit;
	private static final FieldBeanPropertyAccessor ACCESSOR = new FieldBeanPropertyAccessor();
	private final List<ExecutorVariableInitializer> initializers = new ArrayList<>();
	
	public static interface ExecutorVariableInitializer {
		void initVariables(Map<String, Object> variables, VariableScope variableScope);
	}
	
	public void assertInit() {
		if(!didInit) {
			List<Field> fields = ReflectionUtil.getFieldsWithAnnotation(ProcessVariable.class, this);
			if(fields != null) {
				for (Field field : fields) {
					if(!field.getType().equals(Expression.class)) {
						LOG.warn("skipping forwarding of a field to executor, because it is not declared as an "+Expression.class.getName());
						continue;
					}
					ProcessVariable pv = field.getAnnotation(ProcessVariable.class);
					ProcessVariable.Access access = pv.value();
					if(access == ProcessVariable.Access.READ_WRITE || access == ProcessVariable.Access.WRITE) {
						final String variableName = field.getName();
						final Expression value = (Expression) ACCESSOR.get(variableName, this);
						if(value != null) {
							initializers.add(new ExecutorVariableInitializer() {

								@Override
								public void initVariables(Map<String, Object> variables, VariableScope variableScope) {
									Object val = value.getValue(variableScope);
									if(val != null) {
										variables.put(variableName, val);
									}
								}
							});
						}
					}
				}
			}
			didInit = true;
		}
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		assertInit();
		final boolean debug = LOG.isDebugEnabled();
		long s = -1;
		if(debug) {
			s = System.currentTimeMillis();
		}
		Class<? extends TaskExecutor> executorClass = getExecutorClass();
		TaskExecutor executor = null;
		if (TaskDelegationRegistry.INSTANCE != null) {
			// if the executor is a defined spring bean, use the spring bean
			executor = TaskDelegationRegistry.INSTANCE.getTaskExecutorByName(executorClass.getName());
		}
		if (executor == null) {
			// if the executor is not known to spring, this will not be too bad, because we will just create our own instance
			executor = executorClass.newInstance();
		}
		ExecutorInitializer executorInitializer = Activator.getInstance();
		if(executorInitializer != null) {
			executorInitializer.initialize(executor);
		}

		// fill the executor statefull process variables with their respective values
		Map<String, Object> vars = new HashMap<>(execution.getVariables());
		for (ExecutorVariableInitializer initializer : initializers) {
			initializer.initVariables(vars, execution);
		}
		fillFieldsOfExecutorInExecution(executor, vars);

		// finally run the executor
		executor.run(); // i don't pass in the execution object, because i want to interact with it in a more abstract manner

		writeFieldOfExecutorToProcess(execution, executor);
		if(debug) {
			long e = System.currentTimeMillis();
			long d = e - s;
			LOG.debug("execution of bpm task {} took {}ms", executor.getClass().getSimpleName(), d);
		}
	}

	private void writeFieldOfExecutorToProcess(DelegateExecution execution, TaskExecutor executor) {
		Class<? extends TaskExecutor> clazz = executor.getClass();
		writeFieldOfExecutorToProcess(execution, executor, clazz);
	}

	private void writeFieldOfExecutorToProcess(DelegateExecution execution, TaskExecutor executor, Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			ProcessVariable pv = field.getAnnotation(ProcessVariable.class);
			if (pv != null) {
				if (pv.value().equals(ProcessVariable.Access.WRITE) || pv.value().equals(ProcessVariable.Access.READ_WRITE)) {
					String fieldName = field.getName();
					Object variableValueInExecutor = null;
					boolean ia = field.isAccessible();
					try {
						field.setAccessible(true);
						variableValueInExecutor = field.get(executor);
					} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
						throw new RuntimeException("could not get field value of TaskExecutor. field: " + fieldName + "(" + field.getType().getName() + ")", e);
					} finally {
						field.setAccessible(ia);
					}
					execution.setVariable(fieldName, variableValueInExecutor);
				}
			}
		}
		clazz = clazz.getSuperclass();
		if (clazz != null) {
			writeFieldOfExecutorToProcess(execution, executor, clazz);
		}
	}

	private void fillFieldsOfExecutorInExecution(TaskExecutor executor, Map<String, Object> vars) {
		Class<? extends TaskExecutor> clazz = executor.getClass();
		fillFieldsOfExecutorInExecution(executor, vars, clazz);
	}

	private void fillFieldsOfExecutorInExecution(TaskExecutor executor, Map<String, Object> vars, Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			ProcessVariable pv = field.getAnnotation(ProcessVariable.class);
			if (pv != null) {
				if (pv.value().equals(ProcessVariable.Access.READ) || pv.value().equals(ProcessVariable.Access.READ_WRITE)) {
					String fieldName = field.getName();
					Object variableValueInProcessInstance = vars.get(fieldName);
					if (variableValueInProcessInstance != null) {
						if (field.getType().isAssignableFrom(variableValueInProcessInstance.getClass())) {
							boolean ia = field.isAccessible();
							try {
								field.setAccessible(true);
								field.set(executor, variableValueInProcessInstance);
							} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
								StringBuffer sb = new StringBuffer();
								sb
									.append("could not set field value of TaskExecutor. field: ")
									.append(fieldName)
									.append("(")
									.append(field.getType().getName())
									.append(") value: ")
									.append(variableValueInProcessInstance)
									.append("(")
									.append(variableValueInProcessInstance.getClass().getName())
									.append(")");
								throw new RuntimeException(sb.toString(), e);
							} finally {
								field.setAccessible(ia);
							}
						} else {
							LOG.error("incompatible types. found {} for field {} of type {} in class {}", variableValueInProcessInstance.getClass(), field.getName(), field.getType(), field.getDeclaringClass().getName());
						}
					}
				}
			}
		}
		clazz = clazz.getSuperclass();
		if (clazz != null) {
			fillFieldsOfExecutorInExecution(executor, vars, clazz);
		}
	}
}
