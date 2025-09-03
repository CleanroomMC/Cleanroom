package com.cleanroommc.kirino.gl.debug;

public class DebugMessageFilter {
    private DebugMsgSource source = DebugMsgSource.ANY;
    private DebugMsgType type = DebugMsgType.ANY;
    private DebugMsgSeverity severity = DebugMsgSeverity.ANY;

    public DebugMsgSource getSource() {
        return source;
    }

    public DebugMsgType getType() {
        return type;
    }

    public DebugMsgSeverity getSeverity() {
        return severity;
    }

    public DebugMessageFilter(DebugMsgSource source, DebugMsgType type, DebugMsgSeverity severity) {
        this.source = source;
        this.type = type;
        this.severity = severity;
    }
}
