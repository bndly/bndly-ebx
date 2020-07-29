package org.bndly.ebx.client.service.api;

/*-
 * #%L
 * org.bndly.ebx.client.generated-service-api
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


import org.bndly.common.service.model.api.StringList;
import org.bndly.ebx.model.WishList;
import org.bndly.rest.client.exception.ClientException;

import java.io.Serializable;
import java.util.List;

public interface CustomWishListService {

    static class WishListMailForm implements Serializable {

    private static final long serialVersionUID = -6208910947808465548L;

    private WishList wishList;
    private StringList recipients;
    private String mailText;
    private String wishListLink;
    private Boolean affirmation;

    public WishListMailForm() {
    }

    public WishListMailForm(WishList wishList) {
        this.wishList = wishList;
    }

    public WishList getWishList() {
        return wishList;
    }

    public void setWishList(WishList wishList) {
        this.wishList = wishList;
    }

    public StringList getRecipients() {
        return recipients;
    }

    public void setRecipients(StringList recipients) {
        this.recipients = recipients;
    }

    public String getMailText() {
        return mailText;
    }

    public void setMailText(String mailText) {
        this.mailText = mailText;
    }

    public String getWishListLink() {
        return wishListLink;
    }

    public void setWishListLink(String wishListLink) {
        this.wishListLink = wishListLink;
    }

    public Boolean getAffirmation() {
        return affirmation;
    }

    public void setAffirmation(Boolean affirmation) {
        this.affirmation = affirmation;
    }
}
	
    List<WishList> findWishListsByPersonId(Long personID) throws ClientException;

    WishList readWishListByPersonIdAndWishListId(long personId, long wishListId) throws ClientException;
    
    WishList readWishListBySecurityToken(String token) throws ClientException;
    
    WishList readWishListById(long id) throws ClientException;

    boolean deleteWishListByPersonIdAndWishListId(long personId, long wishListId) throws ClientException;
    
    List<WishList> findWishListsOfCurrentUser() throws ClientException;
    
    void sendMail(WishListMailForm mail) throws ClientException;

    void delete(long id) throws ClientException;

    WishListMailForm readMailTemplate(WishList model) throws ClientException;
    
    WishListMailForm readMailTemplateByWishListId(long id) throws ClientException;
}
