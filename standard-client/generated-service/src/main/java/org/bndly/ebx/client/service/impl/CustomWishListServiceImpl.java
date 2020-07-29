package org.bndly.ebx.client.service.impl;

/*-
 * #%L
 * org.bndly.ebx.client.generated-service
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

import org.bndly.common.service.model.api.StringList;
import org.bndly.common.service.shared.api.ProxyAware;
import org.bndly.ebx.client.service.api.UserIDProvider;
import org.bndly.ebx.model.Person;
import org.bndly.ebx.model.WishList;
import org.bndly.ebx.model.impl.PersonImpl;
import org.bndly.ebx.model.impl.WishListImpl;
import org.bndly.rest.beans.ebx.WishListReferenceRestBean;
import org.bndly.rest.beans.ebx.WishListRestBean;
import org.bndly.rest.beans.ebx.misc.WishListMailRestBean;
import org.bndly.ebx.client.service.api.CustomWishListService;
import org.bndly.ebx.client.service.api.CustomWishListService.WishListMailForm;
import org.bndly.ebx.client.service.api.WishListService;
import org.bndly.rest.client.exception.ClientException;
import org.bndly.rest.client.exception.UnknownResourceClientException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ben on 18/06/15.
 */
public class CustomWishListServiceImpl implements ProxyAware<WishListService>, CustomWishListService {

    private UserIDProvider userIDProvider;
//    private PersonService personService;
//    private WishListPrivacyService wishListPrivacyService;
    private WishListService thisProxy;

    @Override
    public void setThisProxy(WishListService serviceProxy) {
        thisProxy = serviceProxy;
    }

//    @Override
//    protected void preprocessModelForCreation(WishList model) {
//        if(model.getPerson() == null) {
//            if(userIDProvider.isLoggedIn()) {
//                model.setPerson(thisProxy.modelAsReferableResource(personService.assertCurrentUserExistsAsPerson()).buildReference());
//            } else {
//                throw new IllegalStateException("can not assert user existence as person while creating a wishlist, because the user was not logged in. hence no elastic social data would be available.");
//            }
//        } else {
//            // when a wishlist is persisted, persist the person only as a reference to not mess up addresses or payment details
//            Person person = personService.readByExternalUserId(model.getPerson().getExternalUserId());
//            if(person != null) {
//                model.setPerson(thisProxy.modelAsReferableResource(person).buildReference());
//            }
//        }
//
//        if(model.getSecurityToken() == null) {
//            // assert that the wishlist has a security token that does not exist while persisting.
//            WishList collidingWishList = model;
//            String uuid = null;
//            while(collidingWishList != null) {
//                uuid = UUID.randomUUID().toString();
//                collidingWishList = readWishListBySecurityToken(uuid);
//            }
//            model.setSecurityToken(uuid);
//        }
//        if(model.getPrivacy() == null) {
//            WishListPrivacy privacy = wishListPrivacyService.getDefault();
//            model.setPrivacy(thisProxy.modelAsReferableResource(privacy).buildReference());
//        }
//    }

    @Override
    public List<WishList> findWishListsByPersonId(Long personID) throws ClientException {
        WishList prototype = new WishListImpl();
        PersonImpl p = new PersonImpl();
        p.setId(personID);
        prototype.setPerson(p);
        return thisProxy.findAllLike(prototype, ArrayList.class);
    }

