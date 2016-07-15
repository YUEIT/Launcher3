/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wos.launcher3;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.wos.launcher3.compat.LauncherAppsCompat;
import com.wos.launcher3.compat.PackageInstallerCompat.PackageInstallInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

//A:luobiao@wind-mobi.com 2015-8-14 begin
//A:luobiao@wind-mobi.com 2015-8-14 end
public class LauncherAppState implements DeviceProfile.DeviceProfileCallbacks {
    private static final String TAG = "LauncherAppState";
    private static final String SHARED_PREFERENCES_KEY = "com.wos.launcher3.prefs";

    private static final boolean DEBUG = false;

    private final AppFilter mAppFilter;
    private final BuildInfo mBuildInfo;
    private LauncherModel mModel;
    private IconCache mIconCache;
    private WidgetPreviewLoader.CacheDb mWidgetPreviewCacheDb;
    private boolean mIsScreenLarge;
    private float mScreenDensity;
    private int mLongPressTimeout = 300;
    private boolean mWallpaperChangedSinceLastCheck;

    private static WeakReference<LauncherProvider> sLauncherProvider;
    private static Context sContext;

    private static LauncherAppState INSTANCE;

    private DynamicGrid mDynamicGrid;
    //A: luobiao@wind-mobi.com 2015-7-28
    public static boolean WOS_OVERVIEW_PANEL = true;
    public static int DEFAULT_THEME_INDEX = 6;
    public static int effectIndex = 0;
    public static boolean SHORTCUT_THEME_KEY = false;//open:true
    public static boolean themeKey = false;
    public static boolean WOS_FOLDER_KEY = true; //open:true
    public static boolean INTERNET_THEME = true;
    private LauncherApplication mApp;
    private PowerManager mPowerManager;
    public boolean isPowerSaveMode(){
        PowerManager powerManager = (PowerManager)sContext.getSystemService(sContext.POWER_SERVICE);
        return mPowerManager.isPowerSaveMode();
    }
    //A:luobiao@wind-mobi.com 2015-7-28
    //A: zhangxutong@wind-mobi.com 2015 07 02 begin
    public final static String ACTION_WOS_CHANGE_THEME = "com.wos.launcher3.action.CHANGE_THEME";
    public final static String ACTION_WOS_OVERSCROLL_EFFECT = "com.wos.launcher3.action.OVERSCROLL_EFFECT";
    //A: zhangxutong@wind-mobi.com 2015 07 02 end

