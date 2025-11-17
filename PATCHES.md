- KirinoCore
- ChunkProviderClient#getLoadedChunks
- ChunkProviderClient
  ```java
  public java.util.function.BiConsumer<Integer, Integer> loadChunkCallback = null;
  public java.util.function.BiConsumer<Integer, Integer> unloadChunkCallback = null;

  public void unloadChunk(int x, int z)
  {
      ...
      if (unloadChunkCallback != null)
      {
          unloadChunkCallback.accept(x, z);
      }
  }

  public Chunk loadChunk(int chunkX, int chunkZ)
  {
      ...
      if (loadChunkCallback != null)
      {
          loadChunkCallback.accept(chunkX, chunkZ);
      }
      return chunk;
  }
  ```
- ChunkPos
  ```java
  public static long asLong(int x, int z)
  {
      return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
  }
  ```
