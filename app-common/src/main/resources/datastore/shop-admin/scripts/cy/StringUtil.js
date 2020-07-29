/*-
 * #%L
 * org.bndly.ebx.app-common
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
define(function() {
    return {
        upperCaseFirst: function(a) {
            if (!a) {
                console.warn("illegal argument while uppercasing first character.");
                return undefined;
            }
            return a.length > 1 ? a.substring(0, 1).toUpperCase() + a.substring(1) : a.toUpperCase();
        },
        lowerCaseFirst: function(a) {
            if (!a) {
                console.warn("illegal argument while lowercasing first character.");
                return undefined;
            }
            return a.length > 1 ? a.substring(0, 1).toLowerCase() + a.substring(1) : a.toLowerCase();
        },
        formatDate: function(value) {
            if (value) {
                var tmp = [];
                tmp[0] = 1900 + value.getYear();
                tmp[1] = value.getMonth() + 1;
                if (tmp[1] < 10) {
                    tmp[1] = "0" + tmp[1];
                }
                tmp[2] = value.getDate();
                if (tmp[2] < 10) {
                    tmp[2] = "0" + tmp[2];
                }
                value = tmp.join("-");
            } else {
                value = "";
            }
            return value;
        },
        formatTime: function(value) {
            if (value) {
                var tmp = [];
                tmp[0] = value.getHours();
                if (tmp[0] < 10) {
                    tmp[1] = "0" + tmp[0];
                }
                tmp[1] = value.getMinutes() + 1;
                if (tmp[1] < 10) {
                    tmp[1] = "0" + tmp[1];
                }
                tmp[2] = value.getSeconds();
                if (tmp[2] < 10) {
                    tmp[2] = "0" + tmp[2];
                }
                value = tmp.join(":");
            } else {
                value = "";
            }
            return value;
        }
    };
});
