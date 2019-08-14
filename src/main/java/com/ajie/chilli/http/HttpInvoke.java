package com.ajie.chilli.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.ajie.chilli.common.ResponseResult;
import com.ajie.chilli.http.Options.WeightRang;
import com.ajie.chilli.http.exception.InvokeException;
import com.ajie.chilli.utils.common.JsonUtils;
import com.ajie.chilli.utils.common.StringUtils;

/**
 * http调用器，url一般不包含uri，只有在调用的时候才会指定调用哪个uri
 * 
 * 如：<br>
 * <p>
 * http://www.nzjie.cn/resource;connect_timeout=10;socket_timeout=15;weight=3;
 * http://xylx.nzjie.cn/resource;connect_timeout=10;socket_timeout=15;weight=1;
 * down
 * </p>
 * 
 * @author niezhenjie
 *
 */
public class HttpInvoke {
	/** 默认连接超时 —— 15s */
	public static final int DEFALUT_CONNECT_TIMEOUT = 15;

	/** 默认响应超时 —— 30s */
	public static final int DEFALUT_READ_TIMEOUT = 30;

	/** 链接信息 */
	volatile protected List<Options> urls;

	/** 最后一次指向的链接 */
	protected int lastCursor;

	/** http链接池配置 */
	private PoolingHttpClientConnectionManager connManager;

	/** 默认最大链接数 */
	public final int DEFAULT_MAXTOTAL = 200;
	/** 默认的每个路由的最大连接数 */
	public final int DEFAULT_MAXPERROUTE = 200;

	/** 请求类型 -- get */
	public static final int TYPE_GET = 1;
	/** 请求类型 -- post */
	public static final int TYPE_POST = 2;

	public HttpInvoke() {
		connManager = new PoolingHttpClientConnectionManager();
		connManager.setMaxTotal(DEFAULT_MAXTOTAL);
		connManager.setDefaultMaxPerRoute(DEFAULT_MAXPERROUTE);
		lastCursor = 0;
	}

	public void setMaxTotal(int maxTotal) {
		connManager.setMaxTotal(maxTotal);
	}

	public void setMaxPerRoute(int max) {
		connManager.setDefaultMaxPerRoute(max);
	}

	/**
	 * 更新url
	 * 
	 * @param urls
	 * @return
	 */
	public boolean updateUrls(List<String> urls) {
		if (null == urls || urls.isEmpty()) {
			return false;
		}
		List<Options> list = new ArrayList<Options>(urls.size());
		for (String url : urls) {
			if (StringUtils.isEmpty(url)) {
				throw new NullPointerException("调用链接为空：" + url);
			}
			Options wrap = parse(url);
			list.add(wrap);
		}
		if (list.isEmpty()) {
			return false;
		}
		synchronized (this.urls) {
			this.urls = list;
			handleWeightRange();
		}
		return true;
	}

	/**
	 * 增加url，如果已经有了，则会覆盖
	 * 
	 * @param urls
	 * @return
	 */
	synchronized public boolean addUrls(List<String> urls) {
		if (null == urls || urls.isEmpty()) {
			return false;
		}
		List<Options> list = new ArrayList<Options>(urls.size()
				+ this.urls.size());
		for (String url : urls) {
			if (StringUtils.isEmpty(url)) {
				throw new NullPointerException("调用链接为空：" + url);
			}
			Options option = parse(url);
			list.add(option);
		}
		//把原来的也加进来
		for (Options url : this.urls) {
			if(list.contains(url)){
				continue;//list已经有了
			}
			list.add(url);
		}
		this.urls = list;
		handleWeightRange();
		return true;
	}

	/**
	 * 请求调用
	 * 
	 * @param uri
	 *            调用uri
	 * @param params
	 *            参数
	 * @return
	 * @throws InvokeException
	 */
	public ResponseResult invoke(String uri, Parameter... params)
			throws InvokeException {
		return doGet(uri, null, params);
	}

