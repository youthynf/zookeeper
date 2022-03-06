package com.study.config;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @ClassName: TestConfig
 * @Description:
 * @Author: ynf
 * @Date: 2022-03-04 7:29
 * @Version: 1.0
 */
public class TestConfig {

    private ZooKeeper zk;

    @Before
    public void conn() {
        zk = ZKUtils.getZk();
    }

    @After
    public void close() {
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getConfig() {
        MyConfig myConfig = new MyConfig();
        ConfigUtils configUtils = new ConfigUtils();
        configUtils.setMyConfig(myConfig);
        configUtils.setZk(zk);

        // 阻塞的方式，通过zk获取对应的配置设置到myConfig对象中
        configUtils.aWait();

        // 每隔2秒读取一次配置，配置通过watcher事件实时更新
        while(true){
            if(myConfig.getConfig().equals("")){
                //1，节点不存在
                System.out.println("conf diu le ......");
                configUtils.aWait();
            }else{
                //2，节点存在
                System.out.println(myConfig.getConfig());
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
