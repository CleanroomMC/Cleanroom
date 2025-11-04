package com.cleanroommc.kirino.api;

import com.cleanroommc.kirino.api.render.IRenderQueue;
import com.cleanroommc.kirino.api.render.RenderCommand;
import com.cleanroommc.kirino.api.render.RenderContext;
import net.minecraft.tileentity.TileEntity;

public interface IKirinoTERenderer {
    IRenderQueue<RenderCommand> render(TileEntity tileEntity, RenderContext<RenderCommand> renderContext);
}
