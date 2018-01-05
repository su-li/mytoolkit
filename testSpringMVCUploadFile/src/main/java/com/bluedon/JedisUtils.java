package com.bluedon;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author HD
 * @date. 2017/12/20
 */
public class JedisUtils {
    static JedisPool jedisPool;

    static {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(500);
        jedisPool = new JedisPool(jedisPoolConfig, "localhost", 6379);
    }

    /**
     * @return 返回一个Jedis连接实例
     */
    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

    /**
     * 释放连接
     *
     * @param jedis jedis连接实例
     */
    public static void close(Jedis jedis) {
        jedis.close();
    }

    public static void addNumberToSet(String key, String number) {

        Jedis resource = jedisPool.getResource();
        resource.sadd(key, number);
    }

    public static void addNumbersToSet(String key, String... numbers) {

        Jedis resource = jedisPool.getResource();
        resource.sadd(key, numbers);
    }

    public static void main(String[] args) {
        Jedis jedis = getJedis();
        jedis.hset("AAA", "1", "000");


        System.out.println(jedis.hexists("AAA", "2"));
    }
}
