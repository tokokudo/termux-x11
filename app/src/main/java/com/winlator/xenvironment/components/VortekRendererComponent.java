package com.winlator.xenvironment.components;

import android.util.Log;
import android.view.Surface;
import com.termux.x11.LorieView;
import androidx.annotation.Keep;

public class VortekRendererComponent {
    private static final String TAG = "VORTEK";
    public static final int VK_MAX_VERSION = vkMakeVersion(1, 3, 128);

    private long nativeHandle = 0;
    private LorieView lorieView;
    private Surface surface;
    private final Object surfaceLock = new Object();
    private int width = 1280;
    private int height = 720;

    public VortekRendererComponent(LorieView view) {
        this.lorieView = view;
        if (view != null && view.getHolder() != null) {
            setSurface(view.getHolder().getSurface());
        }
    }

    public static class Options {
        public String[] exposedDeviceExtensions = null;
        public int maxDeviceMemory = 4096;
        public int vkMaxVersion = VK_MAX_VERSION;

        public static Options defaultOptions() {
            Options o = new Options();
            o.vkMaxVersion = VK_MAX_VERSION;
            o.maxDeviceMemory = 4096;
            o.exposedDeviceExtensions = null;
            return o;
        }
    }

    // Native methods
    public native long createVkContext(int fd, Options options);
    public native void destroyVkContext(long ctx);

    @Keep
    private int getWindowWidth(int id) {
        synchronized (surfaceLock) {
            return width;
        }
    }

    @Keep
    private int getWindowHeight(int id) {
        synchronized (surfaceLock) {
            return height;
        }
    }

    @Keep
    private long getWindowHardwareBuffer(int id) {
        synchronized (surfaceLock) {
            if (surface != null && surface.isValid()) {
                try {
                    // Use reflection to access the native surface object
                    return getSurfaceNativeObject(surface);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to get hardware buffer", e);
                }
            }
            return 0;
        }
    }

    @Keep
    private void updateWindowContent(int id) {
        if (lorieView != null) {
            lorieView.postInvalidate();
        }
    }

    public void setSurface(Surface s) {
        synchronized (surfaceLock) {
            this.surface = s;
            if (s != null && s.isValid()) {
                Log.i(TAG, "Surface attached to Vulkan");
            } else {
                Log.w(TAG, "Surface is null or invalid");
            }
        }
    }

    public void setSurfaceDimensions(int width, int height) {
        synchronized (surfaceLock) {
            this.width = width;
            this.height = height;
        }
    }

    public long initContext(int fd, Options opts) {
        if (opts == null) opts = Options.defaultOptions();
        
        synchronized (surfaceLock) {
            if (surface == null || !surface.isValid()) {
                Log.e(TAG, "Cannot init Vortek: surface not ready");
                return 0;
            }

            nativeHandle = createVkContext(fd, opts);
            if (nativeHandle == 0) {
                Log.e(TAG, "Failed to create Vulkan context");
            }
            return nativeHandle;
        }
    }

    public void destroy() {
        if (nativeHandle != 0) {
            destroyVkContext(nativeHandle);
            nativeHandle = 0;
            Log.i(TAG, "Vortek context destroyed");
        }
    }

    public static int vkMakeVersion(int major, int minor, int patch) {
        return (major << 22) | (minor << 12) | patch;
    }

    private long getSurfaceNativeObject(Surface surface) {
        try {
            // Try to get the native object from Surface using reflection
            java.lang.reflect.Field field = surface.getClass().getDeclaredField("mNativeObject");
            field.setAccessible(true);
            return field.getLong(surface);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get surface native object", e);
            return 0;
        }
    }

    static {
        try {
            System.loadLibrary("vortekrenderer");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load vortekrenderer.so", e);
        }
    }
}