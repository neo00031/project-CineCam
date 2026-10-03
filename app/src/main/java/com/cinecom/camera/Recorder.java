package com.cinecom.camera;

import android.media.*;
import android.os.Build;
import android.view.Surface;
import java.io.File;

public final class Recorder {
    private MediaCodec codec; private MediaMuxer muxer; private Surface surface;
    private int track=-1; private volatile boolean started=false, stopping=false; private Thread drainThread;

    public Surface start(int width,int height,int fps,int bitrate,boolean tenBit,long profile,File file) throws Exception {
        String mime=MediaFormat.MIMETYPE_VIDEO_HEVC;
        codec=MediaCodec.createEncoderByType(mime);
        MediaFormat f=MediaFormat.createVideoFormat(mime,width,height);
        f.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        f.setInteger(MediaFormat.KEY_BIT_RATE,bitrate); f.setInteger(MediaFormat.KEY_FRAME_RATE,fps); f.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);
        if(tenBit){
            int p=MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10;
            if(profile==android.hardware.camera2.params.DynamicRangeProfiles.HDR10) p=MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10;
            else if(Build.VERSION.SDK_INT>=34 && profile==android.hardware.camera2.params.DynamicRangeProfiles.HDR10_PLUS) p=MediaCodecInfo.CodecProfileLevel.HEVCProfileMain10HDR10Plus;
            f.setInteger(MediaFormat.KEY_PROFILE,p);
            f.setInteger(MediaFormat.KEY_COLOR_STANDARD,MediaFormat.COLOR_STANDARD_BT2020);
            f.setInteger(MediaFormat.KEY_COLOR_RANGE,MediaFormat.COLOR_RANGE_LIMITED);
            f.setInteger(MediaFormat.KEY_COLOR_TRANSFER, profile==android.hardware.camera2.params.DynamicRangeProfiles.HLG10 ? MediaFormat.COLOR_TRANSFER_HLG : MediaFormat.COLOR_TRANSFER_ST2084);
        }
        codec.configure(f,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE); surface=codec.createInputSurface();
        muxer=new MediaMuxer(file.getAbsolutePath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        codec.start();
        drainThread=new Thread(() -> { while(!stopping){ drain(false); try{Thread.sleep(8);}catch(InterruptedException ignored){} } },"Cinecom-EncoderDrain");
        drainThread.start();
        return surface;
    }

    private synchronized void drain(boolean end) {
        if(codec==null)return;
        MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
        while(true){
            int i=codec.dequeueOutputBuffer(info,0);
            if(i==MediaCodec.INFO_TRY_AGAIN_LATER) break;
            if(i==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                if(started) throw new IllegalStateException("format changed twice");
                track=muxer.addTrack(codec.getOutputFormat()); muxer.start(); started=true;
            } else if(i>=0){
                if((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG)==0 && info.size>0 && started){
                    java.nio.ByteBuffer b=codec.getOutputBuffer(i);
                    if(b!=null){b.position(info.offset);b.limit(info.offset+info.size);muxer.writeSampleData(track,b,info);}
                }
                codec.releaseOutputBuffer(i,false);
                if((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0) break;
            }
        }
    }

    public void stop(){
        stopping=true;
        try{if(codec!=null)codec.signalEndOfInputStream();}catch(Exception ignored){}
        if(drainThread!=null){try{drainThread.join(2000);}catch(InterruptedException ignored){}}
        try{if(codec!=null)drain(true);}catch(Exception ignored){}
        try{if(muxer!=null&&started)muxer.stop();}catch(Exception ignored){}
        try{if(muxer!=null)muxer.release();}catch(Exception ignored){}
        try{if(codec!=null)codec.stop();}catch(Exception ignored){}
        try{if(codec!=null)codec.release();}catch(Exception ignored){}
        codec=null;muxer=null;surface=null;started=false;track=-1;drainThread=null;
    }
}