	/**
	 * 请求调用
	 * 
	 * @param uri
	 *            链接
	 * @param type
	 *            类型 link{HttpInvoke.TYPE_XXX}
	 * @param params
	 *            参数
	 * @return
	 * @throws InvokeException
	 */
	public ResponseResult invoke(String uri, int type, Parameter... params)
			throws InvokeException {
		if (type == TYPE_GET) {
			return doGet(uri, null, params);
		}
		return doPost(uri, null, params);
	}

	/**
	 * 请求调用
	 * 
	 * @param uri
	 *            链接
	 * @param type
	 *            类型 link{HttpInvoke.TYPE_XXX}
	 * @param header
	 *            http信息头
	 * @param params
	 *            参数
	 * @return
	 * @throws InvokeException
	 */
	public ResponseResult invoke(String uri, int type,
			Map<String, String> header, Parameter... params)
			throws InvokeException {
		if (type == TYPE_GET) {
			return doGet(uri, header, params);
		} else {
			return doPost(uri, header, params);
		}
	}

	/**
	 * get方式
	 * 
	 * @param uri
	 * @param params
	 * @return
	 * @throws InvokeException
	 */
	private ResponseResult doGet(String uri, Map<String, String> header,
			Parameter... params) throws InvokeException {
		Options wrap = getNext();
		HttpClient client = get(wrap);
		String url = wrap.getUrl();
		URIBuilder builder = null;
		HttpGet get = null;
		HttpResponse res = null;
		try {
			builder = new URIBuilder(genUrl(url, uri));
			for (Parameter p : params) {
				builder.addParameter(p.getKey(), p.getValue());
			}
			get = new HttpGet(builder.build());
			if (null != header) {
				Iterator<String> it = header.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					String val = header.get(key);
					get.addHeader(key, val);
				}
			}
			res = client.execute(get);
			String result = "";
			ResponseResult response = null;
			if (res.getStatusLine().getStatusCode() != StatusCode.SC_OK) {
				throw new InvokeException(res.getStatusLine().getStatusCode()
						+ "/" + res.getStatusLine().getReasonPhrase(),
						InvokeException.TYPE_ERRCODE);
			}

			result = EntityUtils.toString(res.getEntity(), "utf-8");
			// 尝试转换成ResponseResult
			try {
				response = JsonUtils.toBean(result, ResponseResult.class);
			} catch (Exception e) {
				// 不能解析，尝试获取信息构造
				Object json = JsonUtils.textToJson(result);
				response = ResponseResult.newResult(StatusCode.SC_OK, json);
			}
			return response;
		} catch (URISyntaxException e) {
			throw new InvokeException("无效uri:" + uri, e,
					InvokeException.TYPE_URISYNTAX);
		} catch (ClientProtocolException e) {
			throw new InvokeException("无效协议:" + url, e,
					InvokeException.TYPE_Protocol);
		} catch (IOException e) {
			assertException(e);
			throw new InvokeException("网络异常", e, InvokeException.TYPE_IO);
		} finally {
			if (null != res) {
				try {
					EntityUtils.consume(res.getEntity());
				} catch (IOException e) {
					// 忽略关闭异常
				}

			}
		}
	}

	private void assertException(IOException e) throws InvokeException {
		if (e instanceof HttpHostConnectException) {
			throw new InvokeException(e.getMessage(), e);
		}
		if (e instanceof UnknownHostException) {
			throw new InvokeException("未知主机：" + e.getMessage(), e);
		}
		if (e instanceof SocketTimeoutException) {
			throw new InvokeException("读取超时", e);
		}
		if (e instanceof ConnectTimeoutException) {
			throw new InvokeException("连接超时", e);
		}

	}

	/**
	 * post方式
	 * 
	 * @param uri
	 * @param params
	 * @return
	 * @throws InvokeException
	 */
	private ResponseResult doPost(String uri, Map<String, String> header,
			Parameter... params) throws InvokeException {
		Options wrap = getNext();
		HttpClient client = get(wrap);
		String url = wrap.getUrl();
		HttpPost post = new HttpPost(genUrl(url, uri));
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		for (Parameter param : params) {
			paramList.add(new BasicNameValuePair(param.getKey(), param
					.getValue()));
		}
		if (null != header) {
			Iterator<String> it = header.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				String val = header.get(key);
				post.addHeader(key, val);
			}
		}
		HttpResponse res = null;
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(paramList);
			post.setEntity(entity);
			res = client.execute(post);
			String result = "";
			ResponseResult response = null;
			if (res.getStatusLine().getStatusCode() != StatusCode.SC_OK) {
				throw new InvokeException(res.getStatusLine().getStatusCode()
						+ "/" + res.getStatusLine().getReasonPhrase(),
						InvokeException.TYPE_ERRCODE);
			}
			result = EntityUtils.toString(res.getEntity(), "utf-8");
			// 尝试转换成ResponseResult
			try {
				response = JsonUtils.toBean(result, ResponseResult.class);
			} catch (Exception e) {
				// 不能解析，尝试获取信息构造
				Object json = JsonUtils.textToJson(result);
				response = ResponseResult.newResult(res.getStatusLine()
						.getStatusCode(), json);
			}
			return response;
		} catch (UnsupportedEncodingException e) {
			throw new InvokeException("参数编码异常", e, InvokeException.TYPE_PARAMS);
		} catch (ClientProtocolException e1) {
			throw new InvokeException("无效协议:" + url, e1,
					InvokeException.TYPE_Protocol);
		} catch (IOException e2) {
			assertException(e2);
			throw new InvokeException("网络异常", e2, InvokeException.TYPE_IO);
		} finally {
			if (null != res) {
				try {
					EntityUtils.consume(res.getEntity());
				} catch (IOException e) {
					// 忽略关闭异常
				}

			}
		}
	}

	private String genUrl(String url, String uri) {
		if (!url.endsWith("/")) {
			url += "/";
		}
		if (StringUtils.isEmpty(uri)) {
			return url;
		}
		return url + uri;
	}

	private HttpClient get(Options url) {
		int connectT = url.getConnectTimeout();
		int socketT = url.getReadTimeout();
		RequestConfig config = RequestConfig.custom()
				.setSocketTimeout(socketT * 1000)
				.setConnectTimeout(connectT * 1000).build();
		HttpClient client = HttpClientBuilder.create()
				.setDefaultRequestConfig(config).build();
		return client;
	}

	/**
	 * 寻找调用链接
	 * 
	 * @return
	 */
	private Options getNext() {
		List<Options> list = this.urls;
		// 检查是否全部不活跃
		int activeCount = 0;
		for (Options u : list) {
			if (u.isActive()) {
				activeCount++;
			}
		}
		if (activeCount == 0) {
			throw new IllegalArgumentException("无活跃链接");
		}
		if (list.size() == 1) {
			if (list.get(0).isActive()) {
				return list.get(0);
			}
			return null;
		}

		int max = list.get(list.size() - 1).getWeightRang().getEnd();
		int idx = this.lastCursor + 1;
		if (idx > max) {
			idx = 1;
		}
		Options find = null;
		for (int i = 0; i < list.size(); i++) {
			Options ww = list.get(i);
			WeightRang rang = ww.getWeightRang();
			if (rang.isHit(idx)) {
				if (!ww.isActive()) {
					// 跳过该链接的权重范围
					this.lastCursor = idx + ww.getWeightRang().getEnd();
					ww = getNext();
				}
				// 找到了
				find = ww;
				break;
			}
		}
		this.lastCursor = idx;
		return find;
	}

	/**
	 * 创建实例
	 * 
	 * @param url
	 *            url;timeout=xxx;weight=xxx;[down]<br>
	 *            如：http://nzjie.cn/blog/index.do;timeout=15;weight=3;down,<br>
	 *            其中，timeout单位为秒
	 * @return
	 */
	public static HttpInvoke getInstance(List<String> urls) {
		if (null == urls) {
			throw new NullPointerException("调用链接为空");
		}
		HttpInvoke invoke = new HttpInvoke();
		List<Options> list = new ArrayList<Options>(urls.size());
		invoke.urls = list;
		for (String url : urls) {
			if (StringUtils.isEmpty(url)) {
				throw new NullPointerException("调用链接为空：" + url);
			}
			Options wrap = parse(url);
			list.add(wrap);
		}
		invoke.handleWeightRange();
		return invoke;
	}

	private void sort() {
		synchronized (urls) {
			Collections.sort(urls, new Comparator<Options>() {
				@Override
				public int compare(Options o1, Options o2) {
					return o2.getWeight() - o1.getWeight();
				}
			});
		}
	}

	/**
	 * 处理UrlWrap列表里的权重范围
	 */
	synchronized public void handleWeightRange() {
		// 按照weight排序
		sort();
		List<Options> list = this.urls;
		if (list.size() <= 1) {// 一个就不需要处理了吧
			return;
		}
		int start = 0;
		for (Options wrap : list) {
			Options.WeightRang rang = Options.WeightRang.valueOf(start + 1,
					start + wrap.getWeight());
			start += wrap.getWeight();
			wrap.setWeightRang(rang);
		}
	}

	static private Options parse(String url) {
		String[] strs = url.split(";");
		String u = strs[0];
		HttpUtils.assertHttpProtocol(u);
		String s_connectTimeout = null;
		String s_socketTimeout = null;
		String s_weight = null;
		int connectTimeout = DEFALUT_CONNECT_TIMEOUT;
		int socketTimeout = DEFALUT_READ_TIMEOUT;
		int weight = 1;
		boolean isActive = true;
		for (int i = 1; i < strs.length; i++) {
			String str = strs[i];
			int idx = str.indexOf("=");
			if (idx < 0) {
				if ("down".equals(str)) {
					isActive = false;
				} else {
					throw new IllegalArgumentException("链接格式错误：" + url);
				}
				continue;
			}
			String s = str.substring(0, idx);
			String item = str.substring(idx + 1);
			if ("connect_timeout".equals(s)) {
				s_connectTimeout = item;
			} else if ("read_timeout".equals(s)) {
				s_socketTimeout = item;
			} else if ("weight".equals(s)) {
				s_weight = item;
			} else {
				throw new IllegalArgumentException("链接格式错误：" + url);
			}
		}
		if (null != s_connectTimeout) {
			try {
				connectTimeout = Integer.parseInt(s_connectTimeout);
			} catch (Exception e) {
				throw new IllegalArgumentException("无效超时值：" + connectTimeout);
			}
		}
		if (null != s_socketTimeout) {
			try {
				socketTimeout = Integer.parseInt(s_socketTimeout);
			} catch (Exception e) {
				throw new IllegalArgumentException("无效超时值：" + s_socketTimeout);
			}
		}
		if (null != s_weight) {
			try {
				weight = Integer.parseInt(s_weight);
			} catch (Exception e) {
				throw new IllegalArgumentException("无效权重值：" + s_weight);
			}
		}
		if (weight > 10) {
			throw new IllegalArgumentException("权重范围【1-10】：" + s_weight);
		}
		return Options.Builder.getBuilder(u).setConnectTimeout(connectTimeout)
				.setSocketTimeout(socketTimeout).setWeight(weight)
				.setActive(isActive).setOriginData(url).build();

	}

	public static void main(String[] args) {
		// String str1 =
		// "http://47.106.211.15:8080/resource;read_timeout=15;weight=1";
		String str2 = "https://www.google.com/;read_timeout=15;connect_timeout=10;weight=1";
		// String str1 =
		// "http://www.nzjie.cn;read_timeout=1;connect_timeout=1";
		List<String> list = new ArrayList<String>();
		list.add(str2);
		// list.add(str2);
		HttpInvoke invoke = getInstance(list);
		for (int i = 0; i < 1; i++) {
			ResponseResult res;
			try {
				res = invoke.invoke("", HttpInvoke.TYPE_POST,
						Parameter.valueOf("ip", "127.0.0.1"));
				if (null != res) {
					System.out.println(res.getData());
				}
			} catch (InvokeException e) {
				e.printStackTrace();
			}
		}

	}
}
