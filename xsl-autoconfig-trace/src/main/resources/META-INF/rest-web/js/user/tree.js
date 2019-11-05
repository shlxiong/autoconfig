
var contextPath = "rest/zkui";
var setting = {
	data : {
		simpleData : {
			enable : true,
			idKey : "id",
			pIdKey : "parentId",
			rootpId : "-1"
		},
		key:{
			name: "name",
			title:"fullPath"
		}
	},
//	check:{  
//	    enable: true,
//	    chkStyle:"checkbox",  //radio  
//	},
	edit : {
		enable : false,
		showRemoveBtn : true,
		showRenameBtn : true
	},
	callback : {
		onClick : zTreeOnClick
		//beforeRemove : zTreeBeforeRemove,
		//onRemove : zTreeOnRemove
	},

	async: {
		enable: true,
		url: contextPath + "/getTree",
		autoParam: ["fullPath=path"]          //表单提交，参数名为'path',值为fullpath属性
	}
		
};

//处理异步加载返回的节点属性信息
function ajaxDataFilter(treeId, parentNode, responseData) {
	if (responseData) {
		for ( var i = 0; i < responseData.length; i++) {
			if (responseData[i].parentId == 0) {
				responseData[i].isParent = "true";
			}
		}
	}
	return responseData;
};

function zTreeOnClick(event, treeId, treeNode) {
	treeObj.expandNode(treeNode);
	var path = treeNode.fullPath;
	$('#data').val('');
	$('#path').text(path);
	
	$.ajax({
		url : contextPath + "/getNodeInfo",
		data:{"path": path},
		type : "post",
		dataType : "json",
		success : function(data) {
			$('#data').val(data.data);
			$('#czxid').text(data.stat.czxid);
			$('#mzxid').text(data.stat.mzxid);
			$('#pzxid').text(data.stat.pzxid);
			$('#ctime').text(new Date(data.stat.ctime).format("yyyy-MM-dd HH:mm:ss"));
			$('#mtime').text(new Date(data.stat.mtime).format("yyyy-MM-dd HH:mm:ss"));
			$('#version').text(data.stat.version);
			$('#cversion').text(data.stat.cversion);
			$('#aversion').text(data.stat.aversion);
			$('#ephemeralOwner').text(data.stat.ephemeralOwner);
			$('#dataLength').text(data.stat.dataLength);
			$('#numChildren').text(data.stat.numChildren);
		}
	});
}

function update() {
	var path = $('#path').text();
	if (path && path != '') {
		if (confirm("确定要修改么？")) {
			$.ajax({
				url : contextPath + "/updatePathData",
				type : "post",
				dataType : "json",
				data: {"path":path, "data": $('#data').val()},
				success : function(data) {
					if (data.success){
						alert(data.content);
					} else {
						alert(data.content);
					} 
				}
			});
		}
	} else {
		alert("path不能为空！");
	}
}

function add() {
	var path = $('#add_search').val();
	if (path && path != '') {
		if (confirm("确定要添加么？")) {
			$.ajax({
				url : contextPath + "/addPath",
				type : "post",
				dataType : "json",
				data: {"path":path, "data": $('#add_data').val(), "flag":$('#flag').val()},
				success : function(data) {
					if (data.success){
						alert(data.content);
						loadTree();
					} else {
						alert(data.content);
					} 
				}
			});
		}
	} else {
		alert("path不能为空！");
	}
}

function deletePath() {
   var path = $('#path').text();
	if (path && path != '') {
		if (confirm("确定要删除么？")) {
			$.ajax({
				url : contextPath + "/deletePath",
				type : "post",
				dataType : "json",
				data: {"path":path},
				success : function(data) {
					if (data.success){
						alert(data.content);
						//清空节点及子节点
						var nodes = treeObj.getSelectedNodes();
						if (nodes && nodes.length>0) {
							treeObj.removeChildNodes(nodes[0]);
							treeObj.removeNode(nodes[0]);
						}
					} else {
						alert(data.content);
					} 
				}
			});
		}
	} else {
		alert("path不能为空！");
	}
}

function search() {
	var path = $('#add_search').val();
	if (path && path != '') {
		var nodes = treeObj.getNodesByParamFuzzy("name", path);
		for (var i = 0; i< nodes.length; i++) {
			treeObj.expandNode(nodes[i]);
		}
	} else {
		alert("请输入搜索内容！");
	}
}


var treeObj;
var tree;
function loadTree() {
	$.ajax({
		url : contextPath + "/getTree",
		type : "post",
		dataType : "json",
		success : function(data) {
			treeObj = $.fn.zTree.init($("#zkTree"), setting, data);
			var node = treeObj.getNodeByParam("id", 0, null);
			//treeObj.expandNode(node);
		}
	});
}


$(document).ready(function() {
	loadTree();
	//treeObj = $.fn.zTree.init($("#zkTree"), setting, data);
});

Date.prototype.format = function(fmt) {
    var date = {
    "M+" : this.getMonth() + 1,
    "d+" : this.getDate(),
    "h+" : this.getHours() % 12 == 0 ? 12 : this.getHours() % 12,
    "H+" : this.getHours(),
    "m+" : this.getMinutes(),
    "s+" : this.getSeconds()
    };
    if(/(y+)/.test(fmt)) {
        fmt=fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    for(var each in date) {
        if(new RegExp("(" + each + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (date[each]) : (("00" + date[each]).substr(("" + date[each]).length)));
        }
    }
    return fmt;
};