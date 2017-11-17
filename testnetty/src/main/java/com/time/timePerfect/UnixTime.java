package com.time.timePerfect;

import java.io.Serializable;
import java.util.Date;

/**
 * 实体类封装 时间字节流
 *
 * @author HD
 * @date 2017/11/18
 */
public class UnixTime implements Serializable {

    private final long value;

    public UnixTime() {
        this(System.currentTimeMillis() / 1000L + 2208988800L);
    }

    public UnixTime(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new Date((getValue() - 2208988800L) * 1000L).toString()+"-----";
    }
}
