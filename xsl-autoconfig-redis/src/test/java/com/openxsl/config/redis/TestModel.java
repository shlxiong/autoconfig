package com.openxsl.config.redis;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TestModel implements Serializable{
	private String username;
	private String password;
	private int age;
	private java.sql.Date workDate;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public java.sql.Date getWorkDate() {
		return workDate;
	}
	public void setWorkDate(java.sql.Date workDate) {
		this.workDate = workDate;
	}
}
