package com.openxsl.admin.entity;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;

@Entity
@Table(name = "admin_user_detail")
@SuppressWarnings("serial")
public class UserDetail extends BaseEntity<Integer> {
//	@Column
//	private int id;             //User.id
	@Column
	private String realName;    //姓名
	@Column
	private String gender;      //性别
	@Column
	private Date birthday;      //出生日期
	@Column
	private String cardType;    //证件
	@Column
	private String cardNo;
	@Column
	private String address;     //地址
	@Column
	private String mobile;      //手机
	@Column
	private String telephone;
	@Column
	private String nation;      //民族
	@Column
	private String politics;    //政治面貌
	@Column
	private String logo;
	
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
	
	public static void main(String[] args) {
		String sql = new UserDetail().generDDLSql();
		System.out.println(sql);
	}

}
