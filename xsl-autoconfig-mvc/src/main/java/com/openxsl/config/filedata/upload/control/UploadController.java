package com.openxsl.config.filedata.upload.control;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.openxsl.config.filedata.upload.model.FileItem;
import com.openxsl.config.filedata.upload.model.UploadConfig;
import com.openxsl.config.filedata.upload.service.UploadRegistry;
import com.openxsl.config.filedata.upload.service.UploadService;
import com.openxsl.config.util.HexEncoder;
import com.openxsl.config.util.StringUtils;
import com.openxsl.config.webmvc.Response;

/**
 * 上传文件
 * @author shuilin.xiong
 */
@Api(value="vmp-base", tags="上传下载")
@RestController
public class UploadController {
	private final Logger logger = Logger.getLogger(getClass());
	
	@Value("${spring.mvc.upload:/uploads}")
	private String uploadPath;
	@Autowired
	private UploadRegistry registry;
	
	private String storePath;
	private int rootLen = 0;
	
	/**
	 * 上传文件
	 * @param request  请求对象
	 * @param serviceId 服务ID
	 * @param password  服务密码
	 * @param dataId    业务主键
	 * @throws Exception
	 */
	@ApiOperation(value="上传文件", notes="综合管控,产业监测")
	@RequestMapping(value="/upload", method=RequestMethod.POST)
	@ResponseBody //不写会默认返回当前路径！！
	public Response upload(HttpServletRequest request, String serviceId, String password,
							String dataId) throws Exception {
		if (!(request instanceof MultipartHttpServletRequest)) {
            return new Response("没有上传任何文件");
        }
		
		if (storePath == null) {
			storePath = request.getServletContext().getRealPath(uploadPath) + File.separator;
			rootLen = request.getServletContext().getRealPath("/").length();
		}
		
		UploadConfig config = this.validateService(serviceId, password);
		UploadService service = registry.getUploadService(serviceId);
		StringBuilder failedFiles = new StringBuilder();
		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
		Iterator<String> itr = multiRequest.getFileNames();
		MultipartFile multiPart;
		Map<String,String> resultMap = new HashMap<String,String>();
		AtomicLong uploadSize = new AtomicLong(0L);
		while (itr.hasNext()) {
			final String name = itr.next();    //input-field name
			multiPart = multiRequest.getFile(name);
			long bytes = this.processServiceConfig(config, multiPart, password, uploadSize);
			try {
				String fileName = this.processFilename(config, multiPart);
				File file = new File(fileName);
				multiPart.transferTo(file);   //*****
				String relativePath = file.getCanonicalPath().substring(rootLen);
				if (StringUtils.isEmpty(config.getPassword())) {
					resultMap.put(name, relativePath);
				}
				if (service != null) { //登记一下
					request.setAttribute("fileSize", bytes);
					service.upload(name, relativePath, this.parseRequest(request));
				}
			} catch (Exception e) {
				failedFiles.append(multiPart.getOriginalFilename()).append(", ");
				logger.error("", e);
			}
		}

		if (failedFiles.length() > 0) {
			return new Response("部分文件上传失败："+failedFiles.substring(0,failedFiles.length()-2));
		}
		return Response.build(() -> resultMap);
	}
	
	@ApiOperation(value="下载文件", notes="综合管控,产业监测")
	@RequestMapping(value="/download", method=RequestMethod.POST)
	public ResponseEntity<Object> getFile(String serviceId, String password, 
						String name, String dataId) throws FileNotFoundException {
		this.validateService(serviceId, password);
		
		List<FileItem> items = registry.queryFileItems(serviceId, name, dataId);
		if (items!=null && items.size() > 0) {
			String fileName = storePath + items.get(0).getFilePath();
			return this.downloadFile(fileName);
		} else {
			throw new IllegalStateException("没有找到文件");
		}
	}
	
