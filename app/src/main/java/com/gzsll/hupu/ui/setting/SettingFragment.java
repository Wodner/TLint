package com.gzsll.hupu.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gzsll.hupu.R;
import com.gzsll.hupu.otto.ChangeThemeEvent;
import com.gzsll.hupu.ui.BaseActivity;
import com.gzsll.hupu.util.CacheUtils;
import com.gzsll.hupu.util.FileUtils;
import com.gzsll.hupu.util.SettingPrefUtils;
import com.gzsll.hupu.widget.PreferenceFragment;
import com.squareup.otto.Bus;

import java.io.File;

import javax.inject.Inject;

/**
 * Created by sll on 2016/3/11.
 */
public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {


    private ListPreference pTextSize;// 字体大小
    private Preference pPicSavePath;// 图片保存路径
    private Preference pClearCache;
    private ListPreference pThreadSort;
    private ListPreference pSwipeBackEdgeMode;// 手势返回方向


    @Inject
    Bus mBus;
    @Inject
    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.setting);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


        pTextSize = (ListPreference) findPreference("pTextSize");
        pTextSize.setOnPreferenceChangeListener(this);
        setListSetting(Integer.parseInt(prefs.getString("pTextSize", "4")), R.array.txtSizeNum, pTextSize);


        pPicSavePath = findPreference("pPicSavePath");
        pPicSavePath.setOnPreferenceClickListener(this);
        pPicSavePath.setSummary("/sdcard" + File.separator + SettingPrefUtils.getPicSavePath(mContext) + File.separator);


        pClearCache = findPreference("pClearCache");
        pClearCache.setOnPreferenceClickListener(this);
        pClearCache.setSummary(CacheUtils.getCacheSize(mContext));

        pThreadSort = (ListPreference) findPreference("pThreadSort");
        pThreadSort.setOnPreferenceChangeListener(this);
        setListSetting(Integer.parseInt(prefs.getString("pThreadSort", "0")), R.array.sortType, pThreadSort);

        pSwipeBackEdgeMode = (ListPreference) findPreference("pSwipeBackEdgeMode");
        pSwipeBackEdgeMode.setOnPreferenceChangeListener(this);
        setListSetting(Integer.parseInt(prefs.getString("pSwipeBackEdgeMode", "0")), R.array.swipeBackEdgeMode, pSwipeBackEdgeMode);

        findPreference("pNightMode").setOnPreferenceChangeListener(this);

//        pOfflineCount = (ListPreference) findPreference("pOfflineCount");
//        pOfflineCount.setOnPreferenceChangeListener(this);
//        setListSetting(Integer.parseInt(prefs.getString("pOfflineCount", "0")), R.array.offlineCount, pOfflineCount);


    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("pTextSize".equals(preference.getKey())) {
            setListSetting(Integer.parseInt(newValue.toString()), R.array.txtSizeNum, pTextSize);
        } else if ("pThreadSort".equals(preference.getKey())) {
            setListSetting(Integer.parseInt(newValue.toString()), R.array.sortType, pThreadSort);
        } else if ("pSwipeBackEdgeMode".equals(preference.getKey())) {
            setListSetting(Integer.parseInt(newValue.toString()), R.array.swipeBackEdgeMode, pSwipeBackEdgeMode);
            ((BaseActivity) getActivity()).reload();
        } else if ("pOfflineCount".equals(preference.getKey())) {
            //  setListSetting(Integer.parseInt(newValue.toString()), R.array.offlineCount, pOfflineCount);
        } else if ("pNightMode".equals(preference.getKey())) {
            mBus.post(new ChangeThemeEvent());
            if (getActivity() instanceof BaseActivity)
                ((BaseActivity) getActivity()).reload();
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if ("pPicSavePath".equals(preference.getKey())) {
            modifyImageSavePath();
        } else if ("pClearCache".equals(preference.getKey())) {
            cleanCache();
        }


        return true;
    }


    private void cleanCache() {
//        new MaterialDialog.Builder(getActivity()).title("提示").content("正在清空缓存...").progress(true,0).show();
        CacheUtils.cleanApplicationCache(mContext);
        Toast.makeText(getActivity(), "缓存清理成功", Toast.LENGTH_SHORT);
        pClearCache.setSummary(CacheUtils.getCacheSize(mContext));
    }

    private void setTextSize(int value) {
        String[] valueTitleArr = getResources().getStringArray(R.array.txtSizeNum);
        pTextSize.setSummary(valueTitleArr[value]);
    }


    protected void setListSetting(int value, int hintId, ListPreference listPreference) {
        String[] valueTitleArr = getResources().getStringArray(hintId);
        listPreference.setSummary(valueTitleArr[value]);
    }




    private void modifyImageSavePath() {
        new MaterialDialog.Builder(getActivity()).title("修改图片保存路径").input(null, SettingPrefUtils.getPicSavePath(mContext), new MaterialDialog.InputCallback() {
            @Override
            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                if (TextUtils.isEmpty(charSequence)) {
                    showToast("路径不能为空");
                    return;
                }
                String path = FileUtils.getSdcardPath() + File.separator + charSequence + File.separator;
                File file = new File(path);
                if (file.exists() || file.mkdirs()) {
                    SettingPrefUtils.setPicSavePath(mContext, charSequence.toString());
                    pPicSavePath.setSummary("/sdcard" + File.separator + charSequence + File.separator);
                    showToast("更新成功");
                } else {
                    showToast("更新失败");
                }

            }
        }).negativeText("取消").show();
    }


    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}
