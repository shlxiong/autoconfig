package com.openxsl.config.statis;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class NameNumList extends ArrayList<NameNumber> {
	private long total;
	private long lastTotal;
	
	public NameNumList(List<NameNumber> data) {
		long total = 0, lastTotal = 0;
		for (NameNumber nameNum : data) {
			total += nameNum.getNum();
			lastTotal += nameNum.getLastNum();
		}
		this.total = total;
		this.lastTotal = lastTotal;
		for (NameNumber nameNum : data) {
			nameNum.setTotal(total);
		}
		this.addAll(data);
	}
	public NameNumList(List<NameNumber> data, boolean appendTotal) {
		this(data);
		if (appendTotal) {
			NameNumber totalElt = new NameNumber("Total", this.getTotal());
			totalElt.setLastNum(lastTotal);
			this.add(totalElt);
		}
	}
	
	public long getTotal() {
		return total;
	}
	public long getLastTotal() {
		return lastTotal;
	}

}
