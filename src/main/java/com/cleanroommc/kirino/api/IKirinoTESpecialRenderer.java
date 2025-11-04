package com.cleanroommc.kirino.api;

import com.cleanroommc.kirino.api.render.IRenderQueue;
import com.cleanroommc.kirino.api.render.RenderCommandSpecial;
import com.cleanroommc.kirino.api.render.RenderContext;
import net.minecraft.tileentity.TileEntity;

public interface IKirinoTESpecialRenderer {
    IRenderQueue<RenderCommandSpecial> render(TileEntity tileEntity, RenderContext<RenderCommandSpecial> renderContext);
}
