package com.xlfc.registry.zk;

import com.xlfc.common.config.RpcConstants;
import com.xlfc.common.exception.RpcException;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class ZookeeperUtils {
    private static Logger log = LoggerFactory.getLogger(ZookeeperUtils.class);


    private static CuratorFramework zkClient;

    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();

    private static final Map<String,List<String>> SERVICE_ADDRESS_MAP=new ConcurrentHashMap<>();

    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (!(REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null)) {
                //mode表示确定节点类型，forpath可以加入数据，如果没有加入则默认存储ip地址
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create persistent node for path [{}] fail", path);
        }
    }

    /**
     * 返回连接，如果没有建立连接则进行建立
     */
    public static CuratorFramework getZkClient() {
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        //指定间隔时间重试
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(100, 3);
        /**
         * connectString ：zk server 的地址和端口，多个用逗号隔开
         * retryPolicy ： 重试策略
         */
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(RpcConstants.DEFAULT_ZOOKEEPER_ADDRESS)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RpcException("zk连接超时");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }

    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result=null;
        String servicePath= RpcConstants.ZK_REGISTER_ROOT_PATH+"/"+rpcServiceName;
        try {
            //这里是查询子节点，每个子节点命名即url。还可以查询节点数据，查询节点。
            result=zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName,result);
            registerWatcher(rpcServiceName,zkClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //watch实际上实现了发布/订阅功能
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath= RpcConstants.ZK_REGISTER_ROOT_PATH+"/"+rpcServiceName;
        //pathChildrenCache监听某一个节点的子节点，另外treeCache监听树上的所有节点
        PathChildrenCache pathChildrenCache=new PathChildrenCache(zkClient, servicePath, true);
        //监听，随时往servicemap中加入新的节点
        PathChildrenCacheListener pathChildrenCacheListener=(curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses=curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName,serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }

}
