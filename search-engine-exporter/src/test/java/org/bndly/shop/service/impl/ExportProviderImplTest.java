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
package org.bndly.shop.service.impl;

/*-
 * #%L
 * org.bndly.ebx.search-engine-exporter
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

import org.bndly.common.converter.impl.ConverterRegistryImpl;
import org.bndly.ebx.model.Currency;
import org.bndly.ebx.model.PriceConstraint;
import org.bndly.ebx.model.UserPrice;
import org.bndly.ebx.model.ValueAddedTax;
import org.bndly.ebx.searchengine.export.api.ExportEntity;
import org.bndly.ebx.searchengine.export.api.ExportFormat;
import org.bndly.ebx.searchengine.export.api.ExportOutputTransformer;
import org.bndly.ebx.searchengine.export.api.ExportProvider;
import org.bndly.ebx.searchengine.export.api.ExportServiceException;
import org.bndly.ebx.searchengine.export.impl.geizhals.GeizhalsEntity;
import org.bndly.ebx.searchengine.export.impl.shared.FTPServerStoredTemplateISourceImpl;
import org.bndly.ebx.searchengine.export.impl.shared.DiscStoredTemplateISourceImpl;
//import org.bndly.ebx.searchengine.export.impl.shared.LegacyExportProviderImpl;
//import org.bndly.shop.model.Price;
//import org.bndly.shop.model.product.ExportEntity;
//import org.bndly.shop.service.api.Exporter;
//import org.bndly.shop.service.api.ExporterConfig;
import org.bndly.ebx.searchengine.export.impl.geizhals.GeizhalsExporterImpl;
import org.bndly.ebx.searchengine.export.impl.preisroboter.PreisRoboterEntity;
//import org.bndly.shop.model.product.Product;
//import org.bndly.shop.service.api.FTPConfig;
//import org.bndly.shop.service.api.ExportProvider;
import org.bndly.ebx.searchengine.export.impl.preisroboter.PreisRoboterExporterImpl;
import org.bndly.ebx.searchengine.export.impl.shared.CSVExportOutputTransformerFactory;
import org.bndly.ebx.searchengine.export.impl.shared.CSVFileExportOutputTransformer;
import org.bndly.ebx.searchengine.export.impl.shared.ExportProviderImpl;
//import org.apache.commons.net.ftp.FTPClient;
//import org.mockftpserver.fake.FakeFtpServer;
//import org.mockftpserver.fake.UserAccount;
//import org.mockftpserver.fake.filesystem.FileEntry;
//import org.mockftpserver.fake.filesystem.FileSystem;
//import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
//import org.supercsv.prefs.CsvPreference;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import java.io.StringWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

public class ExportProviderImplTest {

    private static final String HOME_DIR = "/";
    private static final String FILENAME = "sample.txt";
    private static final String FILE_WITH_PATH = "/dir/" + FILENAME;
    private static final String TEMPLATE = "/dir/sample_de_DE.txt";
    private static final String CONTENT = "${productComparison}";

//    @SpringBean(LegacyExportProviderImpl.NAME)
    ExportProviderImpl productExportProvider;

//    private FakeFtpServer fakeFtpServer;
//    private FTPConfig ftpConfig;

    List<PreisRoboterEntity> preisRoboterEntities = new ArrayList<>();
    List<GeizhalsEntity> geizhalsEntities = new ArrayList<>();

	private UserPrice createPrice(final BigDecimal netPrice) {
		final ValueAddedTax tax = new ValueAddedTax() {
			private BigDecimal val = new BigDecimal(19);

			@Override
			public String getDescription() {
				return "Standard 19%";
			}

			@Override
			public void setDescription(String description) {
			}

			@Override
			public String getName() {
				return "Standard";
			}

			@Override
			public void setName(String name) {
			}

			@Override
			public BigDecimal getValue() {
				return val;
			}

			@Override
			public void setValue(BigDecimal value) {
			}
		};
		final Currency currency = new Currency() {

			@Override
			public String getCode() {
				return "EUR";
			}

			@Override
			public void setCode(String code) {
			}

			@Override
			public String getSymbol() {
				return "EUR";
			}

			@Override
			public void setSymbol(String symbol) {
			}

			@Override
			public Long getDecimalPlaces() {
				return 2L;
			}

			@Override
			public void setDecimalPlaces(Long decimalPlaces) {
			}
		};
		UserPrice price = new UserPrice() {

			@Override
			public BigDecimal getNetValue() {
				return netPrice;
			}

			@Override
			public void setNetValue(BigDecimal netValue) {
			}

			@Override
			public BigDecimal getGrossValue() {
				return ((tax.getValue().add(BigDecimal.valueOf(100)))
						.multiply(netPrice))
						.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
			}

			@Override
			public void setGrossValue(BigDecimal grossValue) {
			}

			@Override
			public BigDecimal getDiscountedNetValue() {
				return getNetValue();
			}

			@Override
			public void setDiscountedNetValue(BigDecimal discountedNetValue) {
			}

			@Override
			public BigDecimal getDiscountedGrossValue() {
				return getGrossValue();
			}

			@Override
			public void setDiscountedGrossValue(BigDecimal discountedGrossValue) {
			}

			@Override
			public Currency getCurrency() {
				return currency;
			}

			@Override
			public void setCurrency(Currency currency) {
			}

			@Override
			public ValueAddedTax getTaxModel() {
				return tax;
			}

			@Override
			public void setTaxModel(ValueAddedTax taxModel) {
			}

			@Override
			public List<PriceConstraint> getConstraints() {
				return Collections.EMPTY_LIST;
			}

			@Override
			public void setConstraints(List<PriceConstraint> constraints) {
			}
		};
		return price;
	}
	
	private PreisRoboterEntity createPreisRoboterEntity(String sku, String gtin, String name, BigDecimal netPrice, String deepLink) {
		PreisRoboterEntity entity = new PreisRoboterEntity();
		entity.setSku(sku);
		entity.setGtin(gtin);
		entity.setArticleName(name);
		entity.setDeepLink(deepLink);
		entity.setPrice(createPrice(netPrice));
		return entity;
	}
	
	private GeizhalsEntity createGeizhalsEntity(String sku, String gtin, String name, BigDecimal netPrice, String deepLink) {
		GeizhalsEntity entity = new GeizhalsEntity();
		entity.setSku(sku);
		entity.setGtin(gtin);
		entity.setArticleName(name);
		entity.setDeepLink(deepLink);
		entity.setPrice(createPrice(netPrice));
		entity.setAvailability("sofort");
		entity.setShipmentPrice("6.90");
		return entity;
	}
	
    @BeforeTest
    public void before() {
		productExportProvider = new ExportProviderImpl();
		CSVExportOutputTransformerFactory csvTransformerFactory = new CSVExportOutputTransformerFactory();
		ConverterRegistryImpl converter = new ConverterRegistryImpl();
		converter.init();
		csvTransformerFactory.setConverterRegistry(converter);
		productExportProvider.registerExportOutputTransformer(csvTransformerFactory);
//        assertNull(fakeFtpServer);
        PreisRoboterEntity p1 = createPreisRoboterEntity("3000115GRN", "GTIN_OR_EAN_1", "Notebook HT-1011", new BigDecimal("1462.33"), "http://demoshop.bndly.de/external/product/3000115GRN");
        PreisRoboterEntity p2 = createPreisRoboterEntity("3000118GRN", "GTIN_OR_EAN_2" , "Notebook HT-1000", new BigDecimal("315.96"), "http://demoshop.bndly.de/external/product/3000118GRN");
        preisRoboterEntities.add(p1);
        preisRoboterEntities.add(p2);
        
		GeizhalsEntity g1 = createGeizhalsEntity("3000115GRN", "GTIN_OR_EAN_1", "Notebook HT-1011", new BigDecimal("1462.33"), "http://demoshop.bndly.de/external/product/3000115GRN");
		GeizhalsEntity g2 = createGeizhalsEntity("3000118GRN", "GTIN_OR_EAN_2" , "Notebook HT-1000", new BigDecimal("315.96"), "http://demoshop.bndly.de/external/product/3000118GRN");
        geizhalsEntities.add(g1);
        geizhalsEntities.add(g2);

//        fakeFtpServer = new FakeFtpServer();
//        fakeFtpServer.setServerControlPort(0);// use any free port
//
//        FileSystem fileSystem = new UnixFakeFileSystem();
//        fileSystem.add(new FileEntry(FILE_WITH_PATH, CONTENT));
//        fileSystem.add(new FileEntry(TEMPLATE, CONTENT));
//
//        fakeFtpServer.addUserAccount(new UserAccount(FTPConfig.USERNAME, FTPConfig.PASSWORD, HOME_DIR));
//
//        fakeFtpServer.setFileSystem(fileSystem);
//
//        fakeFtpServer.start();
//        int port = fakeFtpServer.getServerControlPort();
//
//        ftpConfig = new FTPConfig("localhost", port, FTPConfig.USERNAME, FTPConfig.PASSWORD);
    }

    @AfterTest
    public void after() {
//        assertNotNull(fakeFtpServer);
//        fakeFtpServer.stop();
//        fakeFtpServer = null;
        preisRoboterEntities.clear();
    }

	@Test
	public void testCSVHeaderOnPreisroboter() throws ExportServiceException, IOException {
		ExportFormat exportFormat = PreisRoboterExporterImpl.DEFAULT_FORMAT;
		StringWriter sw = new StringWriter();
		ExportOutputTransformer exportOutputTransformer = productExportProvider.getExportOutputTransformer(exportFormat, sw);
		assertNotNull(exportOutputTransformer);
		exportOutputTransformer.beforeEntities();
		exportOutputTransformer.afterEntities();
		sw.flush();
		String output = sw.toString();
		assertEquals(output, "");
	}
	
	@Test
	public void testCSVHeaderOnGeizhals() throws ExportServiceException, IOException {
		ExportFormat exportFormat = GeizhalsExporterImpl.DEFAULT_FORMAT;
		StringWriter sw = new StringWriter();
		ExportOutputTransformer exportOutputTransformer = productExportProvider.getExportOutputTransformer(exportFormat, sw);
		assertNotNull(exportOutputTransformer);
		exportOutputTransformer.beforeEntities();
		exportOutputTransformer.afterEntities();
		sw.flush();
		String output = sw.toString();
		assertEquals(output, "Artikel-Nr.|Artikelname|Preise|Versandk.|Verfügbarkeit.|EAN.|PZN.");
	}
	
	@Test
	public void testEntitiesOnPreisroboter() throws ExportServiceException, IOException {
		ExportFormat exportFormat = PreisRoboterExporterImpl.DEFAULT_FORMAT;
		StringWriter sw = new StringWriter();
		ExportOutputTransformer exportOutputTransformer = productExportProvider.getExportOutputTransformer(exportFormat, sw);
		assertNotNull(exportOutputTransformer);
		exportOutputTransformer.beforeEntities();
		for (ExportEntity productEntity : preisRoboterEntities) {
			exportOutputTransformer.dealWithExportEntity(productEntity);
		}
		exportOutputTransformer.afterEntities();
		sw.flush();
		String output = sw.toString();
		assertEquals(output, "3000115GRN,Notebook HT-1011,1740.17,http://demoshop.bndly.de/external/product/3000115GRN,,,,,GTIN_OR_EAN_1,,,,\n" +
"3000118GRN,Notebook HT-1000,375.99,http://demoshop.bndly.de/external/product/3000118GRN,,,,,GTIN_OR_EAN_2,,,,");
	}
	
	@Test
	public void testEntitiesOnGeizhals() throws ExportServiceException, IOException {
		ExportFormat exportFormat = GeizhalsExporterImpl.DEFAULT_FORMAT;
		StringWriter sw = new StringWriter();
		ExportOutputTransformer exportOutputTransformer = productExportProvider.getExportOutputTransformer(exportFormat, sw);
		assertNotNull(exportOutputTransformer);
		exportOutputTransformer.beforeEntities();
		for (ExportEntity productEntity : geizhalsEntities) {
			exportOutputTransformer.dealWithExportEntity(productEntity);
		}
		exportOutputTransformer.afterEntities();
		sw.flush();
		String output = sw.toString();
		assertEquals(output, "Artikel-Nr.|Artikelname|Preise|Versandk.|Verfügbarkeit.|EAN.|PZN.\n" +
"3000115GRN|Notebook HT-1011|1740.17|6.90|sofort|GTIN_OR_EAN_1|\n" +
"3000118GRN|Notebook HT-1000|375.99|6.90|sofort|GTIN_OR_EAN_2|");
	}
	
//     private String readFile(String filename) throws IOException {
//        FTPClient ftpClient = new FTPClient();
//
//        ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
//        ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
//
//        assertTrue(ftpClient.isConnected());
//        StringWriter outputStream = new StringWriter();
//        boolean success = ftpClient.retrieveFile(filename, outputStream);
//
//         ftpClient.disconnect();
//         if(!success) {
//             throw new IOException("Retrieve file failed: " + filename);
//         }
//
//         return outputStream.toString();
//    }
//
//    @Test
//    public void testReadFileFromFakeFtpServer() throws Exception {
//        String contents = readFile(FILE_WITH_PATH);
//        assertEquals(CONTENT, contents);
//    }
//
//    @Test(dependsOnMethods = {"testReadFileFromFakeFtpServer"})
//    public void testExportProviderImplTest() throws IOException {
//        assertNotNull(productExportProvider);
//        Exporter<PreisRoboterExporterImpl, Product> exporter =  productExportProvider.getExporter(PreisRoboterExporterImpl.class);
//        assertNotNull(exporter);
//    }
//
//    @Test(dependsOnMethods = {"testReadFileFromFakeFtpServer", "testExportProviderImplTest"})
//    public void testExportPreisRoboterEntityFTPStored() throws IOException {
//        Exporter<PreisRoboterExporterImpl, Product> exporter =  productExportProvider.getExporter(PreisRoboterExporterImpl.class);
//        FTPServerStoredTemplateISourceImpl ftpServerStored = new FTPServerStoredTemplateISourceImpl(ftpConfig);
//        ExporterConfig config = exporter.getExporterConfig();
//
//        config.setSourceFileWithPath(FILENAME + ".tmp");
//        config.setDestinationFileWithPath(FILE_WITH_PATH + ".stored");
//        config.setTemplateSource(ftpServerStored);
//
//        exporter.export();
//    }
//
//    @Test(dependsOnMethods = {"testReadFileFromFakeFtpServer", "testExportProviderImplTest", "testExportPreisRoboterEntityFTPStored"})
//    public void testExportPreisRoboterEntityDiscStoredDefaultFormat() throws IOException {
//        Exporter<PreisRoboterExporterImpl, Product> exporter =  productExportProvider.getExporter(PreisRoboterExporterImpl.class);
//        DiscStoredTemplateISourceImpl discStored = new DiscStoredTemplateISourceImpl();
//        ExporterConfig config = exporter.getExporterConfig();
//
//        config.setSourceFileWithPath(FILENAME + ".disc.default.tmp");
//        config.setDestinationFileWithPath(FILENAME + ".disc.default" + ".stored");
//        config.setTemplateSource(discStored);
//
//        exporter.export();
//    }
//
//    @Test(dependsOnMethods = {"testReadFileFromFakeFtpServer", "testExportProviderImplTest", "testExportPreisRoboterEntityFTPStored", "testExportPreisRoboterEntityDiscStoredDefaultFormat"})
//    public void testExportPreisRoboterEntityDiscStoredExcelFormatAndHeader() throws IOException {
//        Exporter<PreisRoboterExporterImpl, Product> exporter =  productExportProvider.getExporter(PreisRoboterExporterImpl.class);
//        DiscStoredTemplateISourceImpl discStored = new DiscStoredTemplateISourceImpl();
//        ExporterConfig config = exporter.getExporterConfig();
//        config.getExportFormat().setFormat(CsvPreference.STANDARD_PREFERENCE);
//
//        config.setSourceFileWithPath(FILENAME + ".disc.excel.tmp");
//        config.setDestinationFileWithPath(FILENAME + ".disc.excel" + ".stored");
//        config.setTemplateSource(discStored);
//        config.setWriteHeader(true);
//
//        exporter.export();
//    }
//
//     @Test(dependsOnMethods = {"testReadFileFromFakeFtpServer", "testExportProviderImplTest", "testExportPreisRoboterEntityFTPStored"})
//    public void testExportGeizhalsEntityDiscStoredDefaultFormat() throws IOException {
//        Exporter<GeizhalsExporterImpl, Product> exporter =  productExportProvider.getExporter(GeizhalsExporterImpl.class);
//        DiscStoredTemplateISourceImpl discStored = new DiscStoredTemplateISourceImpl();
//        ExporterConfig config = exporter.getExporterConfig();
//
//        config.setSourceFileWithPath(FILENAME + "geizhals.disc.default.tmp");
//        config.setDestinationFileWithPath(FILENAME + "geizhals.disc.default" + ".stored");
//        config.setTemplateSource(discStored);
//
//        exporter.export();
//    }
}
