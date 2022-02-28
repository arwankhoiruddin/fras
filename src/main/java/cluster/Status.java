package cluster;

public enum Status {
    IDLE, SEND_DATA, RECEIVE_DATA, RUN_MAP, RUN_SHUFFLE, RUN_SORT, RUN_REDUCE;
}
