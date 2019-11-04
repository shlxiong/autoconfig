package com.openxsl.config.logger.context;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author heyc
 */
public class IdUtil {

    private static AtomicReference<Integer> INDEXER = new AtomicReference<Integer>(0);

    private static final int MAX_INDEX_VAL = 9999;

    private static String serverIp = NetworkUtil.ip2HexString(NetworkUtil.getSiteIp());


    /**
     * 生成traceId
     * 服务器IP + 时间戳 + 序号 + 线程ID
     * @return
     */
    public static String getTraceId(){
        return serverIp + System.currentTimeMillis() + String.format("%04d",Thread.currentThread().getId()) + String.format("%04d",getIndex());
    }

    /**
     * getIndex
     * @return
     */
    private static Integer getIndex(){
        for (;;){
            Integer current = INDEXER.get();
            Integer next = (current == MAX_INDEX_VAL ? 0 : current + 1);
            if(INDEXER.compareAndSet(current,next)){
                return next;
            }
        }
    }

    public static void main(String[] args) {
        for (int i=0;i<100;i++){
            System.out.println(generateShortUuid());
        }
    }

    private static String[] chars = new String[] { "a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z" };

    public static String generateShortUuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            shortBuffer.append(chars[Integer.parseInt(uuid.substring(i * 4, i * 4 + 4), 16) % 0x3E]);
        }
        return shortBuffer.toString();
    }
}
