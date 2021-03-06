package com.wos.launcher3;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ThemeIconConfig {

    private Context context;
    private SharedPreferences mSharedPreferences;
    public ThemeIconConfig(Context context) {
        this.context = context;
        mSharedPreferences = context.getSharedPreferences("LauncherSettings",context.MODE_WORLD_READABLE);
    }

    public ArrayList<Node> parseXml() {
        ArrayList<Node> nodeList = null;
        Node node = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser pullParser = factory.newPullParser();
            int index = mSharedPreferences.getInt("themeIndex", LauncherAppState.DEFAULT_THEME_INDEX);
            StringBuilder fileName = new StringBuilder("theme_icon_config_");
            if(index > 6 ||index < 1){
                index = LauncherAppState.DEFAULT_THEME_INDEX;
            }

            fileName.append(index);
            fileName.append(".xml");
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
        int index = mSharedPreferences.getInt("themeIndex", LauncherAppState.DEFAULT_THEME_INDEX);
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

    public static String themePath = Environment.getExternalStorageDirectory()+"/theme";

    /*
    直接读取zip包中内容
     */
    public InputStream readZipFile(String filePatch) throws IOException {
        String themeName = mSharedPreferences.getString("themeName", "theme01.wos");
        String themeXmlpatch  = themePath+"/"+themeName;
        File xmlFile = new File(themeXmlpatch);
        ZipFile zipFile = new ZipFile(xmlFile);
        ZipEntry zipEntry = zipFile.getEntry(filePatch);
        InputStream read = zipFile.getInputStream(zipEntry);
        return read;
    }
    public ArrayList<Node> parseInternetXml() {
        ArrayList<Node> nodeList = null;
        Node node = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser pullParser = factory.newPullParser();

            InputStream slideInputStream = readZipFile("theme.xml");
            Log.d("LUOBIAO","slideInputStream:"+slideInputStream);
            //InputStream slideInputStream = new FileInputStream(themeXmlpatch);
            factory.setNamespaceAware(true);
            pullParser.setInput(slideInputStream, "UTF-8");

            int eventType = pullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = pullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        nodeList = new ArrayList<>();
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

        } catch (Exception e) {
            return null;
        }
        return nodeList;
    }

}
