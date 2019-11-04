package com.openxsl.config.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ID生成器
 * @author xiongsl
 */
public class IDGenerator {
	public static final char[] RADIX64 = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
			'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'+', '/'
	};
	private static AtomicReference<Integer> INDEXER = new AtomicReference<Integer>(0);
    private static final int MAX_INDEX_VAL = 4096;
    private static final String HEX_LOCAL_IP;
    
    static {
    	Object[] ips = new Integer[4];
		int i = 0;
		for (String segment : NetworkUtils.LOCAL_IP.split("\\.")) {
			ips[i++] = Integer.valueOf(segment);
		}
		HEX_LOCAL_IP = String.format("%02x%02x%02x%02x", ips);
    }

	/**
	 * UUID经过编码后成24位
	 * UUID: 8-4-4-4-12分别是：当前日期时间-时钟序列-IEEE机器识别号(Mac)
	 */
	public static String getUUID() {
		String uuid = UUID.randomUUID().toString().replace('-', '0');
		return hex2Radix64(uuid);
	}
	/**
	 * 16位序列号
	 * 十六进制序列号：（IP[8]+时间[13]+序号[3]）
	 */
	public static String getTraceID() {
		String sequence = HEX_LOCAL_IP + Long.toHexString(System.nanoTime())
				+ String.format("%03x", getIndex());
		return hex2Radix64(sequence);
	}
	public static String hex2Radix64(String hexStr) {
		StringBuilder target = new StringBuilder();
		String child, tempStr = hexStr;
		int mod = hexStr.length() % 6;
		int len = (mod==0) ? 0 : (6-mod);
		for (int i = 0; i<len; i++) {
			tempStr = '0' + tempStr;
		}
		while (tempStr.length() >= 6) {
			child = tempStr.substring(tempStr.length()-6);
			tempStr = tempStr.substring(0, tempStr.length()-6);
			int decimal = Integer.parseInt(child, 16);
			//每6位转换成4位64进制
			for (int i=0; i<4; i++) {
				final int bit = decimal % 64;
				target.insert(0, RADIX64[bit]);
				decimal = decimal / 64;
			}
		}
		
		//把前面的补位‘0’去掉
		int idx = 0;
		while (target.charAt(idx) == '0') {
			idx ++;
		}
		return target.substring(idx);
	}
	
	private static Integer getIndex(){
        for (;;){
            Integer current = INDEXER.get();
            Integer next = (current == MAX_INDEX_VAL ? 0 : current + 1);
            //相等时Set，并返回true
            if (INDEXER.compareAndSet(current,next)){
                return next;
            }
        }
    }
	
	public static void main(String[] args) {
		for (int i=0; i<100; i++) {
			System.out.println(getUUID());
			System.out.println(getTraceID());
		}
	}

}
