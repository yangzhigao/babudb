/*
 * Copyright (c) 2011, Jan Stender, Bjoern Kolbeck, Mikael Hoegqvist,
 *                     Felix Hupfeld, Felix Langner, Zuse Institute Berlin
 * 
 * Licensed under the BSD License, see LICENSE file for details.
 * 
 */
package org.xtreemfs.babudb.replication.service.operations;

import static org.junit.Assert.*;
import static org.xtreemfs.babudb.replication.TestParameters.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xtreemfs.babudb.config.ReplicationConfig;
import org.xtreemfs.babudb.lsmdb.LSN;
import org.xtreemfs.babudb.mock.BabuDBMock;
import org.xtreemfs.babudb.mock.StatesManipulationMock;
import org.xtreemfs.babudb.replication.BabuDBInterface;
import org.xtreemfs.babudb.replication.LockableService;
import org.xtreemfs.babudb.replication.LockableService.ServiceLockedException;
import org.xtreemfs.babudb.replication.control.ControlLayerInterface;
import org.xtreemfs.babudb.replication.service.ReplicationRequestHandler;
import org.xtreemfs.babudb.replication.service.RequestManagement;
import org.xtreemfs.babudb.replication.service.StageRequest;
import org.xtreemfs.babudb.replication.service.ReplicationStage.BusyServerException;
import org.xtreemfs.babudb.replication.service.clients.MasterClient;
import org.xtreemfs.babudb.replication.transmission.FileIO;
import org.xtreemfs.babudb.replication.transmission.client.ReplicationClientAdapter;
import org.xtreemfs.babudb.replication.transmission.dispatcher.RequestControl;
import org.xtreemfs.babudb.replication.transmission.dispatcher.RequestDispatcher;
import org.xtreemfs.babudb.replication.transmission.dispatcher.RequestHandler;
import org.xtreemfs.foundation.LifeCycleListener;
import org.xtreemfs.foundation.TimeSync;
import org.xtreemfs.foundation.buffer.ASCIIString;
import org.xtreemfs.foundation.buffer.BufferPool;
import org.xtreemfs.foundation.buffer.ReusableBuffer;
import org.xtreemfs.foundation.flease.comm.FleaseMessage;
import org.xtreemfs.foundation.flease.comm.FleaseMessage.MsgType;
import org.xtreemfs.foundation.logging.Logging;
import org.xtreemfs.foundation.logging.Logging.Category;
import org.xtreemfs.foundation.pbrpc.client.RPCNIOSocketClient;

/**
 * Test of the operation logic for master replication requests.
 * 
 * @author flangner
 * @since 04/08/2011
 */
public class MasterReplicationOperationsTest implements LifeCycleListener {
    
    private static ReplicationConfig    config;
    private static RPCNIOSocketClient   rpcClient;
    private MasterClient                client;
    private RequestDispatcher           dispatcher;
    
    // test data
    private final static Random                random = new Random();
    private final static AtomicReference<LSN>  lastOnView = new AtomicReference<LSN>(new LSN(1,1L));
    private final static String                testFileName = "TestFile";
    private final static long                  maxTestFileSize = 4 * 1024;
    private final static FleaseMessage         testMessage = 
        new FleaseMessage(MsgType.MSG_ACCEPT_ACK);
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Logging.start(Logging.LEVEL_DEBUG, Category.all);
        TimeSync.initializeLocal(TIMESYNC_GLOBAL, TIMESYNC_LOCAL);
        
        config = new ReplicationConfig("config/replication_server0.test", conf0);
        
        rpcClient = new RPCNIOSocketClient(config.getSSLOptions(), RQ_TIMEOUT, CON_TIMEOUT);
        rpcClient.start();
        rpcClient.waitForStartup();
        
        // create testFile
        FileOutputStream sOut = new FileOutputStream(testFileName);
        
        long size = 0L;
        while (size < maxTestFileSize) {
            sOut.write(random.nextInt());
            size++;
        }
        sOut.flush();
        sOut.close();
        
        // setup flease message
        testMessage.setCellId(new ASCIIString("testCellId"));
        testMessage.setLeaseHolder(new ASCIIString("testLeaseholder"));
        testMessage.setLeaseTimeout(4711L);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rpcClient.shutdown();
        rpcClient.waitForShutdown();
        
        TimeSync ts = TimeSync.getInstance();
        ts.shutdown();
        ts.waitForShutdown();
        
        // delete testFile
        File f = new File(testFileName);
        if (!f.delete()) f.deleteOnExit();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        
        client = new ReplicationClientAdapter(rpcClient, config.getInetSocketAddress());
        
