package com.openxsl.config.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.alibaba.fastjson.JSON;

/**
 * Gis树结构，子节点直接加在children结构中，用法：
 * <prev>
 * 
 *     GisTreeView tree = new GisTreeView(data);
 *     tree.addChildren(parentId, children);
 *     
 * </prev>
 * @author shuilin.xiong
 */
public class GisTreeView {
	private List<GisTreeNode> nodes = new ArrayList<GisTreeNode>();
	
	public static GisTreeNode toNode(Object data) {
		if (data==null || data instanceof GisTreeNode) {
			return (GisTreeNode)data;
		}
		return JSON.parseObject(JSON.toJSONString(data), GisTreeNode.class);
	}
	private static List<GisTreeNode> toNodeList(List<?> data){
		if (data == null || data.size() < 1) {
			return Collections.emptyList();
		}
		List<GisTreeNode> lstNode = new ArrayList<GisTreeNode>(data.size());
		for (Object elt : data) {
			lstNode.add( toNode(elt) );
		}
		return lstNode;
	}
	
	public GisTreeView() {}
	
	public GisTreeView(List<?> data) {
		nodes = toNodeList(data);
	}
	
	public void addChildren(final String parentId, List<?> data) {
		List<GisTreeNode> children = toNodeList(data);
		if (children.size() > 0) {
			if (parentId == null) {
				nodes.addAll(children);
				return;
			}
			for (GisTreeNode node : nodes) {
				if (parentId.equals(node.getId())) {
					List<GisTreeNode> list = Optional.ofNullable(node.getChildren())
							.orElse(new ArrayList<GisTreeNode>());
					list.addAll(children);
					return;
				}
			}
		}
	}
	
	public List<GisTreeNode> getNodes() {
		return nodes;
	}
	public void setNodes(List<GisTreeNode> nodes) {
		this.nodes = nodes;
	}

	public static class GisTreeNode {
		private String id;
		private String title;
		private String type;
		private String url;
		private boolean location;  //是否需要定位
		private String lng;    //经度
		private String lat;    //维度
		private String state;      //地图点位状态：0-常态，1-hover颜色，2-红色
		private List<GisTreeNode> children;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public boolean isLocation() {
			return location;
		}

		public void setLocation(boolean location) {
			this.location = location;
		}

		public String getLng() {
			return lng;
		}

		public void setLng(String lng) {
			this.lng = lng;
		}

		public String getLat() {
			return lat;
		}

		public void setLat(String lat) {
			this.lat = lat;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		public List<GisTreeNode> getChildren() {
			return children;
		}

		public void setChildren(List<GisTreeNode> children) {
			this.children = children;
		}
		
	}
	
	public static class GisBlockInfor{
		private String title;   //标题
		private String type;    //类别
		private String type2;   //类别2
		private List<String> images;  //显示图片
		private List<?> videos;       //视频对象
		private List<NameValue> categories = new ArrayList<NameValue>();  //显示属性
		private String objectId;      //对象ID
		
		public void addCategory(String name, Object value) {
			categories.add(new NameValue(name,value));
		}
		
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public List<String> getImages() {
			return images;
		}

		public void setImages(List<String> images) {
			this.images = images;
		}

		public List<?> getVideos() {
			return videos;
		}

		public void setVideos(List<?> videos) {
			this.videos = videos;
		}

		public List<NameValue> getCategories() {
			return categories;
		}

		public void setCategories(List<NameValue> categories) {
			this.categories = categories;
		}

		public String getObjectId() {
			return objectId;
		}

		public void setObjectId(String id) {
			this.objectId = id;
		}

		public String getType2() {
			return type2;
		}

		public void setType2(String type2) {
			this.type2 = type2;
		}

		static class NameValue {
			String name;
			Object value;
			
			NameValue(){}
			NameValue(String name, Object value){
				this.setName(name);
				this.setValue(value);
			}
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public Object getValue() {
				return value;
			}
			public void setValue(Object value) {
				this.value = value;
			}
		}
	}

}
