/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.controller.util;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

/**
 * fastDFS连接池
 * 
 * @author qinp
 * 
 * @since 0.0.1
 */
public class FastDFSConnectionPoolFactory {

    private GenericObjectPool<StorageClient> pool;

    public FastDFSConnectionPoolFactory(Config config, TrackerClient trackerClient) {
        ConnectionFactory factory = new ConnectionFactory(trackerClient);
        pool = new GenericObjectPool<StorageClient>(factory, config);
    }

    public StorageClient getConnection() throws Exception {
        return pool.borrowObject();
    }

    public void releaseConnection(StorageClient socket) {
        try {
            pool.returnObject(socket);
        } catch (Exception e) {

        }
    }

    /**
     * inner 继承BasePoolableObjectFactory 实现连接创建回收
     * 
     * @author qinp
     * @since 0.0.1
     */
    class ConnectionFactory extends BasePoolableObjectFactory<StorageClient> {

        private TrackerClient tracker;

        public ConnectionFactory(TrackerClient tracker) {
            this.tracker = tracker;
        }

        @Override
        public StorageClient makeObject() throws Exception {
            TrackerServer trackerServer = tracker.getConnection();
            StorageClient socket = new StorageClient(trackerServer, null);
            return socket;
        }

        @Override
        public void destroyObject(StorageClient obj) throws Exception {
            super.destroyObject(obj);
        }
    }

}