package com.feather.binlog;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FeatherBinlogCatch {

    private static final int port = 11111;

    public static void main(String[] args) throws InvalidProtocolBufferException {
        CanalConnector connect = CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),port),"example","","");
        int batchSize = 10;
        int emptyCount = 0;
        try{
            while(true){
                connect.connect();
                connect.subscribe();
                connect.rollback();
                Message message = connect.getWithoutAck(batchSize);
                List<CanalEntry.Entry> entryList = message.getEntries();
                for(CanalEntry.Entry entry: entryList ){
                    CanalEntry.Header header = entry.getHeader();
                    System.out.println(String.format("%s-%s-%s-%s-%s-%s",header.getLogfileName(),header.getLogfileOffset(),header.getExecuteTime(),header.getSchemaName(),header.getTableName(),header.getEventType()));
                    System.out.println(entry.getEntryType().getNumber());
                    CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                    List<CanalEntry.RowData> rowDataList = rowChange.getRowDatasList();
                    for(CanalEntry.RowData rowData : rowDataList){
                        List<CanalEntry.Column> columnList = rowData.getBeforeColumnsList();
                        for(CanalEntry.Column column : columnList){
                            System.out.println(column.getMysqlType()+" "+column.getName()+" "+column.getValue());
                        }
                    }
                    for(CanalEntry.RowData rowData : rowDataList){
                        List<CanalEntry.Column> columnList = rowData.getAfterColumnsList();
                        for(CanalEntry.Column column : columnList){
                            System.out.println(column.getMysqlType()+" "+column.getName()+" "+column.getValue());
                        }
                    }
                }
                connect.ack(message.getId());
            }
        }finally {
            connect.disconnect();
        }
    }

    private static void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }
}
