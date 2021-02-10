package com.openxsl.admin.entity;

import io.swagger.annotations.ApiModelProperty;

import java.sql.Date;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.openxsl.config.dal.jdbc.BaseEntity;
import com.openxsl.config.dal.jdbc.anno.Index;

@Entity
@Table(name = "admin_user_detail")
@SuppressWarnings("serial")
public class UserDetail extends BaseEntity<Integer> {
//	@Column
//	private int id;             //User.id
	@Column
	@Index
	@ApiModelProperty("用户ID")
	private String userId;
	@Column
	@ApiModelProperty("姓名")
	private String realName;    //姓名
	@Column(length=4)
	@ApiModelProperty("性别")
	private String gender;      //性别
	@Column
	@ApiModelProperty("出生日期")
	private Date birthday;      //出生日期
	@Column
	@ApiModelProperty("证件类型")
	private String cardType;    //证件
	@Column
	@ApiModelProperty("证件号码")
	private String cardNo;
	@Column(length=64)
	@ApiModelProperty("联系地址")
	private String address;     //地址
	@Column
	@ApiModelProperty("手机号")
	private String mobile;      //手机
	@Column
	@ApiModelProperty("联系电话")
	private String telephone;
	@Column
	@ApiModelProperty("民族")
	private String nation;      //民族
	@Column
	@ApiModelProperty("政治面貌")
	private String politics;    //政治面貌
	@Column(length=64)
	@ApiModelProperty("头像")
	private String logo;
	
	private String username;
	private String email;
	private boolean disabled;         //true:禁用, false:启用
	private String domain;
	private int userType;
	private String encodeName;    //userName的加密
	
	@JsonIgnore
	@SuppressWarnings("deprecation")
	public int getAge() {
		return Calendar.getInstance().get(Calendar.YEAR) - birthday.getYear();
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
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
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
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
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public int getUserType() {
		return userType;
	}
	public void setUserType(int userType) {
		this.userType = userType;
	}
	public String getEncodeName() {
		return encodeName;
	}
	public void setEncodeName(String encodeName) {
		this.encodeName = encodeName;
	}

}
