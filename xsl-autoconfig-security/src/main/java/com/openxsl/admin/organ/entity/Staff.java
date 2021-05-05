package com.openxsl.admin.organ.entity;

import io.swagger.annotations.ApiModelProperty;

import java.sql.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.openxsl.config.dal.jdbc.BaseEntity;

@Entity
@Table(name = "organ_staff_info")
@SuppressWarnings("serial")
public class Staff extends BaseEntity<Integer> {
//	@Column
//	private int id; 
	@Column
	@ApiModelProperty("用户ID")
	private String userId;
	@Column
	@ApiModelProperty("员工编号")
	private String staffNo;     //员工编号
	@Column
	@ApiModelProperty("姓名")
	private String name;
	@Column
	@ApiModelProperty("性别")
	private String gender;      //性别
	@Column
	@ApiModelProperty("出生日期")
//	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthday;      //出生日期
	@Column
	@ApiModelProperty("证件类型")
	private String cardType;    //证件
	@Column
	@ApiModelProperty("证件号码")
	private String cardNo;
	@Column
	@ApiModelProperty("联系地址")
	private String address;     //地址
	@Column
	@ApiModelProperty("手机号")
	private String mobile;      //手机
	@Column
	@ApiModelProperty("电话")
	private String telephone;
	@Column
	@ApiModelProperty("民族")
	private String nation;      //民族
	@Column
	@ApiModelProperty("政治面貌")
	private String politics;    //政治面貌
	@Column
	@ApiModelProperty("工作邮箱")
	private String email;       //工作邮箱
	@Column
	@ApiModelProperty("头像")
	private String logo;
	@Column
	@ApiModelProperty("照片")
	private String photo;       //照片
	@Column
	@ApiModelProperty("地区编码")
	private String areaCode;      //单位
	
	@Transient
	@ApiModelProperty("单位编码")
	private String corpCode;      //单位
	private List<String> subCorpCodes;
	private List<Integer> deptIds;
	private List<String> deptNames;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getStaffNo() {
		return staffNo;
	}
	public void setStaffNo(String staffNo) {
		this.staffNo = staffNo;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getNation() {
		return nation;
	}
	public void setNation(String nation) {
		this.nation = nation;
	}
	public String getPolitics() {
		return politics;
	}
	public void setPolitics(String politics) {
		this.politics = politics;
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
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public String getCorpCode() {
		return corpCode;
	}
	public void setCorpCode(String corpCode) {
		this.corpCode = corpCode;
	}
	public List<String> getSubCorpCodes() {
		return subCorpCodes;
	}
	public void setSubCorpCodes(List<String> subCorpCodes) {
		this.subCorpCodes = subCorpCodes;
	}
	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	public List<String> getDeptNames() {
		return deptNames;
	}
	public void setDeptNames(List<String> deptNames) {
		this.deptNames = deptNames;
	}
	public List<Integer> getDeptIds() {
		return deptIds;
	}
	public void setDeptIds(List<Integer> deptIds) {
		this.deptIds = deptIds;
	}
	
}
