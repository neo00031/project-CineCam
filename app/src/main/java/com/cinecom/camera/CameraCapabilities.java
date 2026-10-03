package com.cinecom.camera;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.DynamicRangeProfiles;
import android.os.Build;
import java.util.ArrayList;
import java.util.List;

public final class CameraCapabilities {
    public final boolean tenBit;
    public final boolean hlg10;
    public final boolean hdr10;
    public final boolean hdr10Plus;
    public final List<Long> dynamicProfiles;
    public CameraCapabilities(CameraCharacteristics c) {
        boolean ten=false, hlg=false, hdr=false, plus=false;
        List<Long> p=new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 33) {
            int[] caps=c.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
            if(caps!=null) for(int x:caps) if(x==CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_DYNAMIC_RANGE_TEN_BIT) ten=true;
            DynamicRangeProfiles dr=c.get(CameraCharacteristics.REQUEST_AVAILABLE_DYNAMIC_RANGE_PROFILES);
            if(dr!=null){ p.addAll(dr.getSupportedProfiles()); hlg=p.contains(DynamicRangeProfiles.HLG10); hdr=p.contains(DynamicRangeProfiles.HDR10); plus=p.contains(DynamicRangeProfiles.HDR10_PLUS); }
        }
        tenBit=ten; hlg10=hlg; hdr10=hdr; hdr10Plus=plus; dynamicProfiles=p;
    }
}
