package com.cleanroommc.kirino.gl.exception;

public class RuntimeGLException extends RuntimeException {
    public RuntimeGLException(String msg) {
        super(msg);
    }
    public RuntimeGLException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
    public RuntimeGLException(Throwable throwable) {
        super(throwable);
    }
}
