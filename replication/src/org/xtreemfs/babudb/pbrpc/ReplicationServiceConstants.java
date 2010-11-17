//automatically generated from replication.proto at Wed Nov 17 15:07:16 CET 2010
//(c) 2010. See LICENSE file for details.

package org.xtreemfs.babudb.pbrpc;

import com.google.protobuf.Message;

public class ReplicationServiceConstants {

    public static final int INTERFACE_ID = 20001;
    public static final int PROC_ID_STATE = 1;
    public static final int PROC_ID_LOAD = 2;
    public static final int PROC_ID_CHUNK = 3;
    public static final int PROC_ID_FLEASE = 4;
    public static final int PROC_ID_LOCALTIME = 5;
    public static final int PROC_ID_REPLICA = 6;
    public static final int PROC_ID_HEARTBEAT = 7;
    public static final int PROC_ID_REPLICATE = 8;

    public static Message getRequestMessage(int procId) {
        switch (procId) {
           case 1: return null;
           case 2: return GlobalTypes.LSN.getDefaultInstance();
           case 3: return GlobalTypes.Chunk.getDefaultInstance();
           case 4: return GlobalTypes.FLease.getDefaultInstance();
           case 5: return null;
           case 6: return GlobalTypes.LSNRange.getDefaultInstance();
           case 7: return GlobalTypes.LSN.getDefaultInstance();
           case 8: return GlobalTypes.LSN.getDefaultInstance();
           default: throw new RuntimeException("unknown procedure id");
        }
    }


    public static Message getResponseMessage(int procId) {
        switch (procId) {
           case 1: return GlobalTypes.LSN.getDefaultInstance();
           case 2: return GlobalTypes.DBFileMetaDatas.getDefaultInstance();
           case 3: return null;
           case 4: return null;
           case 5: return GlobalTypes.Timestamp.getDefaultInstance();
           case 6: return GlobalTypes.LogEntries.getDefaultInstance();
           case 7: return null;
           case 8: return null;
           default: throw new RuntimeException("unknown procedure id");
        }
    }


}