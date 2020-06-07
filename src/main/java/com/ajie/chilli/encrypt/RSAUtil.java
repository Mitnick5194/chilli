package com.ajie.chilli.encrypt;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * rsa加密
 * 
 * @author niezhenjie
 */
public class RSAUtil {

	/**
	 * 生成一对秘钥
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static RSAKey genKey() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		// 初始化密钥对生成器，密钥大小为96-1024位
		keyPairGen.initialize(1024, new SecureRandom());
		// 生成一个密钥对，保存在keyPair中
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate(); // 得到私钥
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic(); // 得到公钥
		String publicKeyString = new String(Base64.getEncoder().encode(
				publicKey.getEncoded()));
		// 得到私钥字符串
		String privateKeyString = new String(Base64.getEncoder().encode(
				(privateKey.getEncoded())));
		return new RSAKey(privateKeyString, publicKeyString);
	}

	/**
	 * RSA公钥加密
	 * 
	 * @param str
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String str, String publicKey) throws Exception {
		// base64编码的公钥
		byte[] decoded = Base64.getDecoder().decode(publicKey);
		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(decoded));
		// RSA加密
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		String outStr = Base64.getEncoder().encodeToString(
				cipher.doFinal(str.getBytes("UTF-8")));
		return outStr;
	}

	/**
	 * RSA私钥解密
	 * 
	 * @param str
	 *            加密字符串
	 * @param privateKey
	 *            私钥
	 * @return 铭文
	 * @throws Exception
	 *             解密过程中的异常信息
	 */
	public static String decrypt(String str, String privateKey)
			throws Exception {
		// 64位解码加密后的字符串
		byte[] inputByte = Base64.getDecoder().decode(str.getBytes("UTF-8"));
		// base64编码的私钥
		byte[] decoded = Base64.getDecoder().decode(privateKey);
		RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
				.generatePrivate(new PKCS8EncodedKeySpec(decoded));
		// RSA解密
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, priKey);
		String outStr = new String(cipher.doFinal(inputByte));
		return outStr;
	}

	public static class RSAKey {
		/** rsa私钥 */
		private String privateKey;
		/** rsa公钥 */
		private String publicKey;

		public RSAKey(String prikey, String pubkey) {
			privateKey = prikey;
			publicKey = pubkey;
		}

		public String getPrivateKey() {
			return privateKey;
		}

		public String getPublicKey() {
			return publicKey;
		}
	}
	
	public static void main(String[] args) throws Exception {
		String content = "abc134431";
		RSAKey key = genKey();
		String enc = encrypt(content, key.getPublicKey());
		System.out.println(enc);
		String dec = decrypt(enc, key.getPrivateKey());
		System.out.println(dec);
		
	}

}
