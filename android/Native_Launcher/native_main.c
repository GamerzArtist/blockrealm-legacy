#include <jni.h>
#ifndef NULL
#define NULL ((void*)0)
#endif

typedef struct ANativeActivity ANativeActivity;
typedef struct ANativeActivityCallbacks {
    void (*onStart)(ANativeActivity*);
    void (*onResume)(ANativeActivity*);
    void* (*onSaveInstanceState)(ANativeActivity*, size_t*);
    void (*onPause)(ANativeActivity*);
    void (*onStop)(ANativeActivity*);
    void (*onDestroy)(ANativeActivity*);
    void (*onWindowFocusChanged)(ANativeActivity*, int);
    void (*onNativeWindowCreated)(ANativeActivity*, void*);
    void (*onNativeWindowResized)(ANativeActivity*, void*);
    void (*onNativeWindowRedrawNeeded)(ANativeActivity*, void*);
    void (*onNativeWindowDestroyed)(ANativeActivity*, void*);
    void (*onInputQueueCreated)(ANativeActivity*, void*);
    void (*onInputQueueDestroyed)(ANativeActivity*, void*);
    void (*onContentRectChanged)(ANativeActivity*, const void*);
    void (*onConfigurationChanged)(ANativeActivity*);
    void (*onLowMemory)(ANativeActivity*);
} ANativeActivityCallbacks;

struct ANativeActivity {
    ANativeActivityCallbacks* callbacks;
    JavaVM* vm;
    JNIEnv* env;
    jobject clazz;
    const char* internalDataPath;
    const char* externalDataPath;
    jint sdkVersion;
    void* instance;
    void* assetManager;
    const char* obbPath;
};

static jobject g_webview = NULL;
static jobject g_root = NULL;

static void clear_exception(JNIEnv* env) {
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionClear(env);
    }
}

static jmethodID method(JNIEnv* env, jclass cls, const char* name, const char* sig) {
    jmethodID id = (*env)->GetMethodID(env, cls, name, sig);
    clear_exception(env);
    return id;
}

static void immersive(ANativeActivity* activity) {
    JNIEnv* env = activity->env;
    jobject act = activity->clazz;
    jclass actClass = (*env)->GetObjectClass(env, act);
    jmethodID getWindow = method(env, actClass, "getWindow", "()Landroid/view/Window;");
    if (!getWindow) return;
    jobject window = (*env)->CallObjectMethod(env, act, getWindow);
    clear_exception(env);
    if (!window) return;
    jclass winClass = (*env)->GetObjectClass(env, window);
    jmethodID addFlags = method(env, winClass, "addFlags", "(I)V");
    if (addFlags) (*env)->CallVoidMethod(env, window, addFlags, (jint)(0x00000400 | 0x00000080 | 0x01000000));
    clear_exception(env);
    jmethodID getDecor = method(env, winClass, "getDecorView", "()Landroid/view/View;");
    if (getDecor) {
        jobject decor = (*env)->CallObjectMethod(env, window, getDecor);
        clear_exception(env);
        if (decor) {
            jclass viewClass = (*env)->GetObjectClass(env, decor);
            jmethodID setUi = method(env, viewClass, "setSystemUiVisibility", "(I)V");
            if (setUi) {
                jint flags = 0x00001000 | 0x00000004 | 0x00000002 | 0x00000100 | 0x00000200 | 0x00000400;
                (*env)->CallVoidMethod(env, decor, setUi, flags);
                clear_exception(env);
            }
            (*env)->DeleteLocalRef(env, viewClass);
            (*env)->DeleteLocalRef(env, decor);
        }
    }
    (*env)->DeleteLocalRef(env, winClass);
    (*env)->DeleteLocalRef(env, window);
    (*env)->DeleteLocalRef(env, actClass);
}

static void call_bool(JNIEnv* env, jobject obj, jclass cls, const char* name, jboolean value) {
    jmethodID id = method(env, cls, name, "(Z)V");
    if (id) (*env)->CallVoidMethod(env, obj, id, value);
    clear_exception(env);
}

