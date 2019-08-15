package com.hwx.rx_chat_client.background.p2p.object;

import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;

public class PipeHolder {
    private PublishProcessor<RxP2PObject> txPipe;
    private PublishSubject<RxP2PObject> rxPipe;

    public PipeHolder(PublishProcessor<RxP2PObject> txPipe, PublishSubject<RxP2PObject> rxPipe) {
        this.txPipe = txPipe;
        this.rxPipe = rxPipe;
    }

    public PublishProcessor<RxP2PObject> getTxPipe() {
        return txPipe;
    }

    public void setTxPipe(PublishProcessor<RxP2PObject> txPipe) {
        this.txPipe = txPipe;
    }

    public PublishSubject<RxP2PObject> getRxPipe() {
        return rxPipe;
    }

    public void setRxPipe(PublishSubject<RxP2PObject> rxPipe) {
        this.rxPipe = rxPipe;
    }
}