	/**
	 * 下载文件
	 * @param fileName 文件绝对路径
	 * @throws FileNotFoundException
	 */
	@ApiOperation(value="免费下载", notes="综合管控,产业监测")
	@RequestMapping(value = "/freeDown", method = {RequestMethod.GET,RequestMethod.POST})
	public ResponseEntity<Object> downloadFile(String fileName) throws FileNotFoundException {
	    File file = new File(fileName);
	    InputStreamResource resource = new InputStreamResource(new FileInputStream((file)));

	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Disposition",String.format("attachment;filename=\"%s\"",file.getName()));
	    headers.add("Cache-Control","no-cache,no-store,must-revalidate");
	    headers.add("Pragma","no-cache");
	    headers.add("Expires","0");

	    return ResponseEntity.ok().headers(headers)
                            .contentLength(file.length())
                            .contentType(MediaType.parseMediaType("application/text"))
                            .body(resource);
	}
	
	@ApiOperation(value="刷新配置", notes="综合管控,产业监测")
	@RequestMapping(value="/upload/refresh", method=RequestMethod.GET)
	public Response refreshConfig() {
		return Response.build(() ->	registry.refresh());
	}

	@ApiOperation(value = "查询图片根据时间和serviceId", notes = "综合管控，产业检测")
	@RequestMapping("queryImages")
	public Response queryImage(String beginDate, String endDate, String serviceId){
		UploadService service = registry.getUploadService(serviceId);
		return Response.build(() -> service.queryImages(serviceId, beginDate, endDate));
	}
	
	private long processServiceConfig(UploadConfig serviceConfig, MultipartFile multiPart,
					String password, AtomicLong uploadSize) throws IOException {
		String contentType = multiPart.getContentType();   //image/png
		String fileExt = FilenameUtils.getExtension(multiPart.getOriginalFilename());
		if (!serviceConfig.supports(contentType) || !serviceConfig.allowsFile(fileExt)) {
			throw new IllegalAccessError("不支持该文件类型："+fileExt);
		}
		
		long totalSize = serviceConfig.getMaxRequestSize();
		long eachSize = serviceConfig.getMaxFileSize();
		int fileSize = multiPart.getInputStream().available();
		long _SIZE = uploadSize.addAndGet(fileSize);
		uploadSize.set(_SIZE);
		if (eachSize > 0 && eachSize > fileSize) {
			throw new SecurityException("单个文件超过最大值");
		}
		if (totalSize > 0 && _SIZE > totalSize) {
			throw new SecurityException("文件累计超过最大值");
		}
		return fileSize;
	}
	@SuppressWarnings("deprecation")
	private String processFilename(UploadConfig serviceConfig, MultipartFile multiPart) {
		String path = serviceConfig.getBasePath();
		path = (path==null) ? storePath : (storePath + path);
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		
		String originalName = multiPart.getOriginalFilename();
		String baseName = FilenameUtils.getBaseName(originalName);
		String extension = FilenameUtils.getExtension(originalName);
		try {  //中文名有问题
			baseName = URLEncoder.encode(baseName, "UTF-8")
					+ '-' + System.nanoTime();
		} catch (UnsupportedEncodingException e) {
			baseName = URLEncoder.encode(baseName);
		}
		return String.format("%s/%s.%s", path,baseName,extension);
	}
	
	private UploadConfig validateService(String serviceId, String password) {
		UploadConfig config = registry.getUploadConfig(serviceId);
		if (config == null) {
			throw new IllegalArgumentException("服务名或密码错");
		}
		String configPwd = config.getPassword();
		if (!StringUtils.isEmpty(configPwd) && !HexEncoder.encode(configPwd).equals(password)) {
			throw new IllegalArgumentException("服务名或密码错");
		}
		return config;
	}
	
	private Map<String,?> parseRequest(HttpServletRequest request){
		Map<String,Object> attributes = new HashMap<String,Object>();
//		Enumeration<String> itr = request.getAttributeNames();
//		while (itr.hasMoreElements()) {
//			String name = itr.nextElement();
//			attributes.put(name, request.getAttribute(name));
//		}
		Enumeration<String> itr = request.getParameterNames();
		while (itr.hasMoreElements()) {
			String name = itr.nextElement();
			String[] values = request.getParameterValues(name);
			attributes.put(name, values.length<2?values[0]:values);
		}
		if (attributes.get("dataId") == null) {
			attributes.put("dataId", request.getAttribute("dataId"));
		}
		attributes.put("fileSize", request.getAttribute("fileSize"));
		return attributes;
	}
	
}