    public static LauncherAppState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LauncherAppState();
        }
        return INSTANCE;
    }

    public static LauncherAppState getInstanceNoCreate() {
        return INSTANCE;
    }

    public Context getContext() {
        return sContext;
    }

    public static void setApplicationContext(Context context) {
        if (sContext != null) {
            Log.w(Launcher.TAG, "setApplicationContext called twice! old=" + sContext + " new=" + context);
        }
        sContext = context.getApplicationContext();
    }

    private LauncherAppState() {
        if (sContext == null) {
            throw new IllegalStateException("LauncherAppState inited before app context set");
        }

        Log.v(Launcher.TAG, "LauncherAppState inited");

        if (sContext.getResources().getBoolean(R.bool.debug_memory_enabled)) {
            MemoryTracker.startTrackingMe(sContext, "L");
        }

        // set sIsScreenXLarge and mScreenDensity *before* creating icon cache
        mIsScreenLarge = isScreenLarge(sContext.getResources());
        mScreenDensity = sContext.getResources().getDisplayMetrics().density;

        recreateWidgetPreviewDb();
        mIconCache = new IconCache(sContext);

        mAppFilter = AppFilter.loadByName(sContext.getString(R.string.app_filter_class));
        mBuildInfo = BuildInfo.loadByName(sContext.getString(R.string.build_info_class));
        mModel = new LauncherModel(this, mIconCache, mAppFilter);
        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(sContext);
        launcherApps.addOnAppsChangedCallback(mModel);

        // Register intent receivers
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        //A: zhangxutong@wind-mobi.com 2015 07 02 begin
        filter.addAction(ACTION_WOS_CHANGE_THEME);
        //A: zhangxutong@wind-mobi.com 2015 07 02 end
        sContext.registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED);
        sContext.registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED);
        sContext.registerReceiver(mModel, filter);

        // Register for changes to the favorites
        ContentResolver resolver = sContext.getContentResolver();
        resolver.registerContentObserver(LauncherSettings.Favorites.CONTENT_URI, true,
                mFavoritesObserver);
        //A:luobiao@wind-mobi.com 2015-8-18 begin
        mPowerManager = (PowerManager)sContext.getSystemService(Context.POWER_SERVICE);
        //A:luobiao@wind-mobi.com 2015-8-18 end
    }

    public void recreateWidgetPreviewDb() {
        if (mWidgetPreviewCacheDb != null) {
            mWidgetPreviewCacheDb.close();
        }
        mWidgetPreviewCacheDb = new WidgetPreviewLoader.CacheDb(sContext);
    }

    /**
     * Call from Application.onTerminate(), which is not guaranteed to ever be called.
     */
    public void onTerminate() {
        sContext.unregisterReceiver(mModel);
        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(sContext);
        launcherApps.removeOnAppsChangedCallback(mModel);

        ContentResolver resolver = sContext.getContentResolver();
        resolver.unregisterContentObserver(mFavoritesObserver);
    }

    /**
     * Receives notifications whenever the user favorites have changed.
     */
    private final ContentObserver mFavoritesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // If the database has ever changed, then we really need to force a reload of the
            // workspace on the next load
            mModel.resetLoadedState(false, true);
            mModel.startLoaderFromBackground();
        }
    };

    LauncherModel setLauncher(Launcher launcher) {
        if (mModel == null) {
            throw new IllegalStateException("setLauncher() called before init()");
        }
        mModel.initialize(launcher);
        return mModel;
    }

    public IconCache getIconCache() {
        return mIconCache;
    }

    LauncherModel getModel() {
        return mModel;
    }

    boolean shouldShowAppOrWidgetProvider(ComponentName componentName) {
        return mAppFilter == null || mAppFilter.shouldShowApp(componentName);
    }

    WidgetPreviewLoader.CacheDb getWidgetPreviewCacheDb() {
        return mWidgetPreviewCacheDb;
    }

    static void setLauncherProvider(LauncherProvider provider) {
        sLauncherProvider = new WeakReference<LauncherProvider>(provider);
    }

    static LauncherProvider getLauncherProvider() {
        return sLauncherProvider.get();
    }

    public static String getSharedPreferencesKey() {
        return SHARED_PREFERENCES_KEY;
    }

    DeviceProfile initDynamicGrid(Context context, int minWidth, int minHeight,
                                  int width, int height,
                                  int availableWidth, int availableHeight) {
        if (mDynamicGrid == null) {
            mDynamicGrid = new DynamicGrid(context,
                    context.getResources(),
                    minWidth, minHeight, width, height,
                    availableWidth, availableHeight);
            mDynamicGrid.getDeviceProfile().addCallback(this);
        }

        // Update the icon size
        DeviceProfile grid = mDynamicGrid.getDeviceProfile();
        grid.updateFromConfiguration(context, context.getResources(), width, height,
                availableWidth, availableHeight);
        return grid;
    }
    public DynamicGrid getDynamicGrid() {
        return mDynamicGrid;
    }

    public boolean isScreenLarge() {
        return mIsScreenLarge;
    }

    // Need a version that doesn't require an instance of LauncherAppState for the wallpaper picker
    public static boolean isScreenLarge(Resources res) {
        return res.getBoolean(R.bool.is_large_tablet);
    }

    public static boolean isScreenLandscape(Context context) {
        return context.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
    }

    public float getScreenDensity() {
        return mScreenDensity;
    }

    public int getLongPressTimeout() {
        return mLongPressTimeout;
    }

    public void onWallpaperChanged() {
        mWallpaperChangedSinceLastCheck = true;
    }

    public boolean hasWallpaperChangedSinceLastCheck() {
        boolean result = mWallpaperChangedSinceLastCheck;
        mWallpaperChangedSinceLastCheck = false;
        return result;
    }

    @Override
    public void onAvailableSizeChanged(DeviceProfile grid) {
        Utilities.setIconSize(grid.iconSizePx);
    }

    public static boolean isDisableAllApps() {
        // Returns false on non-dogfood builds.
        //M: zhangxutong@wind-mobi.com 2015 07 06 begin
        /*return getInstance().mBuildInfo.isDogfoodBuild() &&
                Launcher.isPropertyEnabled(Launcher.DISABLE_ALL_APPS_PROPERTY);*/
        return true;
        //M: zhangxutong@wind-mobi.com 2015 07 06 end
    }

    public static boolean isDogfoodBuild() {
        return getInstance().mBuildInfo.isDogfoodBuild();
    }

    public void setPackageState(ArrayList<PackageInstallInfo> installInfo) {
        mModel.setPackageState(installInfo);
    }

    /**
     * Updates the icons and label of all icons for the provided package name.
     */
    public void updatePackageBadge(String packageName) {
        mModel.updatePackageBadge(packageName);
    }
    //A:luobiao@wind-mobi.com 2015-8-14 begin
    public void setLauncehrApplication(LauncherApplication app){
        mApp = app;
    }

    public LauncherApplication getLauncehrApplication(){
        return mApp;
    }

    public PowerManager getPowerManager() {
        return mPowerManager;
    }
    //A:luobiao@wind-mobi.com 2015-8-14 end
}
