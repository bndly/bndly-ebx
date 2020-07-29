package org.bndly.ebx.osgi.config;

/*-
 * #%L
 * org.bndly.ebx.crypto-config
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

import org.bndly.common.osgi.config.spi.CipherProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.ServiceLoader;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class CipherProviderImplTest {
	
	@Test
	public void testEncryptionAndDecryption() throws IOException {
		ServiceLoader<CipherProvider> loader = ServiceLoader.load(CipherProvider.class, getClass().getClassLoader());
		Iterator<CipherProvider> iter = loader.iterator();
		CipherProvider cipherProvider = null;
		while (iter.hasNext()) {
			CipherProvider next = iter.next();
			if (CipherProviderImpl.ALIAS.equals(next.getAlias())) {
				cipherProvider = next;
			}
		}
		Assert.assertNotNull(cipherProvider);
		String somethingToEncrypt = "This is a secret!";
		byte[] somethingToEncryptBytes = somethingToEncrypt.getBytes("UTF-8");
		Cipher encCipher = cipherProvider.restoreEncryptionCipher(CipherProviderImpl.ALIAS);
		//
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (CipherOutputStream cipherOutputStream = new CipherOutputStream(bos, encCipher)) {
			try (OutputStreamWriter osw = new OutputStreamWriter(cipherOutputStream, "UTF-8")) {
				osw.write(somethingToEncrypt);
				osw.flush();
			}
			cipherOutputStream.flush();
		}
		bos.flush();
		byte[] encryptedBytes = bos.toByteArray();
		Assert.assertNotEquals(somethingToEncryptBytes.length, encryptedBytes.length);
		for (int i = 0; i < encryptedBytes.length; i++) {
			byte encryptedByte = encryptedBytes[i];
			if (somethingToEncryptBytes.length > i) {
				Assert.assertNotEquals(encryptedByte, somethingToEncryptBytes[i]);
			} else {
				break;
			}
		}

		Cipher decCipher = cipherProvider.restoreDecryptionCipher(CipherProviderImpl.ALIAS, null);
		bos = new ByteArrayOutputStream();
		try (CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(encryptedBytes), decCipher)) {
			int i;
			while ((i = cipherInputStream.read()) > -1) {
				bos.write(i);
			}
		}
		bos.flush();
		String decrypted = bos.toString("UTF-8");
		Assert.assertEquals(decrypted, somethingToEncrypt);
	}
}
