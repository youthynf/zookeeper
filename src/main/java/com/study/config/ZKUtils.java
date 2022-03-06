package com.study.config;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @ClassName: ZKUtils
 * @Description:
 * @Author: ynf
 * @Date: 2022-03-04 7:08
 * @Version: 1.0
 */
public class ZKUtils {

    private static ZooKeeper zk;

    // 使用testConfig作为前缀
    private static final String address = "192.168.93.129:2181,192.168.93.130:2181,192.168.93.131:2181/testConfig";

    // 将watch事件独立成一个类
    private static final DefaultWatch watcher = new DefaultWatch();

    // DefaultWatch接收ZKUtils的CountDownLatch，保证同步有效
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    public static ZooKeeper getZk() {
        try {
            // 通过传递countDownLatch确保用的是同一道门栓
            watcher.setCountDownLatch(countDownLatch);
            zk = new ZooKeeper(address, 1000, watcher);
            // 一直阻塞住，直到watcher收到zk连接成功事件才继续往下
            countDownLatch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return zk;
    }
}
