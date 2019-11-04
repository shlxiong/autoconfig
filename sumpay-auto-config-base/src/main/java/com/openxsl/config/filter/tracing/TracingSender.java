package com.openxsl.config.filter.tracing;

import com.openxsl.config.filter.domain.InvocTrace;

public interface TracingSender {
	
	public void send(InvocTrace trace);

}
