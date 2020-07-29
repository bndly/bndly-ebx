package org.bndly.business.executor;

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

import org.bndly.common.bpm.annotation.ProcessVariable;
import org.bndly.common.bpm.api.TaskExecutor;
import org.bndly.ebx.model.Address;
import org.bndly.ebx.model.CheckoutRequest;
import org.bndly.ebx.model.LineItem;
import org.bndly.ebx.model.PurchaseOrder;
import org.bndly.ebx.model.WishList;
import org.bndly.ebx.model.WishListItem;
import org.bndly.schema.api.Record;
import org.bndly.schema.api.RecordContext;
import org.bndly.schema.api.exception.EmptyResultException;
import org.bndly.schema.beans.ActiveRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdjustWishListsAfterCheckoutTaskExecutorImpl extends AbstractSchemaRelatedTaskExecutor implements TaskExecutor {

    @ProcessVariable(ProcessVariable.Access.READ)
    private CheckoutRequest checkoutRequest;

	@Override
    public void run() {
        RecordContext ctx = schemaBeanFactory.getRecordFromSchemaBean(checkoutRequest).getContext();
        PurchaseOrder order = checkoutRequest.getOrder();

        List<LineItem> lineItems = order.getItems();
        if (lineItems != null) {
            Set<String> wishListSecurityTokens = wishListSecurityTokensForLineItems(lineItems);

            if (!wishListSecurityTokens.isEmpty()) {
                Collection<WishListItem> wishListItems = findAndAdjustWishListsAndLineItems(wishListSecurityTokens, lineItems, ctx);
                updateChangedWishListItems(wishListItems);
            }
        }
    }

    private Set<String> wishListSecurityTokensForLineItems(List<LineItem> lineItems) {
        Set<String> wishListSecurityTokens = new LinkedHashSet<>();
        for (LineItem lineItem : lineItems) {
            if (lineItem.getWishListSecurityToken() != null) {
                wishListSecurityTokens.add(lineItem.getWishListSecurityToken());
            }
        }
        return wishListSecurityTokens;
    }

    private Collection<WishListItem> findAndAdjustWishListsAndLineItems(Set<String> wishListSecurityTokens, List<LineItem> lineItems, RecordContext ctx) {
        List<WishList> wishLists = new ArrayList<>();
        for (String token : wishListSecurityTokens) {
            Record r = null;
            try {
                r = engine.getAccessor().queryByExample(WishList.class.getSimpleName(), ctx).attribute("securityToken", token).single();
            } catch(EmptyResultException e) {
            }
            if(r != null) {
                wishLists.add(schemaBeanFactory.getSchemaBean(WishList.class, r));
            }
        }
        List<WishListItem> changedWishListItems = new ArrayList<>();
        
        Map<String, WishList> wishListsBySecurityToken = new CollectionIndexer().asMap(wishLists, new CollectionIndexer.IndexerFunction<String, WishList>() {
            @Override
            public String buildKey(WishList input) {
                return input.getSecurityToken();
            }
        });
        
        for (LineItem lineItem : lineItems) {
            if (lineItem.getWishListSecurityToken() != null) {
                WishList wishList = wishListsBySecurityToken.get(lineItem.getWishListSecurityToken());
                if (wishList != null) {
                    List<WishListItem> wishListItems = wishList.getItems();
                    Map<String, WishListItem> wishListItemsBySKU = new CollectionIndexer().asMap(wishListItems, new CollectionIndexer.IndexerFunction<String, WishListItem>() {
                        @Override
                        public String buildKey(WishListItem input) {
                            return input.getSku();
                        }
                    });
                    WishListItem wishListItemFromWishList = wishListItemsBySKU.get(lineItem.getSku());

                    adjustRemainingAmountOfWishListItem(wishListItemFromWishList, lineItem);
                    if (wishListItemFromWishList != null) {
                        // FIX ME : THIS IS NEVER ENTERED
                        changedWishListItems.add(wishListItemFromWishList);
                    }

                    lineItem.setWishListName(wishList.getName());
                    if (wishList.getPrivacy().getUseNickNameAsWishListOwner()) {
                        lineItem.setWishListOwner(wishList.getUserNickName());
                    } else {
                        Address a = wishList.getPerson().getAddress();
                        lineItem.setWishListOwner(a.getFirstName() + " " + a.getLastName());
                    }
                }
            }
        }

        return changedWishListItems;
    }

    private WishListItem adjustRemainingAmountOfWishListItem(WishListItem wishListItemFromWishList, LineItem lineItem) {
        if (wishListItemFromWishList != null) {
            Long lineItemQuantity = lineItem.getQuantity();
			lineItemQuantity = lineItemQuantity == null ? 0 : lineItemQuantity;
			Long ra = wishListItemFromWishList.getRemainingAmount();
            if(ra == null) {
				ra = wishListItemFromWishList.getDesiredAmount();
                wishListItemFromWishList.setRemainingAmount(ra);
            }
            if(ra != null) {
				long remainingAmount = ra - lineItemQuantity;
				if (remainingAmount < 0) {
					remainingAmount = 0;
				}
				wishListItemFromWishList.setRemainingAmount(remainingAmount);
				
			}
            Long boughtAmount = wishListItemFromWishList.getBoughtAmount();
            if (boughtAmount == null) {
                boughtAmount = 0L;
            }
            boughtAmount += lineItemQuantity;
            wishListItemFromWishList.setBoughtAmount(boughtAmount);
        }

        return wishListItemFromWishList;
    }

    private void updateChangedWishListItems(Collection<WishListItem> wishListItems) {
        for (WishListItem wishListItem : wishListItems) {
            ((ActiveRecord) wishListItem).update();
        }
    }
    
}
