package com.cleanroommc.kirino.engine.render.pipeline.draw.cmd;

import com.cleanroommc.kirino.engine.render.pipeline.pass.PassHint;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class HighLevelDC implements IDrawCommand {
    public enum CommandSource {
        PASS_INTERNAL,
        SCENE_SUBMITTED
    }

    public final CommandSource source;
    public final PassHint passHint;
    public final String meshTicketID;
    public final int mode;
    public final int elementType;
    // todo: sort key, is visible, material

    private HighLevelDC(CommandSource source, PassHint passHint, String meshTicketID,
                        int mode, int elementType) {
        this.source = source;
        this.passHint = passHint;
        this.meshTicketID = meshTicketID;
        this.mode = mode;
        this.elementType = elementType;
    }

    private interface CommandBuilder {
        @NonNull
        HighLevelDC build();
    }

    public static class PassInternalBuilder implements CommandBuilder {
        private PassInternalBuilder() {
        }

        private String meshTicketID = null;
        private int mode = -1;
        private int elementType = -1;

        public PassInternalBuilder meshTicketID(@NonNull String meshTicketID) {
            Preconditions.checkNotNull(meshTicketID, "Invalid \"meshTicketID\".");
            Preconditions.checkArgument(!meshTicketID.isEmpty(), "Invalid \"meshTicketID\".");

            this.meshTicketID = meshTicketID;
            return this;
        }

        public PassInternalBuilder mode(int mode) {
            Preconditions.checkArgument(mode >= 0, "Invalid \"mode\".");

            this.mode = mode;
            return this;
        }

        public PassInternalBuilder elementType(int elementType) {
            Preconditions.checkArgument(elementType >= 0, "Invalid \"elementType\".");

            this.elementType = elementType;
            return this;
        }

        @NonNull
        @Override
        public HighLevelDC build() {
            Preconditions.checkState(meshTicketID != null && mode != 1 && elementType != -1,
                    "Must set all parameters \"meshTicketID\", \"mode\", \"elementType\".");

            return new HighLevelDC(CommandSource.PASS_INTERNAL, null, meshTicketID, mode, elementType);
        }
    }

    public static class SceneSubmittedBuilder implements CommandBuilder {
        private SceneSubmittedBuilder() {
        }

        private PassHint passHint = null;
        private String meshTicketID = null;
        private int mode = -1;
        private int elementType = -1;

        public SceneSubmittedBuilder passHint(@NonNull PassHint passHint) {
            Preconditions.checkNotNull(passHint, "Invalid \"passHint\".");

            this.passHint = passHint;
            return this;
        }

        public SceneSubmittedBuilder meshTicketID(@NonNull String meshTicketID) {
            Preconditions.checkNotNull(meshTicketID, "Invalid \"meshTicketID\".");
            Preconditions.checkArgument(!meshTicketID.isEmpty(), "Invalid \"meshTicketID\".");

            this.meshTicketID = meshTicketID;
            return this;
        }

        public SceneSubmittedBuilder mode(int mode) {
            Preconditions.checkArgument(mode >= 0, "Invalid \"mode\".");

            this.mode = mode;
            return this;
        }

        public SceneSubmittedBuilder elementType(int elementType) {
            Preconditions.checkArgument(elementType >= 0, "Invalid \"elementType\".");

            this.elementType = elementType;
            return this;
        }

        @NonNull
        @Override
        public HighLevelDC build() {
            Preconditions.checkState(passHint != null && meshTicketID != null,
                    "Must set all parameters \"passHint\", \"meshTicketID\", \"mode\", \"elementType\".");

            return new HighLevelDC(CommandSource.SCENE_SUBMITTED, passHint, meshTicketID, mode, elementType);
        }
    }

    public static PassInternalBuilder passInternal() {
        return new PassInternalBuilder();
    }

    public static SceneSubmittedBuilder sceneSubmitted() {
        return new SceneSubmittedBuilder();
    }
}
