package com.study.config;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * @ClassName: WatchCallback
 * @Description: 配置获取类
 * @Author: ynf
 * @Date: 2022-03-04 7:38
 * @Version: 1.0
 */
public class ConfigUtils implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback{

    private MyConfig myConfig;

    private ZooKeeper zk;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public MyConfig getMyConfig() {
        return myConfig;
    }

    public void setMyConfig(MyConfig myConfig) {
        this.myConfig = myConfig;
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    // 异步方式（callback）判断节点是否存在，同时使用watcher监听节点变化和使用StatCallback获取状态回调
    public void aWait() {
        zk.exists("/appConfig", this, this, "ABC");
        try {
            // 阻塞住，直到获取到配置，并设置属性完成
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // AsyncCallback.StatCallback，作为zk.exists的状态回调方法，异步返回状态查询信息
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if(stat != null){
            // 节点已经存在，则直接获取节点内容，否则需要在watch事件回调中获取节点信息
            // 注册节点变换监听watch和数据获取回调方法AsyncCallback.DataCallback
            System.out.println("AsyncCallback.StatCallback......");
            zk.getData("/appConfig", this, this, "sdfs");
        }
    }

    // Watch监听节点变化
    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                // 监听节点被创建时，获取节点内容
                System.out.println("ConfigUtils process, NodeCreated......");
                zk.getData("/appConfig",this,this,"sdfs");
                break;
            case NodeDeleted:
                System.out.println("ConfigUtils process, NodeDeleted......");
                //容忍性
                myConfig.setConfig("");
                // 节点被删除，重置
                countDownLatch = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                System.out.println("ConfigUtils process, NodeDataChanged......");
                zk.getData("/appConfig", this, this, "dsgd");
                break;
            case NodeChildrenChanged:
                break;
        }
    }

    // AsyncCallback.DataCallback，作为zk.getData的数据回调方法
    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        if(data != null ){
            String s = new String(data);
            myConfig.setConfig(s);
            countDownLatch.countDown();
        }
    }
}
