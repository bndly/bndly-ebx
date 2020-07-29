/*
 * Copyright (c) 2012, cyber:con GmbH, Bonn.
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

package org.bndly.ebx.client.service.property;

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

import org.bndly.ebx.model.Property;
import java.io.Serializable;

public class PropertyKey implements Serializable {


    private static final long serialVersionUID = 4776648906675540997L;

    private String path;
    private String value;

    private Property property;

    public PropertyKey(String path) {
        this.path = path;
    }

    public <T extends Property> PropertyKey(String path, T property) {
        this.path = path;
        if (null != property) {
            this.property = property;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public <T extends Property> T getProperty() {
        return (T) property;
    }

    public <T extends Property> void setProperty(T property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PropertyKey that = (PropertyKey) o;

        if (path != null ? !path.equals(that.path) : that.path != null) {
            return false;
        }
        if (property != null ? !property.equals(that.property) : that.property != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (property != null ? property.hashCode() : 0);
        return result;
    }

    public String getIdentifier() {
        return "" + hashCode();
    }
}
