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

import android.app.Application;
//A:luobiao@wind-mobi.com 2015-8-14 begin
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
//A:luobiao@wind-mobi.com 2015-8-14 end
public class LauncherApplication extends Application {
    //A:luobiao@wind-mobi.com 2015-8-14 begin
    /// M: flag for starting Launcher from application
    private boolean mTotallyStart = false;

    /// M: added for unread feature.
    private WOSUnreadLoader mUnreadLoader;
    private WOSContentObserver mWOSContentObserver;
    //A:luobiao@wind-mobi.com 2015-8-14 end
    @Override
    public void onCreate() {
        super.onCreate();
        LauncherAppState.setApplicationContext(this);
        //A:luobiao@wind-mobi.com 2015-8-14 begin
        LauncherAppState.getInstance().setLauncehrApplication(this);
        /**M: register unread broadcast.@{**/
        if (getResources().getBoolean(R.bool.config_unreadSupport)) {
            Log.d("LUOBIAO", "LauncherApplication");
            mUnreadLoader = new WOSUnreadLoader(getApplicationContext());
            mWOSContentObserver = new WOSContentObserver(getApplicationContext());
            // Register unread change broadcast.
            IntentFilter filter = new IntentFilter();
            filter.addAction(WOSUnreadLoader.ACTION_UNREAD_CHANGED);
            filter.addAction(WOSUnreadLoader.ACTION_CALL_CONTENT_OBSERVER);
            filter.addAction(WOSUnreadLoader.ACTION_MMS_CONTENT_OBSERVER);
            registerReceiver(mUnreadLoader, filter);
        }
        /**@}**/
        //A:luobiao@wind-mobi.com 2015-8-14 end
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        /**M:luobiao@wind-mobi.com 2015-8-14 added for unread feature, unregister unread receiver.@{**/
        if (getResources().getBoolean(R.bool.config_unreadSupport)) {
            unregisterReceiver(mUnreadLoader);
        }
        /**@}**/
        LauncherAppState.getInstance().onTerminate();
    }

    // A:luobiao@wind-mobi.com 2015-8-14 LauncherApplication start flag @{
    public void setTotalStartFlag() {
        mTotallyStart = true;
    }

    public void resetTotalStartFlag() {
        mTotallyStart = false;
    }

    public boolean isTotalStart() {
        return mTotallyStart;
    }
    // A:luobiao@wind-mobi.com 2015-8-14 }@

    /**M: Added for unread message feature.@{**/
    /**
     * A:luobiao@wind-mobi.com 2015-8-14 Get unread loader, added for unread feature.
     */
    public WOSUnreadLoader getUnreadLoader() {
        return mUnreadLoader;
    }

    public WOSContentObserver getWOSContentObserver(){
        return mWOSContentObserver;
    }
    /**@}**/
}