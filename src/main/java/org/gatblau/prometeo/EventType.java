package org.gatblau.prometeo;

public enum EventType {
    START_PROCESS,
    END_PROCESS,
    DOWNLOAD_SCRIPTS,
    RUN_ANSIBLE,
    SETUP_ANSIBLE,
    REMOVE_WORKDIR,
    ERROR,
    CHECKOUT_TAG,
    CONFIG,
    CALLBACK,
    START_DEV_PROCESS,
    INVALID_PAYLOAD,
}


