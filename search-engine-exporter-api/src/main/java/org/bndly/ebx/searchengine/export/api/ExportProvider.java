/*
 * Copyright (c) 2013, cyber:con GmbH, Bonn.
 *
 * All rights reserved. This source file is provided to you for
 * documentation purposes only. No part of this file may be
 * reproduced or copied in any form without the written
 * permission of cyber:con GmbH. No liability can be accepted
 * for errors in the program or in the documentation or for damages
 * which arise through using the program. If an error is discovered,
 * cyber:con GmbH will endeavour to correct it as quickly as possible.
 * The use of the program occurs exclusively under the conditions
 * of the licence contract with cyber:con GmbH.
 */

package org.bndly.ebx.searchengine.export.api;

/*-
 * #%L
 * org.bndly.ebx.search-engine-exporter-api
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

import java.io.Writer;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: borismatosevic
 * Date: 15.05.13
 * Time: 09:05
 * To change this template use File | Settings | File Templates.
 */
public interface ExportProvider {
    public void registerExportOutputTransformer(ExportOutputTransformer.Factory transformer);
    public void registerExporterByEntity(Class<? extends ExportEntity> registerForEntity, Exporter exporter);
    public void registerExportMapperByEntity(Class<? extends ExportEntity> registerForEntity, ExportMapper exportMapper);
    public void registerExportEntityCollectorByEntity(Class<? extends ExportEntity> registerForEntity, ExportEntityCollector exportEntityCollector);
    
    public void unregisterExportOutputTransformer(ExportOutputTransformer.Factory transformer);
	public void unregisterExporterByEntity(Class<? extends ExportEntity> registerForEntity);
    public void unregisterExportMapperByEntity(Class<? extends ExportEntity> registerForEntity);
    public void unregisterExportEntityCollectorByEntity(Class<? extends ExportEntity> registerForEntity);
    
	public ExportOutputTransformer getExportOutputTransformer(ExportFormat exportFormat, Writer writer);
	public <E extends ExportEntity> ExportMapper<E> getExportMapperByEntity(Class<E> registeredEntity);
	public <E extends ExportEntity> ExportEntityCollector<E> getExportEntityCollectorByEntity(Class<E> registeredEntity);
	
	public List<Exporter> getAvailableExporters();
	public Exporter getExporterByName(String exporterName);
}
