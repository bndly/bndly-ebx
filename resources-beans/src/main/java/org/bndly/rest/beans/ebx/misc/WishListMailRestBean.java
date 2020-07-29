package org.bndly.rest.beans.ebx.misc;

/*-
 * #%L
 * org.bndly.ebx.resources-beans
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

import org.bndly.rest.atomlink.api.annotation.ParentBean;
import org.bndly.rest.beans.ebx.WishListReferenceRestBean;
import org.bndly.rest.beans.ebx.WishListRestBean;
import org.bndly.rest.common.beans.RestBean;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "wishListMail")
@XmlAccessorType(XmlAccessType.NONE)
public class WishListMailRestBean extends RestBean {

    @XmlElements({
        @XmlElement(name = "wishList", type = WishListRestBean.class),
        @XmlElement(name = "wishListRef", type = WishListReferenceRestBean.class)
    })
    @ParentBean
    private WishListReferenceRestBean wishList;
    @XmlElement
    private String text;
    @XmlElement
    private String wishListLink;
    @XmlElement
    private List<String> receiver;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getReceivers() {
        return receiver;
    }

    public void setReceivers(List<String> receivers) {
        this.receiver = receivers;
    }

    public String getWishListLink() {
        return wishListLink;
    }

    public void setWishListLink(String wishListLink) {
        this.wishListLink = wishListLink;
    }

    public WishListReferenceRestBean getWishList() {
        return wishList;
    }

    public void setWishList(WishListReferenceRestBean wishList) {
        this.wishList = wishList;
    }

    /**
     * convenience method to add email receivers from a semicolon separated
     * string list
     *
     * @param semicolonSeparatedReceivers
     */
    public void setReceivers(String semicolonSeparatedReceivers) {
        if (semicolonSeparatedReceivers != null) {
            String[] allReceivers = semicolonSeparatedReceivers.split(";");
            for (String string : allReceivers) {
                addReceiver(string.trim());
            }
        }
    }

    /**
     * convenience method to add email receivers
     *
     * @param receiverMailAddress
     */
    public void addReceiver(String receiverMailAddress) {
        if (receiver == null) {
            receiver = new ArrayList<String>();
        }
        receiver.add(receiverMailAddress);
    }

    public void setParentResource(WishListReferenceRestBean parent) {
        setWishList(parent);
    }

    public WishListReferenceRestBean getParentResource() {
        return getWishList();
    }
}