static void create_webview(ANativeActivity* activity) {
    if (g_webview) return;
    JNIEnv* env = activity->env;
    jobject act = activity->clazz;

    immersive(activity);

    /* Root FrameLayout keeps a visible boot message behind the WebView. */
    jclass frameClass = (*env)->FindClass(env, "android/widget/FrameLayout");
    clear_exception(env);
    jmethodID frameCtor = frameClass ? method(env, frameClass, "<init>", "(Landroid/content/Context;)V") : NULL;
    jobject root = frameCtor ? (*env)->NewObject(env, frameClass, frameCtor, act) : NULL;
    clear_exception(env);

    jclass textClass = (*env)->FindClass(env, "android/widget/TextView");
    clear_exception(env);
    jmethodID textCtor = textClass ? method(env, textClass, "<init>", "(Landroid/content/Context;)V") : NULL;
    jobject text = textCtor ? (*env)->NewObject(env, textClass, textCtor, act) : NULL;
    clear_exception(env);
    if (text) {
        jmethodID setText = method(env, textClass, "setText", "(Ljava/lang/CharSequence;)V");
        jmethodID setColor = method(env, textClass, "setTextColor", "(I)V");
        jmethodID setSize = method(env, textClass, "setTextSize", "(F)V");
        jmethodID setGravity = method(env, textClass, "setGravity", "(I)V");
        jmethodID setBg = method(env, textClass, "setBackgroundColor", "(I)V");
        jstring msg = (*env)->NewStringUTF(env, "BLOCKREALM: LEGACY 4.0.0\n\nNATIVE LAUNCHER READY\nLOADING OFFLINE GAME…\n\nIf this remains visible, update Android System WebView or Chrome.");
        if (setText) (*env)->CallVoidMethod(env, text, setText, msg);
        if (setColor) (*env)->CallVoidMethod(env, text, setColor, (jint)0xFFFFFFFF);
        if (setSize) (*env)->CallVoidMethod(env, text, setSize, (jfloat)16.0f);
        if (setGravity) (*env)->CallVoidMethod(env, text, setGravity, (jint)17);
        if (setBg) (*env)->CallVoidMethod(env, text, setBg, (jint)0xFF0B0E0C);
        clear_exception(env);
        (*env)->DeleteLocalRef(env, msg);
    }

    jclass webClass = (*env)->FindClass(env, "android/webkit/WebView");
    clear_exception(env);
    jmethodID webCtor = webClass ? method(env, webClass, "<init>", "(Landroid/content/Context;)V") : NULL;
    jobject web = webCtor ? (*env)->NewObject(env, webClass, webCtor, act) : NULL;
    clear_exception(env);
    if (!root || !web) return;

    /* Plain clients keep local navigation inside the app and enable normal WebView page handling. */
    jclass clientClass = (*env)->FindClass(env, "android/webkit/WebViewClient");
    clear_exception(env);
    jmethodID clientCtor = clientClass ? method(env, clientClass, "<init>", "()V") : NULL;
    jobject client = clientCtor ? (*env)->NewObject(env, clientClass, clientCtor) : NULL;
    clear_exception(env);
    jmethodID setClient = method(env, webClass, "setWebViewClient", "(Landroid/webkit/WebViewClient;)V");
    if (setClient && client) (*env)->CallVoidMethod(env, web, setClient, client);
    clear_exception(env);

    jclass chromeClass = (*env)->FindClass(env, "android/webkit/WebChromeClient");
    clear_exception(env);
    jmethodID chromeCtor = chromeClass ? method(env, chromeClass, "<init>", "()V") : NULL;
    jobject chrome = chromeCtor ? (*env)->NewObject(env, chromeClass, chromeCtor) : NULL;
    clear_exception(env);
    jmethodID setChrome = method(env, webClass, "setWebChromeClient", "(Landroid/webkit/WebChromeClient;)V");
    if (setChrome && chrome) (*env)->CallVoidMethod(env, web, setChrome, chrome);
    clear_exception(env);

    jmethodID getSettings = method(env, webClass, "getSettings", "()Landroid/webkit/WebSettings;");
    jobject settings = getSettings ? (*env)->CallObjectMethod(env, web, getSettings) : NULL;
    clear_exception(env);
    if (settings) {
        jclass settingsClass = (*env)->GetObjectClass(env, settings);
        call_bool(env, settings, settingsClass, "setJavaScriptEnabled", JNI_TRUE);
        call_bool(env, settings, settingsClass, "setDomStorageEnabled", JNI_TRUE);
        call_bool(env, settings, settingsClass, "setDatabaseEnabled", JNI_TRUE);
        call_bool(env, settings, settingsClass, "setAllowFileAccess", JNI_TRUE);
        call_bool(env, settings, settingsClass, "setAllowContentAccess", JNI_FALSE);
        call_bool(env, settings, settingsClass, "setMediaPlaybackRequiresUserGesture", JNI_FALSE);
        call_bool(env, settings, settingsClass, "setBuiltInZoomControls", JNI_FALSE);
        call_bool(env, settings, settingsClass, "setDisplayZoomControls", JNI_FALSE);
        call_bool(env, settings, settingsClass, "setSupportZoom", JNI_FALSE);
        call_bool(env, settings, settingsClass, "setUseWideViewPort", JNI_TRUE);
        (*env)->DeleteLocalRef(env, settingsClass);
        (*env)->DeleteLocalRef(env, settings);
    }

    jmethodID setBg = method(env, webClass, "setBackgroundColor", "(I)V");
    if (setBg) (*env)->CallVoidMethod(env, web, setBg, (jint)0x00000000);
    jmethodID setLayer = method(env, webClass, "setLayerType", "(ILandroid/graphics/Paint;)V");
    if (setLayer) (*env)->CallVoidMethod(env, web, setLayer, (jint)2, NULL);
    clear_exception(env);

    jmethodID addView = method(env, frameClass, "addView", "(Landroid/view/View;)V");
    if (addView && text) (*env)->CallVoidMethod(env, root, addView, text);
    if (addView) (*env)->CallVoidMethod(env, root, addView, web);
    clear_exception(env);

    jclass actClass = (*env)->GetObjectClass(env, act);
    jmethodID setContent = method(env, actClass, "setContentView", "(Landroid/view/View;)V");
    if (setContent) (*env)->CallVoidMethod(env, act, setContent, root);
    clear_exception(env);

    jmethodID loadUrl = method(env, webClass, "loadUrl", "(Ljava/lang/String;)V");
    if (loadUrl) {
        jstring url = (*env)->NewStringUTF(env, "file:///android_asset/index.html?android=1&build=40000");
        (*env)->CallVoidMethod(env, web, loadUrl, url);
        clear_exception(env);
        (*env)->DeleteLocalRef(env, url);
    }

    g_webview = (*env)->NewGlobalRef(env, web);
    g_root = (*env)->NewGlobalRef(env, root);

    if (text) (*env)->DeleteLocalRef(env, text);
    (*env)->DeleteLocalRef(env, web);
    (*env)->DeleteLocalRef(env, root);
    if (client) (*env)->DeleteLocalRef(env, client);
    if (clientClass) (*env)->DeleteLocalRef(env, clientClass);
    if (chrome) (*env)->DeleteLocalRef(env, chrome);
    if (chromeClass) (*env)->DeleteLocalRef(env, chromeClass);
    (*env)->DeleteLocalRef(env, actClass);
    (*env)->DeleteLocalRef(env, webClass);
    (*env)->DeleteLocalRef(env, textClass);
    (*env)->DeleteLocalRef(env, frameClass);
}

