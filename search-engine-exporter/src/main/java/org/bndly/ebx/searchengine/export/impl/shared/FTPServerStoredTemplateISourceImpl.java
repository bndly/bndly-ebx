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

import org.bndly.ebx.searchengine.export.api.FTPConfig;
import org.bndly.ebx.searchengine.export.api.TemplateSource;
//import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
public class FTPServerStoredTemplateISourceImpl /*implements TemplateSource*/ {

//    private FTPConfig ftpConfig;
//
//    public FTPServerStoredTemplateISourceImpl(FTPConfig ftpConfig){
//        this.ftpConfig = ftpConfig;
//    }
//
//    public File downloadFile(String filename) throws IOException {
//
//        if(filename != null){
//
//            String fileNameWithoutPath = null;
//            if (filename.contains("/")) {
//                String[] pathAndFilename = filename.split("/");
//                if (pathAndFilename.length > 1) {
//                    fileNameWithoutPath = pathAndFilename[pathAndFilename.length - 1];
//                }
//            } else {
//                fileNameWithoutPath = filename;
//            }
//
//            FTPClient ftpClient = new FTPClient();
//            ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
//            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
//
//            File file = new File(fileNameWithoutPath);
//
//            if (file.exists()) {
//                file.setWritable(true);
//                file.delete();
//            }
//
//            file.createNewFile();
//
//            FileOutputStream fileOutputStream = null;
//            if (file.exists()) {
//                fileOutputStream = new FileOutputStream(file);
//                try {
//                    readFileFromFTPServer(ftpClient, filename, fileOutputStream);
//                } finally {
//                    ftpClient.disconnect();
//
//                    if (fileOutputStream != null) {
//                        fileOutputStream.close();
//                    }
//                }
//                return file;
//            }
//        }
//        return null;
//    }
//
//    private boolean uploadFile(String toBeUploadedFilenameWithPath, String remoteFileDir) throws IOException {
//        if(toBeUploadedFilenameWithPath != null){
//            File file = new File(toBeUploadedFilenameWithPath);
//            if(file.exists()){
//                return uploadFile(file, remoteFileDir);
//            }
//        }
//        return false;
//    }
//
//    private boolean uploadFile(File file, String remoteFileDir) throws IOException {
//        FileInputStream fileInputStream = null;
//        if(file != null){
//            fileInputStream = new FileInputStream(file);
//            return uploadStream(fileInputStream, remoteFileDir);
//        }
//        return false;
//    }
//
//    private boolean uploadStream(InputStream inputStream, String remoteFileDir) throws IOException {
//        if (inputStream != null && remoteFileDir != null) {
//            FTPClient ftpClient = new FTPClient();
//            ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
//            ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
//
//            try {
//                return ftpClient.storeFile(remoteFileDir, inputStream);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } finally {
//                ftpClient.disconnect();
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//            }
//        }
//        return false;
//    }
//
//
//    @Override
//    public boolean store(String filename, String fileDir) throws IOException {
//        return uploadFile(filename,fileDir);
//    }
//
//    @Override
//    public boolean store(File file, String fileDir) throws IOException {
//        return uploadFile(file, fileDir);
//    }
//
//    @Override
//    public boolean store(InputStream inputStream, String fileDir) throws IOException {
//        return uploadStream(inputStream, fileDir);
//    }
//
//    public void write(File file, String filenameWithPathOnServer) throws IOException {
//
//        FTPClient ftpClient = new FTPClient();
//        ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
//        ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
//
//
//        FileInputStream fileInputStream = null;
//        try {
//            fileInputStream = new FileInputStream(file);
//            storeFileOnFTPServer(ftpClient, filenameWithPathOnServer, fileInputStream);
//        } finally {
//            ftpClient.disconnect();
//
//            if(fileInputStream != null){
//                fileInputStream.close();
//            }
//        }
//
//    }
//
//    protected final void readFileFromFTPServer(FTPClient ftpClient, String filenameWithPathOnServer, OutputStream outputStream) throws IOException {
//
//        boolean success = ftpClient.retrieveFile(filenameWithPathOnServer, outputStream);
//        if(!success){
//            throw new IOException("Retrieve file failded: " + filenameWithPathOnServer);
//        }
//    }
//
//    protected final void storeFileOnFTPServer(FTPClient ftpClient, String filename, InputStream inputStream) throws IOException {
//
//        boolean success = ftpClient.storeFile(filename, inputStream);
//        if(!success){
//            throw new IOException("Store file failded: " + filename);
//        }
//    }
//
//    @Override
//    public void read(String filename, OutputStream outputStream) throws IOException {
//
//        FTPClient ftpClient = new FTPClient();
//        ftpClient.connect(ftpConfig.getServer(), ftpConfig.getPort());
//        ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
//
//        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
//
//        try {
//            readFileFromFTPServer(ftpClient, filename, bufferedOutputStream);
//        } finally {
//            ftpClient.disconnect();
//        }
//
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
//    public Reader getReader(String filename) throws IOException {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        read(filename, outputStream);
//        StringReader stringReader = new StringReader((String)outputStream.toString());
//        outputStream.close();
//
//        return new BufferedReader(stringReader);
//    }
}
