package org.bndly.ebx.resources.custom;

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

import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.listener.DeleteListener;
import org.bndly.schema.api.listener.MergeListener;
import org.bndly.schema.api.listener.PersistListener;
import org.bndly.schema.api.services.Accessor;
import java.util.Iterator;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public abstract class AbstractRecordObserver implements PersistListener, MergeListener, DeleteListener {

	protected abstract RecordContext getRecordContext();
	protected abstract Accessor getAccessor();
	
	protected boolean isRelevantRecord(Record record) {
		return false;
	}
	
	protected void newEntry(Record entry) {}
	protected void updatedEntry(Record newEntry, Record oldEntry) {}
	protected void removedEntry(Record entry) {}
	protected void contextHasChanged() {}
	
	public final void initWithQuery(String query, Object... args) {
		Iterator<Record> iter = getAccessor().query(query, getRecordContext(), null, args);
		while (iter.hasNext()) {
			Record next = iter.next();
			newEntry(next);
			contextHasChanged();
		}
	}
	
	@Override
	public final void onPersist(Record record) {
		if(isRelevantRecord(record)) {
			RecordContext ctx = getRecordContext();
			Record entry = ctx.create(record.getType(), record.getId());
			newEntry(entry);
		}
	}

	@Override
	public final void onMerge(Record record) {
		if(isRelevantRecord(record)) {
			RecordContext ctx = getRecordContext();
			Record oldEntry = ctx.get(record.getType(), record.getId());
			if(oldEntry != null) {
				ctx.detach(oldEntry);
			}
			Record newEntry = ctx.create(record.getType(), record.getId());
			updatedEntry(newEntry, oldEntry);
			contextHasChanged();
		}
	}

	@Override
	public final void onDelete(Record record) {
		if(isRelevantRecord(record)) {
			RecordContext ctx = getRecordContext();
			Record oldEntry = ctx.get(record.getType(), record.getId());
			if(oldEntry != null) {
				ctx.detach(oldEntry);
				removedEntry(oldEntry);
				contextHasChanged();
			}
		}
	}
	
	
}
