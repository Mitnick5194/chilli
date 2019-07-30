package com.ajie.chilli.http;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import com.ajie.chilli.http.UrlWrap.WeightRang;
import com.ajie.chilli.http.impl.ResponseWrap;
import com.ajie.chilli.utils.common.StringUtils;

/**
 * http调用器，url一般不包含uri，只有在调用的时候才会指定调用哪个uri
 * 
 * @author niezhenjie
 *
 */
public class HttpInvoke {

	/** 链接信息 */
	protected List<UrlWrap> urls;

	/** 最后一次指向的链接 */
	protected int lastCursor;

	/** http链接池配置 */
	private PoolingHttpClientConnectionManager connManager;

	/** 默认最大链接数 */
	public final int DEFAULT_MAXTOTAL = 200;
	/** 默认的每个路由的最大连接数 */
	public final int DEFAULT_MAXPERROUTE = 200;

	/** 请求类型 -- get */
	public final int TYPE_GET = 1;
	/** 请求类型 -- post */
	public final int TYPE_POST = 2;

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
		List<UrlWrap> list = new ArrayList<UrlWrap>(urls.size());
		for (String url : urls) {
			if (StringUtils.isEmpty(url)) {
				throw new NullPointerException("调用链接为空：" + url);
			}
			UrlWrap wrap = parse(url);
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
		List<UrlWrap> list = new ArrayList<UrlWrap>(urls.size()
				+ this.urls.size());
		for (String url : urls) {
			if (StringUtils.isEmpty(url)) {
				throw new NullPointerException("调用链接为空：" + url);
			}
			UrlWrap wrap = parse(url);
			list.add(wrap);
		}
		for (UrlWrap url : this.urls) {
			boolean had = false;
			for (UrlWrap uw : list) {
				if (url.getUrl().equals(uw.getUrl())) {
					had = true;
				}
			}
			// 已经有了，使用新的覆盖掉原来的
			if (had) {
				continue;
			}
			list.add(url);

		}
		this.urls = list;
		handleWeightRange();
		return true;
	}

	public Response invoke(String uri, Parameter... params) {
		return doGet(uri, params);
	}

	public Response invoke(String uri, int type, Parameter... params) {
		return null;
	}

	private Response doGet(String uri, Parameter... params) {
		UrlWrap wrap = getNext();
		HttpClient client = get(wrap);
		String url = wrap.getUrl();
		System.out.println("当前链接：" + url);
		URIBuilder builder = null;
		HttpGet get = null;
		HttpResponse res = null;
		try {
			builder = new URIBuilder(genUrl(url, uri));
			for (Parameter p : params) {
				builder.addParameter(p.getKey(), p.getValue());
			}
			get = new HttpGet(builder.build());
			client.execute(get);
			res = client.execute(get);
			String result = null;
			if (res.getStatusLine().getStatusCode() == StatusCode.SC_OK) {
				result = EntityUtils.toString(res.getEntity(), "utf-8");
			}
			ResponseWrap response = new ResponseWrap(res.getStatusLine()
					.getStatusCode());
			response.setMsg(result);
			return response;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String genUrl(String url, String uri) {
		if (!url.endsWith("/")) {
			url += "/";
		}
		return url + uri;
	}

	private HttpClient get(UrlWrap url) {
		int connectT = url.getConnectTimeout();
		int socketT = url.getSocketTimeout();
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
	private UrlWrap getNext() {
		List<UrlWrap> list = this.urls;
		// 检查是否全部不活跃
		int activeCount = 0;
		for (UrlWrap u : list) {
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
		UrlWrap find = null;
		for (int i = 0; i < list.size(); i++) {
			UrlWrap ww = list.get(i);
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
		List<UrlWrap> list = new ArrayList<UrlWrap>(urls.size());
		invoke.urls = list;
		for (String url : urls) {
			if (StringUtils.isEmpty(url)) {
				throw new NullPointerException("调用链接为空：" + url);
			}
			UrlWrap wrap = parse(url);
			list.add(wrap);
		}
		invoke.handleWeightRange();
		return invoke;
	}

	private void sort() {
		synchronized (urls) {
			Collections.sort(urls, new Comparator<UrlWrap>() {
				@Override
				public int compare(UrlWrap o1, UrlWrap o2) {
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
		List<UrlWrap> list = this.urls;
		if (list.size() <= 1) {// 一个就不需要处理了吧
			return;
		}
		int start = 0;
		for (UrlWrap wrap : list) {
			UrlWrap.WeightRang rang = UrlWrap.WeightRang.valueOf(start + 1,
					start + wrap.getWeight());
			start += wrap.getWeight();
			wrap.setWeightRang(rang);
		}
	}

	static private UrlWrap parse(String url) {
		String[] strs = url.split(";");
		String u = strs[0];
		HttpUtils.assertHttpProtocol(u);
		String s_connectTimeout = null;
		String s_socketTimeout = null;
		String s_weight = null;
		int connectTimeout = -1;
		int socketTimeout = -1;
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
			} else if ("socket_timeout".equals(s)) {
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
			throw new IllegalArgumentException("权重范围【1-100】：" + s_weight);
		}
		return UrlWrap.Builder.getBuilder(u).setConnectTimeout(connectTimeout)
				.setSocketTimeout(socketTimeout).setWeight(weight)
				.setActive(isActive).setOriginData(url).build();

	}

	public static void main(String[] args) {
		String str1 = "http://47.106.211.15:8080/resource;socket_timeout=15;weight=1";
		String str2 = "http://xylx.nzjie.cn/resource;socket_timeout=15;weight=1";
		List<String> list = new ArrayList<String>();
		list.add(str1);
		list.add(str2);
		HttpInvoke invoke = getInstance(list);
		for (int i = 0; i < 5; i++) {
			Response response2 = invoke.invoke("queryIp.do",
					Parameter.valueOf("ip", "127.0.0.1"));
			System.out.println(response2);
		}

	}
}
