package com.xlfc.cluster.loadBalance;



import com.xlfc.common.config.RpcRequest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


public class RandomLoadBalance extends AbstractLoadBalance {
    private final int replicaCount = 3;
    private final SortedMap<Long, String> ring = new TreeMap<>();

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        for(String node : serviceAddresses){
            for (int i = 0; i < replicaCount; i++) {
                long hash = md5Hash(node + i);
                ring.put(hash, node);
            }
        }
        long hash = md5Hash(rpcRequest.getRequestId());
        if (!ring.containsKey(hash)) {
            //tailMap返回所有大于等于Key的键值对
            SortedMap<Long, String> tailMap = ring.tailMap(hash);
            hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        }

        return ring.get(hash);
    }

    private long md5Hash(String key){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        //reset重置
        md.reset();
        //update使用指定的字节更新摘要
        md.update(key.getBytes());
        //通过执行诸如填充之类的最终操作完成哈希计算。
        byte[] digest = md.digest();
        long hash = ((long) (digest[3] & 0xFF) << 24)
                | ((long) (digest[2] & 0xFF) << 16)
                | ((long) (digest[1] & 0xFF) << 8)
                | (digest[0] & 0xFF);
        return hash & 0xFFFFFFFFL;
    }
}
