package com.openxsl.admin.entity;

import javax.persistence.Column;
import javax.persistence.Table;

import com.openxsl.config.dal.jdbc.BaseEntity;
import com.openxsl.config.dal.jdbc.anno.InitialData;

@SuppressWarnings("serial")
@InitialData
@Table(name = "web_config")
public class WebConfig extends BaseEntity<Integer>{
	@Column
	private String name;
	@Column
	private String support;
	@Column
	private String mainTitle;
	@Column
	private String subTitle;
	@Column
	private String logo;
	@Column(length=64)
	private String homeUrl;
	@Column(length=64)
	private String loginUrl;
	@Column
	private String background;
	@Column
	private String webType;          //0.大屏显示配置 1.移动配置 2.管理平台配置
//	private String webSize;          //屏幕尺寸(0.3*3 1.3*4 2.3*5)
//	private String webDpi;           //屏幕分辨率
	@Column
	private String domain;
//	private String projectCode;
	@Column(length=64)
	private String controlUrl;        //总控平台地址
	
	//============================ TESB属性 ===============================//
	@Column
	private String serviceCode;       //TESB
	@Column(length=64)
	private String parkCodes;         //TESB景区统一编码，用于票务、微信等
	@Column(length=16)
	private String cityCode;          //TESB天气预报城市
	@Column
	private String externalCorpCode;  //TESB外部企业编码
	@Column
	private String corpCode;          //行政区划
	@Column
	private String esbUrl = "http://apiesb.zhiyoubao.com";
	@Column
	private String esbAppid;
	@Column
	private String esbAppkey;
	@Column
	private String esbName;              //功能编码
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSupport() {
		return support;
	}
	public void setSupport(String support) {
		this.support = support;
	}
	public String getMainTitle() {
		return mainTitle;
	}
	public void setMainTitle(String mainTitle) {
		this.mainTitle = mainTitle;
	}
	public String getSubTitle() {
		return subTitle;
	}
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getHomeUrl() {
		return homeUrl;
	}
	public void setHomeUrl(String homeUrl) {
		this.homeUrl = homeUrl;
	}
	public String getLoginUrl() {
		return loginUrl;
	}
	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}
	public String getBackground() {
		return background;
	}
	public void setBackground(String background) {
		this.background = background;
	}
	public String getWebType() {
		return webType;
	}
	public void setWebType(String webType) {
		this.webType = webType;
	}
//	public String getWebSize() {
//		return webSize;
//	}
//	public void setWebSize(String webSize) {
//		this.webSize = webSize;
//	}
//	public String getWebDpi() {
//		return webDpi;
//	}
//	public void setWebDpi(String webDpi) {
//		this.webDpi = webDpi;
//	}
	public String getCorpCode() {
		return corpCode;
	}
	public void setCorpCode(String corpCode) {
		this.corpCode = corpCode;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getControlUrl() {
		return controlUrl;
	}
	public void setControlUrl(String controlUrl) {
		this.controlUrl = controlUrl;
	}
	public String getServiceCode() {
		return serviceCode;
	}
	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}
	public String getParkCodes() {
		return parkCodes;
	}
	public void setParkCodes(String parkCodes) {
		this.parkCodes = parkCodes;
	}
	public String getCityCode() {
		return cityCode;
	}
	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}
	public String getExternalCorpCode() {
		return externalCorpCode;
	}
	public void setExternalCorpCode(String externalCorpCode) {
		this.externalCorpCode = externalCorpCode;
	}
	
}
