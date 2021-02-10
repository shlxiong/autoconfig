package com.openxsl.admin.organ.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.alibaba.fastjson.JSON;
import com.openxsl.config.dal.jdbc.BaseEntity;
import com.openxsl.config.dal.jdbc.anno.Index;
import com.openxsl.config.util.TreeView;
import com.openxsl.config.util.TreeView.TreeNode;
import com.openxsl.config.util.TreeView.UTreeNode;

@Entity
@Table(name = "organ_corporation")
@SuppressWarnings("serial")
public class Corporation extends BaseEntity<Integer> {
//	@Column
//	private int id; 
	@Column
	@Index
	private String code;
	@Column
	private String name;
	@Column(length=2)
	private Integer type;        //0-USER_SCENIC景区，1-USER_GOV政府
	@Column(length=4)
	private String level;       //级别
	@Column
	private String socialCode;  //社会组织编码
	@Column
	private String business;    //主营业务(duty)
	@Column(length=16)
	private String legalMan;    //法人
	@Column
	private String areaCode;
	@Column
	private String fax;
	@Column
	private String phone;
	@Column(length=16)
	private String linkMan;
	@Column(length=64)
	private String address;
	@Column
	private String website;
	@Column
	private String email;
	@Column
	private String logo;
	@Column
	@Index
	private Integer parentId;
	@Column
	private Integer seqNo;
	
	public String getNodeType() {
		return OrganTreeView.CORP;
	}
	public String getNodeId() {
		return TreeNode.getNodeId(String.valueOf(this.getId()), this.getNodeType());
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getSocialCode() {
		return socialCode;
	}
	public void setSocialCode(String socialCode) {
		this.socialCode = socialCode;
	}
	public String getBusiness() {
		return business;
	}
	public void setBusiness(String business) {
		this.business = business;
	}
	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getLinkMan() {
		return linkMan;
	}
	public void setLinkMan(String linkMan) {
		this.linkMan = linkMan;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public Integer getParentId() {
		return parentId;
	}
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	public String getLegalMan() {
		return legalMan;
	}
	public void setLegalMan(String legalMan) {
		this.legalMan = legalMan;
	}
	public Integer getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(Integer seqNo) {
		this.seqNo = seqNo;
	}
	
	public static void main(String[] args) {
		System.out.println(new Corporation().generDDLSql());
		Corporation corp = new Corporation();
		corp.setId(2);
		corp.setName("全球总公司");
		corp.setParentId(1);
		UTreeNode node = TreeView.toNode(corp);
		System.out.println(JSON.toJSONString(node));
	}

}