        RequestHandler rqHandler = new ReplicationRequestHandler(
                new StatesManipulationMock(config.getInetSocketAddress()), 
                new ControlLayerInterface() {
            
            @Override
            public void updateLeaseHolder(InetSocketAddress leaseholder) throws Exception {
                fail("Operation should not have been accessed by this test!");
            }
            
            @Override
            public void receive(FleaseMessage message) {
                fail("Operation should not have been accessed by this test!");
            }
            
            @Override
            public void driftDetected() {
                fail("Operation should not have been accessed by this test!");
            }
            
            @Override
            public void unlockUser() {
                fail("Operation should not have been accessed by this test!");
            }
            
            @Override
            public void unlockReplication() {
                fail("Operation should not have been accessed by this test!");
            }
            
            @Override
            public void registerUserInterface(LockableService service) {
                fail("Operation should not have been accessed by this test!");
            }
            
            @Override
            public void registerReplicationControl(LockableService service) {
                fail("Operation should not have been accessed by this test!");
            }
            
            @Override
            public void registerProxyRequestControl(RequestControl control) {
                fail("Operation should not have been accessed by this test!");
            }
            
            @Override
            public void notifyForSuccessfulFailover(InetSocketAddress master) {
                fail("Operation should not have been accessed by this test!");
            }
            
            @Override
            public void lockAll() throws InterruptedException {
                fail("Operation should not have been accessed by this test!");
            }
            
            @Override
            public boolean isItMe(InetSocketAddress address) {
                fail("Operation should not have been accessed by this test!");
                return false;
            }
            
            @Override
            public InetSocketAddress getLeaseHolder() {
                fail("Operation should not have been accessed by this test!");
                return null;
            }
        }, new BabuDBInterface(new BabuDBMock("BabuDBMock", conf0)), new RequestManagement() {
            
            @Override
            public void finalizeRequest(StageRequest op) {
                fail("Operation should not have been accessed by this test!");
            }
            
            @Override
            public void enqueueOperation(Object[] args) throws BusyServerException, ServiceLockedException {
                fail("Operation should not have been accessed by this test!");
            }
            
            @Override
            public void createStableState(LSN lastOnView, InetSocketAddress master) {
                fail("Operation should not have been accessed by this test!");
                
            }
        }, lastOnView, config.getChunkSize(), new FileIO(config), MAX_Q);
        
        dispatcher = new RequestDispatcher(config);
        dispatcher.setLifeCycleListener(this);
        dispatcher.addHandler(rqHandler);
        dispatcher.start();
        dispatcher.waitForStartup();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        dispatcher.shutdown();
        dispatcher.waitForShutdown();
    }
    
    /** 
     * @throws Exception
     */
    @Test
    public void testChunkRequest() throws Exception {
        
        // test data
        int minChunkSize = 512;
        
        int start = random.nextInt((int) (maxTestFileSize - minChunkSize));
        int end = random.nextInt((int) (maxTestFileSize - minChunkSize - start)) 
                        + minChunkSize + start;
        ReusableBuffer result = client.chunk(testFileName, start, end).get();
        assertEquals(end - start, result.remaining());
        
        byte[] testData = new byte[end - start];
        FileInputStream sIn = new FileInputStream(testFileName);
        sIn.skip(start);
        sIn.read(testData, 0, end - start);
        sIn.close();
        
        assertEquals(new String(testData), new String(result.array()));
        
        // clean up
        BufferPool.free(result);
    }
    
    /** 
     * @throws Exception
     */
    @Test
    public void testFleaseRequest() throws Exception {
        
        // TODO
        //client.flease(message);
        fail("Not yet implemented");
    }
    
    /** 
     * @throws Exception
     */
    @Test
    public void testHeartbeatRequest() throws Exception {
//        client.heartbeat(lsn, localPort); TODO
        fail("Not yet implemented");
    }
    
    /** 
     * @throws Exception
     */
    @Test
    public void testLoadRequest() throws Exception {
//        client.load(lsn); TODO
        fail("Not yet implemented");
    }
    
    /** 
     * @throws Exception
     */
    @Test
    public void testReplicaRequest() throws Exception {
//        client.replica(start, end); TODO
        fail("Not yet implemented");
    }
    
    /** 
     * @throws Exception
     */
    @Test
    public void testStateRequest() throws Exception {
//        client.state(); TODO
        fail("Not yet implemented");
    }
    
    /** 
     * @throws Exception
     */
    @Test
    public void testSynchronizeRequest() throws Exception {
//        client.synchronize(lsn, localPort); TODO
        fail("Not yet implemented");
    }
    
    /** 
     * @throws Exception
     */
    @Test
    public void testTimeRequest() throws Exception {
//        client.time(); TODO
        fail("Not yet implemented");
    }
    
    /** 
     * @throws Exception
     */
    @Test
    public void testVolatileStateRequest() throws Exception {
//        client.volatileState().get(); TODO
        fail("Not yet implemented");
    }

    /* (non-Javadoc)
     * @see org.xtreemfs.foundation.LifeCycleListener#startupPerformed()
     */
    @Override
    public void startupPerformed() { }

    /* (non-Javadoc)
     * @see org.xtreemfs.foundation.LifeCycleListener#shutdownPerformed()
     */
    @Override
    public void shutdownPerformed() { }

    /* (non-Javadoc)
     * @see org.xtreemfs.foundation.LifeCycleListener#crashPerformed(java.lang.Throwable)
     */
    @Override
    public void crashPerformed(Throwable cause) {
        fail("Dispatcher crashed: " + cause.getMessage());
    }
}