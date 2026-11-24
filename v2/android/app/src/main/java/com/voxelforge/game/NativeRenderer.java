package com.voxelforge.game;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import static android.opengl.GLES30.*;

/** Actual native OpenGL ES 3 renderer. GL resources are owned by the GL thread. */
final class NativeRenderer implements GLSurfaceView.Renderer {
    interface Listener {void status(String text);void ready();void failure(String text);}
    private static final String TAG="VoxelForge";
    private static final int RADIUS=2;
    final VoxelWorld world;
    final Listener listener;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final ConcurrentLinkedQueue<Result> results=new ConcurrentLinkedQueue<>();
    private final Map<Long,GpuMesh> meshes=new HashMap<>();
    private final Set<Long> queued=new HashSet<>();
    private final Map<Long,Integer> revisions=new HashMap<>();
    private final float[] projection=new float[16],view=new float[16],vp=new float[16];
    private int program,sky,vao,texture,width=1,height=1,frame,epoch=0;
    private long lastNs=0;private double accumulator=0;private float clock=0;
    private boolean initialized,ready,failed;private volatile boolean paused=true;
    private volatile State saved;
    float x,y,z,yaw,pitch,oldX,oldY,oldZ,vy,moveX,moveZ;boolean jump,fly;int selected=1;
    private int eyeBlock=0;
    static final class State {final float x,y,z,yaw,pitch;final int selected;State(float x,float y,float z,float yaw,float pitch,int selected){this.x=x;this.y=y;this.z=z;this.yaw=yaw;this.pitch=pitch;this.selected=selected;}}
    static final class Result {final int cx,cz,revision,epoch;final VoxelWorld.Mesh mesh;Result(int cx,int cz,int rev,int epoch,VoxelWorld.Mesh m){this.cx=cx;this.cz=cz;revision=rev;this.epoch=epoch;mesh=m;}}
    static final class GpuMesh {final int opaque,water,opaqueCount,waterCount;GpuMesh(int o,int w,int a,int b){opaque=o;water=w;opaqueCount=a;waterCount=b;}void delete(){glDeleteBuffers(2,new int[]{opaque,water},0);}}
    NativeRenderer(VoxelWorld world,State s,Listener listener){this.world=world;this.listener=listener;x=s.x;y=s.y;z=s.z;yaw=s.yaw;pitch=s.pitch;selected=s.selected;oldX=x;oldY=y;oldZ=z;saved=s;}
    State state(){return saved;}
    void setPaused(boolean v){paused=v;}
    void move(float dx,float dz){moveX=dx;moveZ=dz;}
    void look(float dx,float dy){yaw+=dx;pitch=Math.max(-1.50f,Math.min(1.50f,pitch-dy));}
    void toggleFly(){fly=!fly;vy=0;}
    void close(){worker.shutdownNow();}
    private static int compile(int type,String src){int shader=glCreateShader(type);glShaderSource(shader,src);glCompileShader(shader);int[] ok=new int[1];glGetShaderiv(shader,GL_COMPILE_STATUS,ok,0);if(ok[0]==0){String log=glGetShaderInfoLog(shader);glDeleteShader(shader);throw new IllegalStateException(log);}return shader;}
    private static int link(String vs,String fs){int v=compile(GL_VERTEX_SHADER,vs),f=compile(GL_FRAGMENT_SHADER,fs),p=glCreateProgram();glAttachShader(p,v);glAttachShader(p,f);glLinkProgram(p);glDeleteShader(v);glDeleteShader(f);int[] ok=new int[1];glGetProgramiv(p,GL_LINK_STATUS,ok,0);if(ok[0]==0)throw new IllegalStateException(glGetProgramInfoLog(p));return p;}
    private static final String VERT="""
        #version 300 es
        layout(location=0) in vec3 vertex;layout(location=1) in vec3 normal;layout(location=2) in float id;
        uniform mat4 vp;uniform float time;out vec3 p,n,rest;flat out int material;
        void main(){rest=vertex;p=vertex;n=normal;material=int(id+.5);if(material==6)p.xz+=vec2(sin(time*1.4+p.x*.16+p.z*.19),cos(time*1.1+p.z*.2))*.095;if(material==10)p.xz+=fract(p.y)*vec2(sin(time*2.+p.x*.6+p.z*.3),cos(time*1.5+p.z*.5))*.19;if(material==8&&normal.y>.5)p.y+=sin(p.x*1.9+p.z*.9-time*1.4)*.024+sin(p.z*2.8-time*.9)*.013;gl_Position=vp*vec4(p,1.);}
        """;
    private static final String FRAG="""
        #version 300 es
        precision highp float;precision highp sampler2DArray;in vec3 p,n,rest;flat in int material;out vec4 frag;
        uniform sampler2DArray tiles;uniform vec3 eye;uniform float time;uniform bool underwater;
        vec3 sky(vec3 d){return mix(vec3(.69,.80,.88),vec3(.26,.51,.82),pow(max(d.y,0.),.46));}
        float hash(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
        void main(){vec3 N=normalize(n),sun=normalize(vec3(.48,.77,-.41));vec3 q=rest-N*.001;vec2 uv=abs(N.y)>.5?q.xz:abs(N.x)>.5?q.zy:q.xy;int face=N.y>.5?0:N.y<-.5?2:1;vec4 tile=texture(tiles,vec3(uv,float(min(material,9)*3+face)));if(material==6&&tile.a<.43)discard;float light=.55+.45*max(dot(N,sun),0.);vec3 color=material==10?mix(vec3(.20,.38,.12),vec3(.42,.63,.22),clamp(fract(rest.y)*2.,0.,1.))*light:tile.rgb*light;float dist=length(p-eye);float haze=smoothstep(31.,67.,dist);if(material==8){vec2 k=p.xz;vec2 slope=vec2(cos(k.x*1.9+k.y*.9-time*1.4)*.075,cos(k.y*2.8-time*.9)*.05);vec3 W=normalize(N+vec3(slope.x,0.,slope.y)),V=normalize(eye-p);float fres=.03+.97*pow(1.-abs(dot(W,V)),5.);vec3 reflected=sky(reflect(-V,W));float spec=pow(max(0.,dot(reflect(-sun,W),V)),75.);color=mix(vec3(.13,.47,.55),reflected,fres)+spec*.25;frag=vec4(mix(color,sky(normalize(p-eye)),haze),mix(.31,.94,fres));return;}if(material==9)color=mix(color,vec3(1.,.71,.24),.65+.1*sin(time*7.));color*=.94+.12*hash(floor(q.xz));color=mix(color,sky(normalize(p-eye)),haze);if(underwater)color=mix(color,vec3(.055,.28,.36),1.-exp(-dist*.12));frag=vec4(color,1.);}
        """;
    private static final String SKY_V="""
        #version 300 es
        out vec2 uv;void main(){uv=vec2(float((gl_VertexID<<1)&2),float(gl_VertexID&2));gl_Position=vec4(uv*2.-1.,.999,1.);}
        """;
    private static final String SKY_F="""
        #version 300 es
        precision highp float;in vec2 uv;out vec4 frag;uniform vec3 forward,right,up;uniform float aspect,time;
        float h(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
        float noise(vec2 p){vec2 i=floor(p),f=fract(p);f=f*f*(3.-2.*f);return mix(mix(h(i),h(i+vec2(1,0)),f.x),mix(h(i+vec2(0,1)),h(i+1.),f.x),f.y);}
        void main(){vec2 v=(uv*2.-1.)*vec2(aspect,1.)*.767;vec3 d=normalize(forward+right*v.x+up*v.y);vec3 sun=normalize(vec3(.48,.77,-.41));float a=max(0.,d.y);vec3 c=mix(vec3(.69,.80,.88),vec3(.26,.51,.82),pow(a,.46));float s=max(0.,dot(d,sun));c+=vec3(.24,.17,.07)*pow(s,35.)+vec3(1.,.84,.48)*smoothstep(.9991,.9996,s);if(a>.04){vec2 q=d.xz/max(.12,a)*1.8+vec2(time*.015,time*.004);float n=noise(q)*.62+noise(q*2.03)*.28+noise(q*4.1)*.10;float cl=smoothstep(.54,.72,n)*smoothstep(.06,.25,a);c=mix(c,mix(vec3(.74,.80,.85),vec3(1.,.98,.91),smoothstep(.54,.67,n)),cl);}frag=vec4(clamp(c,0.,1.),1.);}
        """;
    @Override public void onSurfaceCreated(GL10 ignored,EGLConfig config){try{
        epoch++;meshes.clear();queued.clear();revisions.clear();results.clear();program=link(VERT,FRAG);sky=link(SKY_V,SKY_F);int[] ids=new int[1];glGenVertexArrays(1,ids,0);vao=ids[0];glBindVertexArray(vao);texture=makeTextures();glEnable(GL_DEPTH_TEST);glEnable(GL_CULL_FACE);glCullFace(GL_BACK);initialized=true;failed=false;ready=false;lastNs=0;frame=0;Log.i(TAG,"VF_GL "+glGetString(GL_VERSION));
        }catch(RuntimeException ex){failed=true;Log.e(TAG,"VF_GL_FAILED",ex);listener.failure("Graphics initialization failed: "+ex.getMessage());}}
    @Override public void onSurfaceChanged(GL10 ignored,int w,int h){width=Math.max(1,w);height=Math.max(1,h);glViewport(0,0,width,height);Matrix.perspectiveM(projection,0,75,width/(float)height,.06f,100);}
    private int makeTextures(){int[] a=new int[1];glGenTextures(1,a,0);glBindTexture(GL_TEXTURE_2D_ARRAY,a[0]);glTexStorage3D(GL_TEXTURE_2D_ARRAY,6,GL_RGBA8,32,32,VoxelWorld.NAMES.length*3);int[][] colors={{0,0,0},{107,151,62},{124,84,52},{134,139,135},{219,199,150},{123,84,48},{74,125,48},{187,142,81},{50,123,157},{234,177,73}};
        for(int id=0;id<colors.length;id++)for(int face=0;face<3;face++){ByteBuffer bytes=ByteBuffer.allocateDirect(4096);for(int y=0;y<32;y++)for(int x=0;x<32;x++){float r=VoxelWorld.hash(x,y,id,973),p=VoxelWorld.hash(x/4,y/4,id,428);int[] c=colors[id];float f=.79f+r*.11f+p*.26f;int alpha=255;if(id==1&&face!=0&&!(face==1&&y>=24+(int)(VoxelWorld.hash(x/3,0,0,8)*5)))c=colors[2];if(id==5&&face==1)f=.65f+.52f*VoxelWorld.hash(x/2,y/12,0,731);if(id==5&&face!=1){c=colors[7];f=(int)Math.hypot(x-15.5,y-15.5)%4==0?.65f:1;}if(id==6&&p<.16f&&r<.7f)alpha=0;if(id==7&&y%8==0)f=.49f;for(int k=0;k<3;k++)bytes.put((byte)Math.max(0,Math.min(255,(int)(c[k]*f))));bytes.put((byte)alpha);}bytes.flip();glTexSubImage3D(GL_TEXTURE_2D_ARRAY,0,0,0,id*3+face,32,32,1,GL_RGBA,GL_UNSIGNED_BYTE,bytes);}
        glGenerateMipmap(GL_TEXTURE_2D_ARRAY);glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MIN_FILTER,GL_NEAREST_MIPMAP_LINEAR);glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MAG_FILTER,GL_NEAREST);glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_WRAP_S,GL_REPEAT);glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_WRAP_T,GL_REPEAT);return a[0];}
    private int upload(float[] data){int[] id=new int[1];glGenBuffers(1,id,0);glBindBuffer(GL_ARRAY_BUFFER,id[0]);FloatBuffer fb=ByteBuffer.allocateDirect(data.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();fb.put(data).flip();glBufferData(GL_ARRAY_BUFFER,data.length*4,fb,GL_STATIC_DRAW);return id[0];}
    private void schedule(int cx,int cz){long k=VoxelWorld.key(cx,cz);if(queued.contains(k)||meshes.containsKey(k))return;int revision=revisions.getOrDefault(k,0),generation=epoch;queued.add(k);worker.execute(()->{try{results.add(new Result(cx,cz,revision,generation,world.mesh(cx,cz)));}catch(RuntimeException e){Log.e(TAG,"VF_MESH_FAILED",e);}});}
    private void invalidate(int cx,int cz){long k=VoxelWorld.key(cx,cz);revisions.put(k,revisions.getOrDefault(k,0)+1);GpuMesh old=meshes.remove(k);if(old!=null)old.delete();}
    private void stream(){int cx=Math.floorDiv((int)Math.floor(x),16),cz=Math.floorDiv((int)Math.floor(z),16);for(int r=0;r<=RADIUS&&queued.size()<5;r++)for(int dz=-r;dz<=r&&queued.size()<5;dz++)for(int dx=-r;dx<=r&&queued.size()<5;dx++)if(Math.max(Math.abs(dx),Math.abs(dz))==r)schedule(cx+dx,cz+dz);for(var it=meshes.entrySet().iterator();it.hasNext();){var e=it.next();int a=(int)(e.getKey()>>32),b=(int)(long)e.getKey();if(Math.max(Math.abs(a-cx),Math.abs(b-cz))>RADIUS+1){e.getValue().delete();it.remove();}}
        for(int i=0;i<2;i++){Result result=results.poll();if(result==null)break;if(result.epoch!=epoch)continue;long k=VoxelWorld.key(result.cx,result.cz);queued.remove(k);if(result.revision!=revisions.getOrDefault(k,0)||Math.max(Math.abs(result.cx-cx),Math.abs(result.cz-cz))>RADIUS+1)continue;GpuMesh old=meshes.put(k,new GpuMesh(upload(result.mesh.opaque),upload(result.mesh.water),result.mesh.opaque.length/7,result.mesh.water.length/7));if(old!=null)old.delete();}}
    private boolean collision(float px,float py,float pz){for(int a=(int)Math.floor(px-.29f);a<=Math.floor(px+.29f);a++)for(int b=(int)Math.floor(py);b<=Math.floor(py+1.68f);b++)for(int c=(int)Math.floor(pz-.29f);c<=Math.floor(pz+.29f);c++)if(VoxelWorld.solid(world.get(a,b,c)))return true;return false;}
    private void tick(){oldX=x;oldY=y;oldZ=z;float forward=-moveZ,strafe=moveX,len=(float)Math.max(1,Math.hypot(forward,strafe));float speed=fly?8:4.4f;float dx=((float)Math.sin(yaw)*forward+(float)Math.cos(yaw)*strafe)*speed*.05f/len,dz=(-(float)Math.cos(yaw)*forward+(float)Math.sin(yaw)*strafe)*speed*.05f/len;boolean water=world.get((int)Math.floor(x),(int)Math.floor(y+.6f),(int)Math.floor(z))==VoxelWorld.WATER;
        if(fly||!collision(x+dx,y,z))x+=dx;if(fly||!collision(x,y,z+dz))z+=dz;
        if(fly){vy=jump?6:0;if(moveZ<-.15)vy+=(float)Math.sin(pitch)*forward*5;}else{boolean grounded=collision(x,y-.08f,z);if(jump&&(grounded||water))vy=water?3.8f:7.2f;vy=Math.max(-24,vy-(water?6:23)*.05f);if(water)vy*=.86f;}float step=vy*.05f;for(int i=0;i<Math.max(1,(int)Math.ceil(Math.abs(step)/.14));i++){float d=step/Math.max(1,(int)Math.ceil(Math.abs(step)/.14));if(fly||!collision(x,y+d,z))y+=d;else{vy=0;break;}}if(y<1){y=world.height((int)x,(int)z)+2;vy=0;}y=Math.min(VoxelWorld.HEIGHT-2,y);saved=new State(x,y,z,yaw,pitch,selected);}
    void edit(boolean place){if(paused||!ready)return;float sx=(float)(Math.sin(yaw)*Math.cos(pitch)),sy=(float)Math.sin(pitch),sz=(float)(-Math.cos(yaw)*Math.cos(pitch));int px=(int)Math.floor(x),py=(int)Math.floor(y+1.6),pz=(int)Math.floor(z);for(float t=.08f;t<6;t+=.065f){int a=(int)Math.floor(x+sx*t),b=(int)Math.floor(y+1.6f+sy*t),c=(int)Math.floor(z+sz*t);int id=world.get(a,b,c);if(VoxelWorld.solid(id)){int tx=place?px:a,ty=place?py:b,tz=place?pz:c;if(ty<1||ty>=VoxelWorld.HEIGHT)return;if(place&&Math.abs(tx+.5-x)<.8&&Math.abs(tz+.5-z)<.8&&ty+1>y&&ty<y+1.7)return;world.set(tx,ty,tz,place?selected:0);int cx=Math.floorDiv(tx,16),cz=Math.floorDiv(tz,16);invalidate(cx,cz);if(Math.floorMod(tx,16)==0)invalidate(cx-1,cz);if(Math.floorMod(tx,16)==15)invalidate(cx+1,cz);if(Math.floorMod(tz,16)==0)invalidate(cx,cz-1);if(Math.floorMod(tz,16)==15)invalidate(cx,cz+1);return;}px=a;py=b;pz=c;}}
    private void draw(int b,int n){if(n==0)return;glBindBuffer(GL_ARRAY_BUFFER,b);glEnableVertexAttribArray(0);glVertexAttribPointer(0,3,GL_FLOAT,false,28,0);glEnableVertexAttribArray(1);glVertexAttribPointer(1,3,GL_FLOAT,false,28,12);glEnableVertexAttribArray(2);glVertexAttribPointer(2,1,GL_FLOAT,false,28,24);glDrawArrays(GL_TRIANGLES,0,n);}
    private void uniform3(int p,String n,float a,float b,float c){glUniform3f(glGetUniformLocation(p,n),a,b,c);}
    @Override public void onDrawFrame(GL10 ignored){if(!initialized||failed)return;try{long now=System.nanoTime();float dt=lastNs==0?.016f:Math.min(.15f,(now-lastNs)/1e9f);lastNs=now;clock+=dt;stream();if(!paused&&ready){accumulator=Math.min(.25,accumulator+dt);while(accumulator>=.05){tick();accumulator-=.05;}}else{accumulator=0;oldX=x;oldY=y;oldZ=z;}float alpha=(float)(accumulator/.05),ex=VoxelWorld.lerp(oldX,x,alpha),ey=VoxelWorld.lerp(oldY,y,alpha)+1.6f,ez=VoxelWorld.lerp(oldZ,z,alpha),fx=(float)(Math.sin(yaw)*Math.cos(pitch)),fy=(float)Math.sin(pitch),fz=(float)(-Math.cos(yaw)*Math.cos(pitch)),rx=(float)Math.cos(yaw),rz=(float)Math.sin(yaw);float upx=-(float)Math.sin(yaw)*fy,upy=(float)Math.cos(pitch),upz=(float)Math.cos(yaw)*fy;
        Matrix.setLookAtM(view,0,ex,ey,ez,ex+fx,ey+fy,ez+fz,0,1,0);Matrix.multiplyMM(vp,0,projection,0,view,0);glClearColor(.36f,.58f,.83f,1);glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);glBindVertexArray(vao);glDisable(GL_DEPTH_TEST);glDisable(GL_CULL_FACE);glUseProgram(sky);uniform3(sky,"forward",fx,fy,fz);uniform3(sky,"right",rx,0,rz);uniform3(sky,"up",upx,upy,upz);glUniform1f(glGetUniformLocation(sky,"aspect"),width/(float)height);glUniform1f(glGetUniformLocation(sky,"time"),clock);glDrawArrays(GL_TRIANGLES,0,3);
        glEnable(GL_DEPTH_TEST);glEnable(GL_CULL_FACE);glUseProgram(program);glUniformMatrix4fv(glGetUniformLocation(program,"vp"),1,false,vp,0);uniform3(program,"eye",ex,ey,ez);glUniform1f(glGetUniformLocation(program,"time"),clock);glActiveTexture(GL_TEXTURE0);glBindTexture(GL_TEXTURE_2D_ARRAY,texture);glUniform1i(glGetUniformLocation(program,"tiles"),0);eyeBlock=world.get((int)Math.floor(ex),(int)Math.floor(ey),(int)Math.floor(ez));glUniform1i(glGetUniformLocation(program,"underwater"),eyeBlock==VoxelWorld.WATER?1:0);for(GpuMesh m:meshes.values())draw(m.opaque,m.opaqueCount);glEnable(GL_BLEND);glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);glDepthMask(false);glDisable(GL_CULL_FACE);ArrayList<Map.Entry<Long,GpuMesh>> list=new ArrayList<>(meshes.entrySet());list.sort((a,b)->Double.compare(distance(b.getKey()),distance(a.getKey())));for(var entry:list)draw(entry.getValue().water,entry.getValue().waterCount);glDepthMask(true);glDisable(GL_BLEND);glEnable(GL_CULL_FACE);
        int error=glGetError();if(error!=GL_NO_ERROR)throw new IllegalStateException("GL error 0x"+Integer.toHexString(error));if(!ready&&meshes.size()>=9){ready=true;listener.ready();Log.i(TAG,"VF_QA_READY chunks="+meshes.size()+" gl=0");}if(++frame%40==0)listener.status((ready?"":"Generating terrain… ")+meshes.size()+" chunks · "+Math.round(1/Math.max(.001f,dt))+" fps · "+(fly?"FLY":"WALK"));
        }catch(RuntimeException e){failed=true;Log.e(TAG,"VF_RENDER_FAILED",e);listener.failure(e.getMessage());}}
    private double distance(long k){return Math.hypot((int)(k>>32)*16+8-x,(int)k*16+8-z);}
}
