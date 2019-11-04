package com.openxsl.config.test;

import java.sql.Date;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

import junit.framework.TestCase;

public class JsonTest extends TestCase{
	
	@Test
	public void testFastJson() {
		try {
			String jsonStr = JSON.toJSONString(new Model());
			System.out.println("json="+jsonStr);
			JSON.parseObject(jsonStr, Model.class);
		}catch(Throwable e) {
			e.printStackTrace();
			throw new RuntimeException("");
		}
	}
	
	public static class Model{
		int id;
		Date birthday = new Date(System.currentTimeMillis());
		String f1 = "f1";
		String f2 = "f2";
		String f3 = "f3";
		String f4 = "f4";
		String f5 = "f5";
		String f6 = "f6";
		String f7 = "f7";
		String f8 = "f8";
		String f9 = "f9";
		String f10 = "f10";
		String f11;
		String f12;
		String f13;
		String f14;
		String f15;
		String f16;
		String f17;
		String f18;
		String f19;
		String f20;
		String f21;
		String f22;
		String f23;
		String f24;
		String f25;
		String f26;
		String f27;
		String f28;
		String f29;
		String f30;
//		String f31;
//		String f32;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public Date getBirthday() {
			return birthday;
		}
		public void setBirthday(Date birthday) {
			this.birthday = birthday;
		}
		public String getF1() {
			return f1;
		}
		public void setF1(String f1) {
			this.f1 = f1;
		}
		public String getF2() {
			return f2;
		}
		public void setF2(String f2) {
			this.f2 = f2;
		}
		public String getF3() {
			return f3;
		}
		public void setF3(String f3) {
			this.f3 = f3;
		}
		public String getF4() {
			return f4;
		}
		public void setF4(String f4) {
			this.f4 = f4;
		}
		public String getF5() {
			return f5;
		}
		public void setF5(String f5) {
			this.f5 = f5;
		}
		public String getF6() {
			return f6;
		}
		public void setF6(String f6) {
			this.f6 = f6;
		}
		public String getF7() {
			return f7;
		}
		public void setF7(String f7) {
			this.f7 = f7;
		}
		public String getF8() {
			return f8;
		}
		public void setF8(String f8) {
			this.f8 = f8;
		}
		public String getF9() {
			return f9;
		}
		public void setF9(String f9) {
			this.f9 = f9;
		}
		public String getF10() {
			return f10;
		}
		public void setF10(String f10) {
			this.f10 = f10;
		}
		public String getF11() {
			return f11;
		}
		public void setF11(String f11) {
			this.f11 = f11;
		}
		public String getF12() {
			return f12;
		}
		public void setF12(String f12) {
			this.f12 = f12;
		}
		public String getF13() {
			return f13;
		}
		public void setF13(String f13) {
			this.f13 = f13;
		}
		public String getF14() {
			return f14;
		}
		public void setF14(String f14) {
			this.f14 = f14;
		}
		public String getF15() {
			return f15;
		}
		public void setF15(String f15) {
			this.f15 = f15;
		}
		public String getF16() {
			return f16;
		}
		public void setF16(String f16) {
			this.f16 = f16;
		}
		public String getF17() {
			return f17;
		}
		public void setF17(String f17) {
			this.f17 = f17;
		}
		public String getF18() {
			return f18;
		}
		public void setF18(String f18) {
			this.f18 = f18;
		}
		public String getF19() {
			return f19;
		}
		public void setF19(String f19) {
			this.f19 = f19;
		}
		public String getF20() {
			return f20;
		}
		public void setF20(String f20) {
			this.f20 = f20;
		}
		public String getF21() {
			return f21;
		}
		public void setF21(String f21) {
			this.f21 = f21;
		}
		public String getF22() {
			return f22;
		}
		public void setF22(String f22) {
			this.f22 = f22;
		}
		public String getF23() {
			return f23;
		}
		public void setF23(String f23) {
			this.f23 = f23;
		}
		public String getF24() {
			return f24;
		}
		public void setF24(String f24) {
			this.f24 = f24;
		}
		public String getF25() {
			return f25;
		}
		public void setF25(String f25) {
			this.f25 = f25;
		}
		public String getF26() {
			return f26;
		}
		public void setF26(String f26) {
			this.f26 = f26;
		}
		public String getF27() {
			return f27;
		}
		public void setF27(String f27) {
			this.f27 = f27;
		}
		public String getF28() {
			return f28;
		}
		public void setF28(String f28) {
			this.f28 = f28;
		}
		public String getF29() {
			return f29;
		}
		public void setF29(String f29) {
			this.f29 = f29;
		}
		public String getF30() {
			return f30;
		}
		public void setF30(String f30) {
			this.f30 = f30;
		}
//		public String getF31() {
//			return f31;
//		}
//		public void setF31(String f31) {
//			this.f31 = f31;
//		}
//		public String getF32() {
//			return f32;
//		}
//		public void setF32(String f32) {
//			this.f32 = f32;
//		}
		
	}

}
