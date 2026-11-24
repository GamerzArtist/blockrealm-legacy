package com.voxelforge.game;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Tiny original PCM synthesizer for native UI, mining, placement and flight. */
final class ProceduralAudio implements AutoCloseable {
    private final SoundPool pool;
    private final Map<String,Integer> samples=new HashMap<>();
    private final Set<Integer> loaded=new HashSet<>();
    ProceduralAudio(Context ctx){
        pool=new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
        pool.setOnLoadCompleteListener((p,id,status)->{if(status==0)loaded.add(id);});
        for(String name:new String[]{"ui","mine","place","fly"})try{
            File f=new File(ctx.getCacheDir(),"vf_"+name+"_v1.wav");
            try(FileOutputStream out=new FileOutputStream(f)){out.write(wave(name));}
            samples.put(name,pool.load(f.getAbsolutePath(),1));
        }catch(Exception ex){Log.w("VoxelForge","Procedural audio initialization failed",ex);}
    }
    private byte[] wave(String name){int rate=22050,n=(int)(rate*(name.equals("mine")?.16:.105));ByteBuffer b=ByteBuffer.allocate(44+2*n).order(ByteOrder.LITTLE_ENDIAN);b.put(new byte[]{'R','I','F','F'}).putInt(36+n*2).put(new byte[]{'W','A','V','E','f','m','t',' '}).putInt(16).putShort((short)1).putShort((short)1).putInt(rate).putInt(rate*2).putShort((short)2).putShort((short)16).put(new byte[]{'d','a','t','a'}).putInt(n*2);Random rng=new Random(342+name.hashCode());double filter=0;
        for(int i=0;i<n;i++){double t=i/(double)rate,fade=Math.min(1,i/35.0)*Math.pow(1-i/(double)n,2.5),noise=rng.nextDouble()*2-1;filter=filter*.71+noise*.29;double sample;
            switch(name){case "mine":sample=filter*.62+Math.sin(t*2*Math.PI*(125-350*t))*.22;break;case "place":sample=filter*.40+Math.sin(t*2*Math.PI*(210-500*t))*.35;break;case "fly":sample=Math.sin(t*2*Math.PI*(390+1900*t))*.55;break;default:sample=Math.sin(t*2*Math.PI*540)*.38+Math.sin(t*2*Math.PI*810)*.15;}
            b.putShort((short)(sample*fade*17000));
        }return b.array();
    }
    void play(String name){Integer id=samples.get(name);if(id!=null&&loaded.contains(id))pool.play(id,.62f,.62f,1,0,1f);}
    @Override public void close(){pool.release();}
}
