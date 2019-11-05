package com.openxsl.tracing.registry.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openxsl.config.dal.zookeeper.ZkNodeData;
import com.openxsl.config.dal.zookeeper.ZooKeeperTemplate;
import com.openxsl.tracing.registry.web.domain.TreeNode;

@Path("zkui")
public class ZooUIRestApi {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final String ROOT = "/";
	private ZooKeeperTemplate registry;
	  
	public ZooUIRestApi() {
	    this.registry = ((ZooKeeperTemplate)ConsoleBootstrap.getBean(null, ZooKeeperTemplate.class));
	    this.registry.setRootPath("/");
	}
	  
	@POST
	@Path("getTree")
	@Produces({"application/json"})
	public List<TreeNode> getTree(@FormParam("path") String path) throws Exception {
	    List<TreeNode> treeList = new ArrayList<TreeNode>();
	    if (path == null) {
	    	TreeNode tree = new TreeNode();
	    	tree.setId(Integer.valueOf(0));
	    	tree.setName(ROOT.substring(1));
	    	tree.setParentId(Integer.valueOf(-1));
	    	tree.setFullPath(ROOT);
	    	tree.setIsParent("true");
	    	tree.setOpen(true);
	    	treeList.add(tree);
	    	path = ROOT;
	    }
	    this.getChildren(path, treeList);
	    return treeList;
	}
	
	@POST
	@Path("getNodeInfo")
	public ZkNodeData getNodeInfo(@FormParam("path") String path) throws IOException {
	    return this.registry.getWithStat(path);
	}
	  
	@POST
	@Path("deletePath")
	public AjaxMessage deletePath(String path) {
	    AjaxMessage msg = new AjaxMessage(true, "删除成功!");
	    try {
	    	if (this.registry.exists(path)) {
	    		this.registry.delete(path);
	    	} else {
		        msg.setSuccess(false);
		        msg.setContent("该节点不存在!");
	    	}
	    } catch (Exception e) {
	    	this.logger.error("", e);
	    	msg.setSuccess(false);
	    	msg.setContent("服务端异常，" + e.getMessage());
	    }
	    return msg;
	}
	  
	@POST
	@Path("updatePathData")
	public AjaxMessage updatePathData(String path, String data) {
		AjaxMessage msg = new AjaxMessage(true, "修改成功!");
		try {
		    if (registry.exists(path)) {
		        registry.save(path, data);
		    } else {
		        msg.setSuccess(false);
		        msg.setContent("无此节点信息!");
		    }
		} catch (Exception e) {
	    	logger.error("", e);
	    	msg.setSuccess(false);
	    	msg.setContent("服务端异常");
	    }
	    return msg;
	  }
	  
	private void getChildren(String path, List<TreeNode> treeList) throws Exception {
		if ("/zookeeper".equals(path)) {
			return;
		}
	    int parentId = "/".equals(path) ? 0 : path.hashCode();
	    String format = "/".equals(path) ? "/%s" : (path+"/%s");
	    for (String subPath : this.registry.getChildren(path)) {
	    	if ("zookeeper".equals(subPath)) {
	    		continue;
	    	}
	    	String newPath = String.format(format, subPath);
	    	TreeNode node = new TreeNode();
	    	node.setId(Integer.valueOf(newPath.hashCode()));
	    	node.setParentId(Integer.valueOf(parentId));
	    	node.setName(subPath);
	    	node.setFullPath(newPath);
	    	if (this.registry.countChildren(newPath) > 0) {
	    		node.setIsParent("true");
	    	} else {
	    		node.setIsParent("false");
	    	}
	    	treeList.add(node);
	    }
	}
	  
	public class AjaxMessage {
	    private boolean success;
	    private String content;
	    
	    public AjaxMessage(boolean success, String content) {
	    	setSuccess(success);
	    	setContent(content);
	    }
	    
	    public boolean isSuccess() {
	    	return this.success;
	    }
	    
	    public void setSuccess(boolean isSuccess) {
	    	this.success = isSuccess;
	    }
	    
	    public String getContent() {
	    	return this.content;
	    }
	    
	    public void setContent(String content) {
	    	this.content = content;
	    }
	}

}