static void on_start(ANativeActivity* activity) { create_webview(activity); }
static void on_resume(ANativeActivity* activity) {
    if (!g_webview) create_webview(activity);
    immersive(activity);
    if (g_webview) {
        JNIEnv* env = activity->env;
        jclass cls = (*env)->GetObjectClass(env, g_webview);
        jmethodID id = method(env, cls, "onResume", "()V");
        if (id) (*env)->CallVoidMethod(env, g_webview, id);
        clear_exception(env);
        (*env)->DeleteLocalRef(env, cls);
    }
}
static void on_pause(ANativeActivity* activity) {
    if (!g_webview) return;
    JNIEnv* env = activity->env;
    jclass cls = (*env)->GetObjectClass(env, g_webview);
    jmethodID eval = method(env, cls, "evaluateJavascript", "(Ljava/lang/String;Landroid/webkit/ValueCallback;)V");
    if (eval) {
        jstring js = (*env)->NewStringUTF(env, "window.BlockRealmAndroidPause&&window.BlockRealmAndroidPause()");
        (*env)->CallVoidMethod(env, g_webview, eval, js, NULL);
        clear_exception(env);
        (*env)->DeleteLocalRef(env, js);
    }
    jmethodID id = method(env, cls, "onPause", "()V");
    if (id) (*env)->CallVoidMethod(env, g_webview, id);
    clear_exception(env);
    (*env)->DeleteLocalRef(env, cls);
}
static void on_focus(ANativeActivity* activity, int focused) { if (focused) immersive(activity); }
static void on_destroy(ANativeActivity* activity) {
    JNIEnv* env = activity->env;
    if (g_webview) {
        jclass cls = (*env)->GetObjectClass(env, g_webview);
        jmethodID id = method(env, cls, "destroy", "()V");
        if (id) (*env)->CallVoidMethod(env, g_webview, id);
        clear_exception(env);
        (*env)->DeleteLocalRef(env, cls);
        (*env)->DeleteGlobalRef(env, g_webview);
        g_webview = NULL;
    }
    if (g_root) {
        (*env)->DeleteGlobalRef(env, g_root);
        g_root = NULL;
    }
}

__attribute__((visibility("default")))
void ANativeActivity_onCreate(ANativeActivity* activity, void* savedState, size_t savedStateSize) {
    (void)savedState;
    (void)savedStateSize;
    activity->instance = activity;
    activity->callbacks->onStart = on_start;
    activity->callbacks->onResume = on_resume;
    activity->callbacks->onPause = on_pause;
    activity->callbacks->onWindowFocusChanged = on_focus;
    activity->callbacks->onDestroy = on_destroy;
    immersive(activity);
}
