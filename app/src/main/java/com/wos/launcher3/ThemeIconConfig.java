package com.wos.launcher3;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.util.ArrayList;

public class ThemeIconConfig {
    Context context;

    public ThemeIconConfig(Context context) {
        this.context = context;
    }
    SharedPreferences mSharedPreferences;
    public ArrayList<Node> parseXml() {
        ArrayList<Node> nodeList = null;
        Node node = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser pullParser = factory.newPullParser();

            //A: zhangxutong@wind-mobi.com 2015 07 01 begin
            StringBuilder fileName = new StringBuilder("theme_icon_config_");
            int index = android.provider.Settings.System.getInt(context.getContentResolver(), "wos_launcher3_theme_index",LauncherAppState.DEFAULT_THEME_INDEX);
            if(index > 6 ||index < 1){
                index = LauncherAppState.DEFAULT_THEME_INDEX;
            }

            fileName.append(index);
            fileName.append(".xml");
            //A: zhangxutong@wind-mobi.com 2015 07 01 end
            mSharedPreferences = context.getSharedPreferences("defaultWallPaper",context.MODE_WORLD_READABLE);
            boolean defaultWallPaper = mSharedPreferences.getBoolean("defaultWallPaper", true);

            if(defaultWallPaper == true){
                Log.d("LUOBIAO", "defaultWallPaper == true");
                setWallPaper(context,index);
                Editor mEditor = mSharedPreferences.edit();
                mEditor.putBoolean("defaultWallPaper", false);
                mEditor.commit();
            }

            pullParser.setInput(context.getResources().getAssets().open(fileName.toString()), "UTF-8");

            int eventType = pullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = pullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        nodeList = new ArrayList<ThemeIconConfig.Node>();
                        break;
                    case XmlPullParser.START_TAG:
                        if (nodeName.equals("item")) {
                            node = new Node();
                            node.className = pullParser.getAttributeValue(0);
                            node.drawableName = pullParser.getAttributeValue(1);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (nodeName.equals("item")) {
                            nodeList.add(node);
                            node = null;
                        }
                        break;

                    default:
                        break;
                }
                eventType = pullParser.next();
            }

        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return nodeList;
    }

    public void changeWallpaper(){
        int index = android.provider.Settings.System.getInt(context.getContentResolver(), "wos_launcher3_theme_index",LauncherAppState.DEFAULT_THEME_INDEX);
        setWallPaper(context ,index);
    }
    public  void setWallPaper(Context context ,int index)
    {
        TypedArray ar = context.getResources().obtainTypedArray(R.array.wallpaper);
        int len = ar.length();
        int[] resIds = new int[len];
        for (int i = 0; i < len; i++)
            resIds[i] = ar.getResourceId(i, 0);
        ar.recycle();
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        try {
            wallpaperManager.setBitmap(BitmapFactory.decodeResource(context.getResources(), resIds[index-1]));
        } catch (IOException e) {
            Log.d("LUOBIAO", "error set wallpaper");
            e.printStackTrace();
        }

    }

    class Node {
        String className;
        String drawableName;
    }

}
