package com.alibaba.datax.plugin.writer.eswriter;

import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ESWriter extends Writer {

    public static class Job extends Writer.Job {

        private Configuration originalConfiguration = null;

        @Override
        public void init() {
            this.originalConfiguration = super.getPluginJobConf();
        }

        @Override
        public void prepare() {
            super.prepare();
        }

        @Override
        public void preCheck() {
            super.preCheck();
        }

        @Override
        public void preHandler(Configuration jobConfiguration) {
            super.preHandler(jobConfiguration);
        }

        @Override
        public void post() {
            super.post();
        }

        @Override
        public void postHandler(Configuration jobConfiguration) {
            super.postHandler(jobConfiguration);
        }

        @Override
        public void destroy() {

        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            List<Configuration> writerSplitConfiguration = new ArrayList<Configuration>();
            for (int i = 0; i < mandatoryNumber; i++) {
                writerSplitConfiguration.add(this.originalConfiguration);
            }
            return writerSplitConfiguration;
        }

    }

    public static class Task extends Writer.Task {

        private static final Logger LOG = LoggerFactory.getLogger(Task.class);
        private Configuration writerSliceConfiguration = null;
        private String esClusterName = null;
        private List<String> esClusterIP = null;
        private Integer esClusterPort = null;
        private String esIndex = null;
        private String esType = null;
        private List<String> columns = null;
        private List<String> docid = null;
        private TransportClient client = null;
        private Integer batchSize = null;

        private void closeBulkProcessor(BulkProcessor bulkProcessor) {
            try {
                boolean awaitClose = bulkProcessor.awaitClose(10, TimeUnit.MINUTES);//阻塞至所有的请求线程处理完毕后，断开连接资源
//                LOGGER.info("closeBulkProcessor return " + awaitClose);
            } catch (Exception e) {
//                LOGGER.error("closeBulkProcessor Exception", e);
            }
        }

        @Override
        public void init() {
            this.writerSliceConfiguration = super.getPluginJobConf();
            this.esClusterName = writerSliceConfiguration.getString(Key.esClusterName);
            this.esClusterIP = writerSliceConfiguration.getList(Key.esClusterIP, String.class);
            this.esClusterPort = writerSliceConfiguration.getInt(Key.esClusterPort, 9300);
            this.esIndex = writerSliceConfiguration.getString(Key.esIndex);
            this.esType = writerSliceConfiguration.getString(Key.esType);
            this.columns = writerSliceConfiguration.getList(Key.COLUMN, String.class);
            this.docid = writerSliceConfiguration.getList(Key.DOCID, String.class);
            this.batchSize = writerSliceConfiguration.getInt(Key.batchSize, 1000);
        }

        @Override
        public void prepare() {
            super.prepare();
            Settings settings = Settings.builder().put("cluster.name", esClusterName)
                    .put("client.tansport.sniff", true).build();
            client = TransportClient.builder().settings(settings).build();
            for (String ip : this.esClusterIP) {
                client.addTransportAddress(new InetSocketTransportAddress(
                        new InetSocketAddress(ip, this.esClusterPort)));
            }
        }

        @Override
        public void preCheck() {
            super.preCheck();
        }

        @Override
        public void preHandler(Configuration jobConfiguration) {
            super.preHandler(jobConfiguration);
        }

        @Override
        public void post() {
            super.post();
        }

        @Override
        public void postHandler(Configuration jobConfiguration) {
            super.postHandler(jobConfiguration);
        }

        @Override
        public void destroy() {
            client.close();
        }

        @Override
        public void startWrite(RecordReceiver lineReceiver) {
            BulkProcessor bulkProcessor = null;
            try {
                bulkProcessor = getBulkProcessor();
            } catch (Exception e) {
                throw DataXException
                        .asDataXException(
                                ESWriterErrorCode.ESSERVER_ERROR,
                                ESWriterErrorCode.ESSERVER_ERROR.getDescription());
            }

            Record record = null;
            while ((record = lineReceiver.getFromReader()) != null) {
                if (record.getColumnNumber() != this.columns.size()) {
                    // 源头读取字段列数与目的表字段写入列数不相等，直接报错
                    throw DataXException
                            .asDataXException(
                                    ESWriterErrorCode.CONF_ERROR,
                                    String.format(
                                            "列配置信息有错误. 因为您配置的任务中，源头读取字段数:%s 与 目的表要写入的字段数:%s 不相等. 请检查您的配置并作出修改.",
                                            record.getColumnNumber(),
                                            this.columns.size()));
                }
                //

                doBatchInsert(bulkProcessor, record);
            }
            closeBulkProcessor(bulkProcessor);
        }

        private BulkProcessor getBulkProcessor() throws Exception {
            BulkProcessor bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
                @Override
                public void beforeBulk(long id, BulkRequest req) {
//                    LOGGER.info("id: " + id + " beforeBulk ");//发送请求前，可以做一些事情
                }

                @Override
                public void afterBulk(long id, BulkRequest req, Throwable cause) {
//                    LOGGER.info("id: " + id + "  afterBulk " + "  cause: " + cause.getMessage());//发送请求失败，可以做一些事情
                }

                @Override
                public void afterBulk(long id, BulkRequest req, BulkResponse rep) {
//                    LOGGER.info("id: " + id + "  afterBulk ");//发送请求成功后，可以做一些事情
                }
            }).setBulkActions(this.batchSize)//达到批量请求处理一次
                    .setBulkSize(new ByteSizeValue(100, ByteSizeUnit.MB))//flush the bulk every 100mb。默认为5m
                    .setFlushInterval(TimeValue.timeValueSeconds(5))//每5秒一定执行，不管已经队列积累了多少。默认不设置这个值
                    .setConcurrentRequests(17)//设置多少个并发处理线程
                    .build();//构建BulkProcessor
            return bulkProcessor;
        }

        private void doBatchInsert(BulkProcessor bulkProcessor, Record record) {
            Map<String, Object> hm = null;
            StringBuffer sbuffer = new StringBuffer();
            int fieldNum = record.getColumnNumber();
            if (null != record && fieldNum > 0) {
                hm = new HashMap<String, Object>();
                for (int i = 0; i < fieldNum; i++) {
                    if (this.docid.contains(this.columns.get(i))) {
                        sbuffer.append(record.getColumn(i).asString());
                    }
                    hm.put(this.columns.get(i), record.getColumn(i).asString());
                }
            }
            if (hm != null) {
                bulkProcessor.add(new IndexRequest(this.esIndex, this.esType, sbuffer.toString()).source(hm));
            }
        }

    }

}
