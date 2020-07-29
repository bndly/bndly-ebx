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
    function swap(items, firstIndex, secondIndex) {
        var temp = items[firstIndex];
        items[firstIndex] = items[secondIndex];
        items[secondIndex] = temp;
    };
    function partition(items, left, right, comparator) {

        var pivot = items[Math.floor((right + left) / 2)],
                i = left,
                j = right;


        while (i <= j) {

            while (comparator(items[i], pivot) < 0) {
                i++;
            }

            while (comparator(items[i], pivot) > 0) {
                j--;
            }

            if (i <= j) {
                swap(items, i, j);
                i++;
                j--;
            }
        }

        return i;
    }
    ;
    function quickSort(items, left, right, comparator) {

        var index;

        if (items.length > 1) {

            left = typeof left !== "number" ? 0 : left;
            right = typeof right !== "number" ? items.length - 1 : right;

            index = partition(items, left, right, comparator);

            if (left < index - 1) {
                quickSort(items, left, index - 1, comparator);
            }

            if (index < right) {
                quickSort(items, index, right, comparator);
            }

        }

        return items;
    }
    ;

    return {
        sort: function(array, comparator) {
            return quickSort(array, undefined, undefined, comparator);
        }
    };
});
