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

import org.bndly.schema.model.NamedAttributeHolder;
import org.bndly.schema.model.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public final class SchemaInspectionUtil {

	private SchemaInspectionUtil() {
	}
	
	public static Iterable<Type> collectParentTypes(Type type) {
		if (type == null) {
			return Collections.EMPTY_LIST;
		}
		Type pt = type.getSuperType();
		if (pt == null) {
			return Collections.EMPTY_LIST;
		}
		List<Type> parentTypes = new ArrayList<>();
		while (pt != null) {
			parentTypes.add(pt);
			pt = pt.getSuperType();
		}
		return parentTypes;
	}
	
	public static Iterable<Type> collectSubTypes(Type type) {
		if (type == null) {
			return Collections.EMPTY_LIST;
		}
		List<Type> subTypes = type.getSubTypes();
		if (subTypes == null || subTypes.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		subTypes = new ArrayList<>();
		collectSubTypesInternal(type, subTypes);
		return subTypes;
	}
	
	private static void collectSubTypesInternal(Type type, List<Type> subTypes) {
		if (type == null) {
			return;
		}
		List<Type> tmp = type.getSubTypes();
		if (tmp == null || tmp.isEmpty()) {
			return;
		}
		for (Type st : tmp) {
			subTypes.add(st);
			collectSubTypesInternal(st, subTypes);
		}
	}
	
	/**
	 * This method returns true, if the inspected attribute holder can be assigned to a variable of the provided type/mixin name.
	 * @param inspectedTypeOrMixin the inspected type or mixin
	 * @param targetAttributeHolderName the name of the type or mixin to test against
	 * @return true, if the inspected attribute holder is a sub type or the exact type of the target attribute holder name
	 */
	public static boolean isAssignableTo(NamedAttributeHolder inspectedTypeOrMixin, String targetAttributeHolderName) {
		if (targetAttributeHolderName.equals(inspectedTypeOrMixin.getName())) {
			return true;
		}
		if (Type.class.isInstance(inspectedTypeOrMixin)) {
			for (Type parentType : collectParentTypes((Type) inspectedTypeOrMixin)) {
				if (targetAttributeHolderName.equals(parentType.getName())) {
					return true;
				}
			}
		}
		return false;
	}
}
