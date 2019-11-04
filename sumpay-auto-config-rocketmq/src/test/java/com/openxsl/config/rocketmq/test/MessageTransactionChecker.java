package com.openxsl.config.rocketmq.test;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionCheckListener;
import org.apache.rocketmq.common.message.MessageExt;

public class MessageTransactionChecker implements TransactionCheckListener {

	@Override
	public LocalTransactionState checkLocalTransactionState(MessageExt msg) {
		Integer seq = Integer.parseInt(msg.getUserProperty("sequence"));
		LocalTransactionState state = (seq.intValue()%6 == 5)
				? LocalTransactionState.ROLLBACK_MESSAGE
				: LocalTransactionState.COMMIT_MESSAGE;
		System.out.println("TransactionCheckListener======seq: "+seq+", tx="+state);
		return state;
	}

}
