package com.devicehive.shim.kafka.server;

import com.devicehive.shim.api.Request;
import com.devicehive.shim.api.server.RpcServer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public class KafkaRpcServer implements RpcServer {

    private String topic;
    private int consumerThreads;
    private Properties consumerProps;
    private ExecutorService consumerExecutor;
    private ClientRequestDispatcher requestDispatcher;

    private List<RequestConsumerWorker> consumerWorkers;

    public KafkaRpcServer(String topic, int consumerThreads, Properties consumerProps, ExecutorService consumerExecutor, ClientRequestDispatcher requestDispatcher) {
        this.topic = topic;
        this.consumerThreads = consumerThreads;
        this.consumerProps = consumerProps;
        this.consumerExecutor = consumerExecutor;
        this.requestDispatcher = requestDispatcher;
    }

    @Override
    public void start() {
        consumerWorkers = new ArrayList<>(consumerThreads);
        for (int i = 0; i < consumerThreads; i++) {
            KafkaConsumer<String, Request> consumer = new KafkaConsumer<>(consumerProps);
            RequestConsumerWorker worker = new RequestConsumerWorker(topic, consumer, requestDispatcher);
            consumerExecutor.submit(worker);
        }

    }

    @Override
    public void shutdown() {
        requestDispatcher.shutdown();
        consumerWorkers.forEach(RequestConsumerWorker::shutdown);
    }

}