    @Override
    public WishList readWishListByPersonIdAndWishListId(long personId, long wishListId) throws ClientException {
        WishListImpl wl = new WishListImpl();
        PersonImpl p = new PersonImpl();
        p.setId(personId);
        wl.setPerson(p);
        wl.setId(wishListId);
		try {
			return thisProxy.find(wl);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public WishList readWishListBySecurityToken(String token) throws ClientException {
        WishList prototype = new WishListImpl();
        prototype.setSecurityToken(token);
		try {
			return thisProxy.find(prototype);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public WishList readWishListById(long id) throws ClientException {
        WishListImpl wl = new WishListImpl();
        wl.setId(id);
		try {
			return thisProxy.find(wl);
		} catch (UnknownResourceClientException e) {
			return null;
		}
    }

    @Override
    public boolean deleteWishListByPersonIdAndWishListId(long personId, long wishListId) {
        throw new IllegalStateException("do not use this method.");
    }

    @Override
    public List<WishList> findWishListsOfCurrentUser() throws ClientException {
        String userId = userIDProvider.getCurrentUserID();
        WishList prototype = new WishListImpl();
        Person p = new PersonImpl();
        p.setExternalUserId(userId);
        prototype.setPerson(p);
        return thisProxy.findAllLikeEagerly(prototype, ArrayList.class);
    }

    @Override
    public void sendMail(WishListMailForm mail) throws ClientException {
        WishListMailRestBean restBean = toWishListMailRestBean(mail);
        thisProxy.createClient(restBean.getWishList()).follow("sendMail").execute(restBean);
    }

    private WishListMailForm toWishListMailForm(WishListMailRestBean template, WishList wl) {
        WishListMailForm form = new WishListMailForm();
        form.setWishList(wl);
        List<String> receivers = template.getReceivers();
        StringList recipientsList = new StringList();
        if(receivers != null) {
            recipientsList.addAll(receivers);
        }
        form.setRecipients(recipientsList);
        form.setMailText(template.getText());
        form.setAffirmation(Boolean.FALSE);

        return form;
    }

    private WishListMailRestBean toWishListMailRestBean(WishListMailForm wishListMailForm) {
        WishListMailRestBean wishListMailRestBean = null;

        if (wishListMailForm != null) {
            wishListMailRestBean = new WishListMailRestBean();

            wishListMailRestBean.setText(wishListMailForm.getMailText());
            wishListMailRestBean.setReceivers(wishListMailForm.getRecipients());
            wishListMailRestBean.setWishListLink(wishListMailForm.getWishListLink());

            WishListReferenceRestBean parent = (WishListReferenceRestBean) thisProxy.toRestReferenceModel(wishListMailForm.getWishList());
            wishListMailRestBean.setParentResource(parent);
        }

        return wishListMailRestBean;
    }

    @Override
    public void delete(long id) throws ClientException {
        WishList wl = readWishListById(id);
        if(wl != null) {
            thisProxy.delete(wl);
        }
    }

    @Override
    public WishListMailForm readMailTemplate(WishList model) throws ClientException {
        WishList wl;
        if(getId(model) != null) {
            wl = readWishListById(getId(model));
        } else if(model.getSecurityToken() != null) {
            wl = readWishListBySecurityToken(model.getSecurityToken());
        } else {
            throw new IllegalArgumentException("can't retrieve wishlist while reading mail template.");
        }
        WishListMailForm form = new WishListMailForm();
        if(wl != null) {
            WishListRestBean bean = (WishListRestBean)thisProxy.toRestModel(wl);
            WishListMailRestBean template = thisProxy.createClient(bean).follow("mail").execute(WishListMailRestBean.class);
            form = toWishListMailForm(template, wl);
        }
        return form;
    }

    private Long getId(WishList model) {
        if(WishListImpl.class.isInstance(model)) {
            return ((WishListImpl)model).getId();
        }
        return null;
    }

    @Override
    public WishListMailForm readMailTemplateByWishListId(long id) throws ClientException {
        WishListImpl proto = new WishListImpl();
        proto.setId(id);
        return readMailTemplate(proto);
    }

    public void setUserIDProvider(UserIDProvider userIDProvider) {
        this.userIDProvider = userIDProvider;
    }

//    @ServiceReference
//    public void setPersonService(PersonService personService) {
//        this.personService = personService;
//    }

//	@ServiceReference
//    public void setWishListPrivacyService(WishListPrivacyService wishListPrivacyService) {
//        this.wishListPrivacyService = wishListPrivacyService;
//    }
}
