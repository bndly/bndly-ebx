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

import org.bndly.common.crypto.impl.shared.Base64Util;
import org.bndly.common.osgi.config.spi.CipherProvider;
import org.bndly.common.osgi.config.spi.PrefixHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class CipherProviderImpl implements CipherProvider, PrefixHandler {

	private static final Logger LOG = LoggerFactory.getLogger(CipherProviderImpl.class);
	public static final String ALIAS = "CRYPT";
	public static final String PREFIX = "CRYPT";
	private static final SecretKey KEY;
	
	private final PrefixHandler prefixHandler = new PrefixHandler() {
		@Override
		public String getPrefix() {
			return PREFIX;
		}

		@Override
		public String get(String rawStringValue) {
			// decrypt value
			Cipher cipher = restoreDecryptionCipher(ALIAS, null);
			try (InputStream is = new CipherInputStream(new ByteArrayInputStream(Base64Util.decode(rawStringValue)), cipher)) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int i;
				while ((i = is.read(buffer)) > -1) {
					bos.write(buffer, 0, i);
				}
				bos.flush();
				return bos.toString("UTF-8");
			} catch (IOException e) {
				LOG.error("could not decrypt value", e);
				return null;
			}
		}

		@Override
		public String set(String rawStringValue) {
			// encrypt value
			Cipher cipher = restoreEncryptionCipher(ALIAS);
			try (InputStream is = new CipherInputStream(new ByteArrayInputStream(Base64Util.decode(rawStringValue)), cipher)) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int i;
				while ((i = is.read(buffer)) > -1) {
					bos.write(buffer, 0, i);
				}
				bos.flush();
				return Base64Util.encode(bos.toByteArray());
			} catch (IOException e) {
				LOG.error("could not decrypt value", e);
				return null;
			}
		}
	};
	
	private final CipherProvider cipherProvider = new CipherProvider() {
		@Override
		public String getAlias() {
			return ALIAS;
		}

		@Override
		public Cipher restoreDecryptionCipher(String alias, String initVectorBase64) {
			if (!ALIAS.equals(alias) || KEY == null) {
			return null;
			}
			try {
				Cipher cipher = Cipher.getInstance("AES");
				if (initVectorBase64 != null) {
					cipher.init(Cipher.DECRYPT_MODE, KEY, new IvParameterSpec(Base64Util.decode(initVectorBase64)));
				} else {
					cipher.init(Cipher.DECRYPT_MODE, KEY);
				}
				return cipher;
			} catch (
					NoSuchAlgorithmException 
					| NoSuchPaddingException 
					| InvalidAlgorithmParameterException 
					| InvalidKeyException ex
			) {
				LOG.error("could not set up cipher", ex);
				return null;
			}
		}

		@Override
		public Cipher restoreEncryptionCipher(String alias) {
			if (!ALIAS.equals(alias) || KEY == null) {
				return null;
			}
			try {
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.ENCRYPT_MODE, KEY);
				return cipher;
			} catch (
					NoSuchAlgorithmException 
					| NoSuchPaddingException 
					| InvalidKeyException ex
			) {
				LOG.error("could not set up cipher", ex);
				return null;
			}
		}
	};
	
	static {
		InputStream tmp = CipherProviderImpl.class.getClassLoader().getResourceAsStream("config.jceks");
		SecretKey tmpKey = null;
		if (tmp != null) {
			try (InputStream is = tmp) {
				KeyStore ks = KeyStore.getInstance("JCEKS");
				ks.load(is, "changeit".toCharArray());
				KeyStore.Entry entry = ks.getEntry("config", new KeyStore.PasswordProtection("changeit".toCharArray()));
				if (KeyStore.SecretKeyEntry.class.isInstance(entry)) {
					tmpKey = ((KeyStore.SecretKeyEntry) entry).getSecretKey();
				} else {
					LOG.error("keystore did not contain the secret key");
				}
			} catch (UnrecoverableEntryException | KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
				LOG.error("could not init secret key", ex);
			}
		}
		KEY = tmpKey;
	}

	@Override
	public final String getAlias() {
		return cipherProvider.getAlias();
	}

	@Override
	public Cipher restoreDecryptionCipher(String alias, String initVectorBase64) {
		return cipherProvider.restoreDecryptionCipher(alias, initVectorBase64);
	}

	@Override
	public Cipher restoreEncryptionCipher(String alias) {
		return cipherProvider.restoreEncryptionCipher(alias);
	}

	@Override
	public String getPrefix() {
		return prefixHandler.getPrefix();
	}

	@Override
	public String get(String rawStringValue) {
		return prefixHandler.get(rawStringValue);
	}

	@Override
	public String set(String rawStringValue) {
		return prefixHandler.set(rawStringValue);
	}

}
