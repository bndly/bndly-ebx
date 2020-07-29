package org.bndly.ebx.jcr.importer.api;

/*-
 * #%L
 * org.bndly.ebx.jcr.importer-api
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

import javax.jcr.*;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by sgodecker on 17.08.15.
 */
public class JcrUtil {

    public static final String[] STANDARD_LABEL_CHAR_MAPPING = new String[]{"_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "-", "_", "_", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "_", "_", "_", "_", "_", "_", "_", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "_", "_", "_", "_", "_", "_", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "_", "_", "_", "_", "_", "_", "f", "_", "_", "_", "fi", "fi", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "y", "_", "_", "_", "_", "i", "c", "p", "o", "v", "_", "s", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "_", "a", "a", "a", "a", "ae", "a", "ae", "c", "e", "e", "e", "e", "i", "i", "i", "i", "d", "n", "o", "o", "o", "o", "oe", "x", "o", "u", "u", "u", "ue", "y", "b", "ss", "a", "a", "a", "a", "ae", "a", "ae", "c", "e", "e", "e", "e", "i", "i", "i", "i", "o", "n", "o", "o", "o", "o", "oe", "_", "o", "u", "u", "u", "ue", "y", "b", "y"};


    public static void setProperty(Node node, String propertyName, Object propertyValue) throws RepositoryException{
        node.setProperty(propertyName,mapObjectToJcrValue(propertyValue,node.getSession()));
    }

    public static void setProperty(Node node, String propertyName, Object propertyValue, boolean filterNullValue) throws RepositoryException{

        if(!filterNullValue || propertyValue != null){
            setProperty(node, propertyName, propertyValue);
        }
    }

    public static Value mapObjectToJcrValue(Object value, Session session) throws RepositoryException{
        ValueFactory fac = session.getValueFactory();
        Value val;
        if(value instanceof Calendar) {
            val = fac.createValue((Calendar)value);
        } else if(value instanceof InputStream) {
            val = fac.createValue(session.getValueFactory().createBinary((InputStream)value));
        } else if(value instanceof Node) {
            val = fac.createValue((Node)value);
        } else if(value instanceof Long) {
            val = fac.createValue(((Long)value).longValue());
        } else if(value instanceof Number) {
            val = fac.createValue(((Number)value).doubleValue());
        } else if(value instanceof Boolean) {
            val = fac.createValue(((Boolean)value).booleanValue());
        } else if(value instanceof String) {
            val = fac.createValue((String)value);
        } else {
            val = null;
        }

        return val;
    }

    public static String createValidName(String title) {
        return createValidName(title, STANDARD_LABEL_CHAR_MAPPING);
    }

    public static String createValidName(String title, String[] labelCharMapping) {
        return createValidName(title, labelCharMapping, "_");
    }

    public static String createValidName(String title, String[] labelCharMapping, String defaultReplacementCharacter) {
        char[] chrs = title.toCharArray();
        StringBuffer name = new StringBuffer(chrs.length);
        boolean prevEscaped = false;

        for(int idx = 0; idx < title.length() && name.length() < 64; ++idx) {
            char c = title.charAt(idx);
            String repl = defaultReplacementCharacter;
            if(c >= 0 && c < labelCharMapping.length) {
                repl = labelCharMapping[c];
            }

            if(repl.equals(defaultReplacementCharacter)) {
                if(!prevEscaped && name.length() < 16) {
                    name.append(defaultReplacementCharacter);
                }

                prevEscaped = true;
            } else {
                name.append(repl);
                prevEscaped = false;
            }
        }

        return name.toString();
    }

}
