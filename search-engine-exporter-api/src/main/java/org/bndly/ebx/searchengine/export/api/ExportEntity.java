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

import org.bndly.ebx.model.Price;

/**
 * Created by IntelliJ IDEA.
 * User: borismatosevic
 * Date: 02.05.13
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
public abstract class ExportEntity {

    private Long id;

    /***
        @see org.bndly.shop.model.product.AbstractProduct.sku
    ***/

    @Mandatory
    private String sku;

    @Mandatory
    private String gtin;

    @Mandatory
    protected String manufacturerArticleNumber;

    private String articleName;

    /***
    *  use 'sku' field
    *  @see org.bndly.shop.model.product.AbstractProduct.sku
    *
    * for PriceRequest 'sku' field to get
    *  @see org.bndly.shop.model.product.PriceRequest.priceGross
    *       or
    *  @see org.bndly.shop.model.product.PriceRequest.priceDiscountingGross
    *
    ***/
    @Mandatory
    private Price price;
	
    @Mandatory
    private String deepLink;

    @Mandatory(false)
    protected  String pricePerUnit; // 3.00 EUR / Kg, 3.00 EUR / Liter => product.quantityUnit.quantity + " / " + product.quantityUnit.abbreviation or maybe call buildIdentifier method

    @Mandatory(false)
    protected  String pictureLink;

    @Mandatory(false)
    protected  String shipmentPrice;

    @Mandatory(false)
    protected  String availability;

    @Mandatory(false)
    protected String deliveryTime;

    public final Long getId() {
        return id;
    }

    public final void setId(Long id) {
        this.id = id;
    }

    public final String getSku() {
        return sku;
    }

    public final void setSku(String sku) {
        this.sku = sku;
    }

    public final String getGtin() {
        return gtin;
    }

    public final void setGtin(String gtin) {
        this.gtin = gtin;
    }

    public final Price getPrice() {
        return price;
    }

    public final void setPrice(Price price) {
        this.price = price;
    }

    public final String getDeepLink() {
        return deepLink;
    }

    public final void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public final String getArticleName() {
        return articleName;
    }

    public final void setArticleName(String articleName) {
        this.articleName = articleName;
    }
}
