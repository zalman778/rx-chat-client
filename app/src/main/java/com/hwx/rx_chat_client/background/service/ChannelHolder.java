//package com.hwx.rx_chat_client.background.service;
//
//import com.hwx.rx_chat.common.object.rx.RxObject;
//
//import reactor.core.Disposable;
//import reactor.core.publisher.ReplayProcessor;
//
///*
//    Object, which holding openned channel: its processors and closeable
// */
//public class ChannelHolder {
//    private ReplayProcessor<RxObject> rpTxProcessor = ReplayProcessor.create();
//    private ReplayProcessor<RxObject> rpRxProcessor = ReplayProcessor.create();
//    private Disposable disposable;
//
//    public ReplayProcessor<RxObject> getRpTxProcessor() {
//        return rpTxProcessor;
//    }
//
//    public void setRpTxProcessor(ReplayProcessor<RxObject> rpTxProcessor) {
//        this.rpTxProcessor = rpTxProcessor;
//    }
//
//    public ReplayProcessor<RxObject> getRpRxProcessor() {
//        return rpRxProcessor;
//    }
//
//    public void setRpRxProcessor(ReplayProcessor<RxObject> rpRxProcessor) {
//        this.rpRxProcessor = rpRxProcessor;
//    }
//
//    public Disposable getDisposable() {
//        return disposable;
//    }
//
//    public void setDisposable(Disposable disposable) {
//        this.disposable = disposable;
//    }
//}
