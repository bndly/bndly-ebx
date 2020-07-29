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

package org.bndly.ebx.searchengine.export.impl.shared;

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

import org.bndly.ebx.searchengine.export.api.ExportServiceException;
import org.bndly.ebx.searchengine.export.api.TemplateSource;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

/**
 * Created by IntelliJ IDEA.
 * User: borismatosevic
 * Date: 14.05.13
 * Time: 11:10
 * To change this template use File | Settings | File Templates.
 */
public class DiscStoredTemplateISourceImpl /*implements TemplateSource*/ {

//    private boolean copy(String source, String target) throws IOException, ExportServiceException {
//        if(!"".equals(source) && source != null && !"".equals(target) && target != null){
//            File sourceFile = new File(source);
//            if(!sourceFile.exists()){
//                throw new ExportServiceException(String.format("Source file '%s' not exists!", source));
//            }
//
//            File targetFile = overwriteFileIfExists(target);
//
//            return copy(sourceFile, targetFile);
//        }
//        return false;
//    }
//
//    private boolean copy(File sourceFile, String target) throws IOException, ExportServiceException {
//
//        if(sourceFile != null && target != null){
//            File targetFile = overwriteFileIfExists(target);
//            return copy(sourceFile, targetFile);
//        }
//
//        return false;
//    }
//
//    private boolean copy(File sourceFile, File targetFile) throws IOException {
//
//        if(sourceFile != null && targetFile != null){
//            InputStream inputStream = new FileInputStream(sourceFile);
//            OutputStream outputStream = new FileOutputStream(targetFile);
//
//            return copy(inputStream, outputStream);
//        }
//
//        return false;
//    }
//
//    private boolean copy(InputStream sourceInStream, String target) throws IOException, ExportServiceException {
//
//        if(sourceInStream != null && target != null){
//            File targetFile = overwriteFileIfExists(target);
//            return copy(sourceInStream, new FileOutputStream(targetFile));
//        }
//        return false;
//    }
//
//
//    private boolean copy(InputStream sourceInStream, OutputStream targetOutStream) throws IOException {
//
//        if(sourceInStream != null && targetOutStream != null) {
//            try {
//                byte[] buf = new byte[1014];
//                int len;
//
//                while((len = sourceInStream.read(buf)) > 0){
//                    targetOutStream.write(buf, 0, len);
//                }
//            } finally {
//              sourceInStream.close();
//              targetOutStream.close();
//            }
//
//
//            return true;
//        }
//        return false;
//    }
//
//    private OutputStream getOutputStream(String toBeUploadedFilename) throws IOException {
//
//        File file = new File(toBeUploadedFilename);
//        if(!file.exists()){
//            file.createNewFile();
//        }
//
//        return new FileOutputStream(file);
//    }
//
//    private File overwriteFileIfExists(String filenameWithPath) throws IOException, ExportServiceException {
//        if(!"".equals(filenameWithPath) && filenameWithPath != null) {
//            File file = new File(filenameWithPath);
//            overwriteFileIfExists(file);
//            return file;
//        }
//        return null;
//    }
//
//    private void overwriteFileIfExists(File file) throws IOException, ExportServiceException {
//       if(file != null && !file.exists()) {
//           file.createNewFile();
//       } else {
//           file.delete();
//           file.createNewFile();
//       }
//
//        if(!file.exists()){
//            throw new ExportServiceException(String.format("File '%s' could not be created or file exists and overwrite failed!", file));
//        }
//    }
//
//    @Override
//    public boolean store(String filename, String fileDir) throws IOException, ExportServiceException {
//        return copy(filename, fileDir);
//    }
//
//    @Override
//    public boolean store(File file, String fileDir) throws IOException, ExportServiceException {
//        return copy(file, fileDir);
//    }
//
//    @Override
//    public boolean store(InputStream inputStream, String fileDir) throws IOException, ExportServiceException {
//        return copy(inputStream, fileDir);
//    }
//
//    @Override
//    public void read(String filename, OutputStream outputStream) throws IOException, ExportServiceException {
//
//        if(!"".equals(filename) && filename != null){
//            File readFile = new File(filename);
//            boolean copySuccessfull = copy(new FileInputStream(readFile), outputStream);
//
//            if (!copySuccessfull) {
//               throw new ExportServiceException(String.format("Read file '%s' into outputStream failed!", readFile));
//            }
//        }
//    }
//
//    @Override
//    public Writer getWriter(String toBeUploadedFilename) throws IOException {
//
//        File file = new File(toBeUploadedFilename);
//        if(!file.exists()){
//            file.createNewFile();
//        }
//
//        return new FileWriter(file);
//    }
//
//    @Override
//    public Reader getReader(String filename) throws IOException, ExportServiceException {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        read(filename, outputStream);
//        StringReader stringReader = new StringReader((String)outputStream.toString());
//        outputStream.close();
//
//        return new BufferedReader(stringReader);
//    }
}
