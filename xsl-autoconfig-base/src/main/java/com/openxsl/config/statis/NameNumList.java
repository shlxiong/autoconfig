package com.openxsl.config.statis;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class NameNumList extends ArrayList<NameNumber> {
	
	public NameNumList(List<NameNumber> data) {
		long total = 0;
		for (NameNumber nameNum : data) {
			total += nameNum.getNum();
		}
		for (NameNumber nameNum : data) {
			nameNum.setTotal(total);
		}
		this.addAll(data);
	}

}
