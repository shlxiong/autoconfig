package com.openxsl.config.statis;

import java.math.BigDecimal;

public interface NameValue {
	
public String getName();
	
	default void setLastValue(NameValue last) {}
	
	default <T> T getValue() { return null; }
	
	default Number getNum() { return null; }
	
	default String increaseRate(long thisNum, long lastNum) {
		if (lastNum != 0) {
			BigDecimal deta = new BigDecimal(thisNum-lastNum);
			return percent(deta, new BigDecimal(lastNum));
		}
		return "--%";
	}
	default String increaseRate(BigDecimal thisNum, BigDecimal lastNum) {
		return percent(thisNum.subtract(lastNum), lastNum);
	}
	
	static String percent(BigDecimal deta, BigDecimal total) {
		if (total.compareTo(BigDecimal.ZERO) == 0) {
			return "--%";
		} else if (BigDecimal.ZERO.compareTo(deta) == 0) {
			return "0.00%";
		}
		
		return deta.multiply(new BigDecimal(100.0)).divide(
						total.abs(), 2,  BigDecimal.ROUND_HALF_UP) + "%";
	}	
}

