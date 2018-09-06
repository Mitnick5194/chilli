package com.ajie.chilli.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * httpclient简单封装
 * 
 * @author niezhenjie
 */
public class HttpClientUtil {
	private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

	public static final int SUC_CODE = 200;

	private HttpClientUtil() {
	}

	/**
	 * get方式请求
	 * 
	 * @param url
	 *            请求连接
	 * @param params
	 *            请求参数
	 * @return
	 * @throws IOException
	 */
	public static String doGet(String url, Map<String, String> params) throws IOException {
		// 创建httpclient对象
		CloseableHttpClient client = HttpClients.createDefault();
		String result = "";
		CloseableHttpResponse response = null;
		try {
			URIBuilder builder = new URIBuilder(url);
			if (null != params) {
				for (String key : params.keySet()) {
					builder.addParameter(key, params.get(key));
				}
			}
			URI uri = builder.build();
			HttpGet get = new HttpGet(uri);
			response = client.execute(get);
			if (response.getStatusLine().getStatusCode() == SUC_CODE) {
				result = EntityUtils.toString(response.getEntity(), "utf-8");
			}
		} catch (URISyntaxException e) {
			logger.error("uri语法错误: " + url, e);
		} catch (ClientProtocolException e) {
			logger.error("url协议有误: " + url, e);
		} finally {
			if (null != response) {
				response.close();
			}
			client.close();
		}
		return result;
	}

	public static String doGet(String url) throws IOException {
		return doGet(url, null);
	}

	/**
	 * post请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static String doPost(String url, Map<String, String> params) throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		String result = "";
		CloseableHttpResponse response = null;
		HttpPost post = new HttpPost(url);
		try {
			if (null != params) {
				List<NameValuePair> paramList = new ArrayList<NameValuePair>();
				for (String key : params.keySet()) {
					paramList.add(new BasicNameValuePair(key, params.get(key)));
				}
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
				post.setEntity(entity);
			}
			response = client.execute(post);
			result = EntityUtils.toString(response.getEntity(), "utf-8");
		} catch (ClientProtocolException e) {
			logger.error("协议无效：" + url, e);
		} catch (ParseException e) {
			logger.error("无法解析远程调用返回结果：" + url + " result: " + result, e);
		} catch (UnsupportedEncodingException e) {
			logger.error("远程调用返回结果不支持的utf-8编码: " + url, e);
		} finally {
			if (null != response) {
				response.close();
			}
			client.close();
		}
		return result;
	}

	public static String doPost(String url) throws IOException {
		return doPost(url, null);
		
	}

}
