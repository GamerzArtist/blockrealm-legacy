package com.voxelforge.game;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

/** Pure Java deterministic terrain and mesh generator; no Android dependencies. */
final class VoxelWorld {
    static final int SIZE = 16, HEIGHT = 112, SEA = 46;
    static final int AIR=0, GRASS=1, DIRT=2, STONE=3, SAND=4, LOG=5, LEAF=6, PLANK=7, WATER=8, TORCH=9;
    static final String[] NAMES={"Air","Grass","Dirt","Stone","Sand","Log","Leaves","Planks","Water","Glowstone"};
    final int seed;
    private final Map<String,Integer> edits = new HashMap<>();
    private final LinkedHashMap<Long,byte[]> cache = new LinkedHashMap<>(64,.75f,true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long,byte[]> e) {return size()>96;}
    };
    VoxelWorld(int seed){this.seed=seed;}
    static long key(int x,int z){return ((long)x<<32)|(z&0xffffffffL);}
    static String editKey(int x,int y,int z){return x+","+y+","+z;}
    static float hash(int x,int y,int z,int s){int n=x*374761393^y*668265263^z*1274126177^s*1597334677;n=(n^(n>>>13))*1274126177;return (float)((n^(n>>>16))&0xffffffffL)/4294967296f;}
    static float lerp(float a,float b,float t){return a+(b-a)*t;}
    static float smooth(float x){return x*x*(3-2*x);}
    float noise(float x,float z,float scale,int offset){x/=scale;z/=scale;int a=(int)Math.floor(x),b=(int)Math.floor(z);float u=smooth(x-a),v=smooth(z-b);return lerp(lerp(hash(a,0,b,seed+offset),hash(a+1,0,b,seed+offset),u),lerp(hash(a,0,b+1,seed+offset),hash(a+1,0,b+1,seed+offset),u),v);}
    int height(int x,int z){float c=noise(x,z,115,1),h=noise(x,z,30,2),detail=noise(x,z,9,3);return Math.max(20,Math.min(85,(int)(45+(c-.43f)*40+(h-.5f)*13+(detail-.5f)*5)));}
    private int index(int x,int y,int z){return (y*SIZE+z)*SIZE+x;}
    private boolean tree(int x,int z){return height(x,z)>SEA+2&&hash(x,0,z,seed+19)<.026f&&noise(x,z,60,12)>.32f;}
    private void setLocal(byte[] a,int x,int y,int z,int id){if(x>=0&&x<SIZE&&z>=0&&z<SIZE&&y>=0&&y<HEIGHT)a[index(x,y,z)]=(byte)id;}
    private byte[] chunk(int cx,int cz){long k=key(cx,cz);byte[] data=cache.get(k);if(data!=null)return data;data=new byte[SIZE*SIZE*HEIGHT];int ox=cx*SIZE,oz=cz*SIZE;
        for(int z=0;z<SIZE;z++)for(int x=0;x<SIZE;x++){
            int h=height(ox+x,oz+z),surface=h<=SEA+1?SAND:GRASS;
            for(int y=0;y<=Math.max(h,SEA);y++)data[index(x,y,z)]=(byte)(y>h?WATER:y==h?surface:y>=h-3?(surface==SAND?SAND:DIRT):STONE);
        }
        // Feature anchors outside the chunk are included to avoid chopped trees at borders.
        for(int z=oz-2;z<oz+SIZE+2;z++)for(int x=ox-2;x<ox+SIZE+2;x++)if(tree(x,z)){
            int h=height(x,z),len=4+(int)(hash(x,1,z,seed)*3);
            for(int dy=-2;dy<=2;dy++)for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)if(Math.abs(dx)+Math.abs(dz)+Math.abs(dy)<5)setLocal(data,x-ox+dx,h+len+dy,z-oz+dz,LEAF);
            for(int y=h+1;y<=h+len;y++)setLocal(data,x-ox,y,z-oz,LOG);
        }
        cache.put(k,data);return data;
    }
    synchronized int get(int x,int y,int z){if(y<0)return STONE;if(y>=HEIGHT)return AIR;Integer edit=edits.isEmpty()?null:edits.get(editKey(x,y,z));if(edit!=null)return edit;return chunk(Math.floorDiv(x,SIZE),Math.floorDiv(z,SIZE))[index(Math.floorMod(x,SIZE),y,Math.floorMod(z,SIZE))]&255;}
    synchronized void set(int x,int y,int z,int id){if(y>0&&y<HEIGHT&&id>=0&&id<NAMES.length)edits.put(editKey(x,y,z),id);}
    synchronized Map<String,Integer> edits(){return new HashMap<>(edits);}
    synchronized void restore(Map<String,Integer> saved){edits.clear();edits.putAll(saved);}
    static boolean solid(int id){return id!=AIR&&id!=WATER;}
    // Faces contain positions, normals, material and UV. Winding is counter-clockwise.
    static final int[][] N={{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
    static final int[][][] V={{{1,0,0},{1,1,0},{1,1,1},{1,0,1}},{{0,0,1},{0,1,1},{0,1,0},{0,0,0}},{{0,1,1},{1,1,1},{1,1,0},{0,1,0}},{{0,0,0},{1,0,0},{1,0,1},{0,0,1}},{{1,0,1},{1,1,1},{0,1,1},{0,0,1}},{{0,0,0},{0,1,0},{1,1,0},{1,0,0}}};
    static final int[] ORDER={0,1,2,0,2,3};
    static final class Mesh {final float[] opaque,water;Mesh(float[] o,float[] w){opaque=o;water=w;}}
    static final class Floats {float[] data=new float[8192];int size;void add(float a){if(size==data.length)data=Arrays.copyOf(data,data.length*2);data[size++]=a;}float[] result(){return Arrays.copyOf(data,size);}}
    private final class Neighborhood {
        final int cx,cz; final byte[][] data=new byte[9][]; final Map<String,Integer> changed;
        Neighborhood(int cx,int cz){this.cx=cx;this.cz=cz;synchronized(VoxelWorld.this){for(int dz=-1;dz<=1;dz++)for(int dx=-1;dx<=1;dx++)data[(dz+1)*3+dx+1]=chunk(cx+dx,cz+dz);changed=new HashMap<>(edits);}}
        int block(int x,int y,int z){if(y<0)return STONE;if(y>=HEIGHT)return AIR;Integer edit=changed.isEmpty()?null:changed.get(editKey(x,y,z));if(edit!=null)return edit;int a=Math.floorDiv(x,SIZE)-cx+1,b=Math.floorDiv(z,SIZE)-cz+1;return data[b*3+a][index(Math.floorMod(x,SIZE),y,Math.floorMod(z,SIZE))]&255;}
    }
    Mesh mesh(int cx,int cz){Neighborhood area=new Neighborhood(cx,cz);Floats o=new Floats(),w=new Floats();int ox=cx*SIZE,oz=cz*SIZE;for(int z=oz;z<oz+SIZE;z++)for(int x=ox;x<ox+SIZE;x++)for(int y=1;y<HEIGHT;y++){
        int id=area.block(x,y,z);if(id==AIR)continue;
        if(id==GRASS&&area.block(x,y+1,z)==AIR&&hash(x,0,z,seed+71)<.32f){
            for(int blade=0;blade<3;blade++){
                float px=x+.2f+hash(x,blade,z,seed+73)*.6f,pz=z+.2f+hash(x,blade,z,seed+74)*.6f;
                float angle=hash(x,blade,z,seed+75)*6.283185f,dx=(float)Math.cos(angle)*.035f,dz=(float)Math.sin(angle)*.035f,ht=.18f+hash(x,blade,z,seed+76)*.22f;
                float[][] points={{px-dx,y+1.003f,pz-dz},{px+dx,y+1.003f,pz+dz},{px+dz,y+1+ht,pz-dx}};
                for(int[] order:new int[][]{{0,1,2},{0,2,1}})for(int j:order){float[] v=points[j];o.add(v[0]);o.add(v[1]);o.add(v[2]);o.add(-dz/.035f);o.add(0);o.add(dx/.035f);o.add(10);}
            }
        }
        for(int f=0;f<6;f++){int[] n=N[f];int next=area.block(x+n[0],y+n[1],z+n[2]);if(id==WATER?next!=AIR:next!=AIR&&next!=WATER&&!(next==LEAF&&id!=LEAF))continue;Floats out=id==WATER?w:o;for(int i:ORDER){int[] v=V[f][i];out.add(x+v[0]);out.add(y+v[1]-(id==WATER&&v[1]==1&&area.block(x,y+1,z)!=WATER?.10f:0));out.add(z+v[2]);out.add(n[0]);out.add(n[1]);out.add(n[2]);out.add(id);}}
    }return new Mesh(o.result(),w.result());}
}
