package com.study.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @ClassName: WatchBack
 * @Description:
 * @Author: ynf
 * @Date: 2022-03-06 23:41
 * @Version: 1.0
 */
public class WatchCallBack implements AsyncCallback.StringCallback, AsyncCallback.Children2Callback, Watcher, AsyncCallback.StatCallback {

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private ZooKeeper zk;

    private String threadName;

    private String nodeName;

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void tryLock() {
        try {
            zk.create("/lock", threadName.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, this, "absf");
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unLock(){
        try {
            zk.delete(nodeName,-1);
            System.out.println(threadName + " over work....");
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    // AsyncCallback.StringCallback，创建结果回调
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if(name != null ){
            System.out.println(threadName  +"  create node : " +  name );
            nodeName =  name ;
            zk.getChildren("/", false, this, "sdf");
        }
    }

    // AsyncCallback.Children2Callback，获取根节点所有孩子节点结果回调
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        Collections.sort(children);
        int index = children.indexOf(nodeName.substring(1));

        System.out.println("ThreadName " + threadName + " index：" + index);

        if (index == 0) {
            System.out.println("I am the first......");
            try {
                zk.setData("/", threadName.getBytes(), -1);
                countDownLatch.countDown();
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            zk.exists("/" + children.get(index - 1), this, this, "sdf");
        }
    }

    // watch
    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                System.out.println("NodeDeleted......");
                zk.getChildren("/",false,this ,"sdf");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
        }
    }

    // AsyncCallback.StatCallback
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {

    }
}
