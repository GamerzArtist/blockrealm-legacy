package com.voxelforge.game;
import org.junit.Test;
import static org.junit.Assert.*;
public class VoxelWorldTest {
    @Test public void deterministicAndNegativeCoordinates(){VoxelWorld a=new VoxelWorld(42),b=new VoxelWorld(42);for(int x=-32;x<32;x++)for(int z=-32;z<32;z++)assertEquals(a.height(x,z),b.height(x,z));assertEquals(VoxelWorld.STONE,a.get(-16,0,-1));}
    @Test public void editAndRestore(){VoxelWorld a=new VoxelWorld(7),b=new VoxelWorld(7);a.set(-1,100,-1,VoxelWorld.PLANK);assertEquals(VoxelWorld.PLANK,a.get(-1,100,-1));b.restore(a.edits());assertEquals(VoxelWorld.PLANK,b.get(-1,100,-1));a.set(-1,100,-1,VoxelWorld.AIR);assertEquals(VoxelWorld.AIR,a.get(-1,100,-1));}
    @Test public void meshTrianglesAreFinite(){VoxelWorld a=new VoxelWorld(42);VoxelWorld.Mesh m=a.mesh(-1,0);assertTrue(m.opaque.length>0);assertEquals(0,m.opaque.length%21);assertEquals(0,m.water.length%21);for(float f:m.opaque)assertTrue(Float.isFinite(f));}
    @Test public void boundaries(){VoxelWorld a=new VoxelWorld(42);assertEquals(VoxelWorld.STONE,a.get(-100,-1,-100));assertEquals(VoxelWorld.AIR,a.get(0,VoxelWorld.HEIGHT,0));a.set(0,0,0,VoxelWorld.AIR);assertEquals(VoxelWorld.STONE,a.get(0,0,0));}
}
