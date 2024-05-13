/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * This interface is deprecated and will be removed in 1.13. It has never been used.
 * It provides methods to be called before and after a specific rendering context.
 *
 * @deprecated TODO remove in 1.13. This has never been used
 */
@Deprecated
public interface IRenderContextHandler
{
    /**
     * This method is called before the specified rendering context.
     * It can be used to perform any necessary setup or preparation for the rendering process.
     */
    void beforeRenderContext();

    /**
     * This method is called after the specified rendering context.
     * It can be used to perform any necessary cleanup or finalization for the rendering process.
     */
    void afterRenderContext();
}

