package com.openxsl.config.dal.http;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Http工具类
 * 
 * @author shuilin.xiong
 */
public class OkHttpUtils {
	private static Logger logger = LoggerFactory.getLogger(OkHttpUtils.class);
	private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
	private static final MediaType XML = MediaType.get("text/xml; charset=utf-8");
	
	private static final OkHttpClient client;
	
	static {
		client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)
					.retryOnConnectionFailure(true)
					.readTimeout(15, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS)
					.build();
	}
	
	public static OkHttpClient getHttpClient() {
		return client;
	}
	
	/**
	 * GET请求
	 * @param url   目标地址
	 * @param headers 头信息
	 * @throws IOException
	 */
	public static String get(String url, String... headers) throws IOException {
		Request request = buildRequestWithHeaders(url, "get", null, headers);
		return execute(request);
	}
	
	/**
	 * POST表单请求
	 * @param url   目标地址
	 * @param argsMap  数据
	 * @throws IOException
	 */
	public static String postForm(String url, Map<String,Object> argsMap, String... headers)throws IOException {
		Request request = buildRequestWithHeaders(url, "post", argsMap, headers);
		return execute(request);
	}
	
	public static String delete(String url, String... headers)throws IOException {
		Request request = buildRequestWithHeaders(url, "delete", null, headers);
		return execute(request);
	}
	
	public static String put(String url, Map<String,Object> argsMap, String... headers)throws IOException {
        Request request = buildRequestWithHeaders(url, "put", argsMap, headers);
        return execute(request);
	}
	
	/**
	 * 异步GET请求
	 * @param url   目标地址
	 * @param succ  处理成功的类
	 * @param error 处理失败的类
	 */
	public void asyncRequest(String url, Consumer<Response> succ, Consumer<IOException> error) {
		Request request = new Request.Builder().url(url).build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				logger.error("请求失败：", e);
				if (error != null) {
					error.accept(e);
				}
			}
			@Override
			public void onResponse(Call call, Response resp) throws IOException {
				logger.error("请求结果：code={}, result={}", resp.code(), resp.body().string());
				if (succ != null) {
					succ.accept(resp);
				}
			}
		});
	}
	
	/**
	 * POST表单请求
	 * @param url   目标地址
	 * @param json  数据
	 * @throws IOException
	 */
	public static String postForm(String url, String... args)throws IOException {
		FormBody.Builder builder = new FormBody.Builder();
		for (int i=0,len=args.length; i<len; i++) {
			try {
				builder.add(args[i], args[++i]);
			} catch (IndexOutOfBoundsException ioe) {
				break;
			}
		}
		
		return doPost(url, builder.build());
	}
	
	/**
	 * POST + json请求
	 * @param url   目标地址
	 * @param json  数据
	 * @throws IOException
	 */
	public static String postJson(String url, String json, String... headers) throws IOException {
		RequestBody body = RequestBody.create(json, JSON);
//		return doPost(url, body);
		Request.Builder builder = new Request.Builder().url(url);
		for (int i=0,len=headers.length; i<len; i++) {
			try {
				builder.header(headers[i], headers[++i]);  //key,value
			} catch (IndexOutOfBoundsException ioe) {
				break;
			}
		}
		builder.post(body);
		return execute(builder.build());
		
	}
	
	/**
	 * POST + XML请求
	 * @param url   目标地址
	 * @param json  数据
	 * @throws IOException
	 */
	public static String postXml(String url, String xml) throws IOException {
		RequestBody body = RequestBody.create(xml, XML);
		return doPost(url, body);
	}
	
	private static Request buildRequestWithHeaders(String url, String method,
					Map<String,Object> argsMap, String... headers) {
		Request.Builder builder = new Request.Builder().url(url);
		RequestBody body = buildRequestBody(argsMap);
		if ("post".equalsIgnoreCase(method)) {
			builder.post(body);
		} else if ("delete".equalsIgnoreCase(method)) {
			builder.delete(body);
		} else if ("put".equalsIgnoreCase(method)) {
			builder.put(body);
		}
		
		for (int i=0,len=headers.length; i<len; i++) {
			try {
				builder.header(headers[i], headers[++i]);  //key,value
			} catch (IndexOutOfBoundsException ioe) {
				break;
			}
		}
		return builder.build();
	}
	private static FormBody buildRequestBody(Map<String,Object> argsMap) {
		FormBody.Builder builder = new FormBody.Builder();
		if (argsMap != null) {
			for (Map.Entry<String, Object> entry : argsMap.entrySet()) {
				try {
					builder.add(entry.getKey(), entry.getValue().toString());
				} catch (IndexOutOfBoundsException ioe) {
					break;
				}
			}
		}
		return builder.build();
	}
	
	private static String execute(Request request) throws IOException {
		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
	            throw new IOException("请求失败：" + response);
	        }
			return response.body().string();
		}
	}
	
	private static String doPost(String url, RequestBody body) throws IOException {
		Request.Builder builder = new Request.Builder().url(url);
		builder.post(body);
		return execute(builder.build());
	}
	
}
