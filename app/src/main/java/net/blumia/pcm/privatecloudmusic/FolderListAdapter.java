package net.blumia.pcm.privatecloudmusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by wzc78 on 2017/10/31.
 */

public class FolderListAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;

    public FolderListAdapter(Context context, ArrayList<PCMServerInfo> infolist) {
        super();

        mLayoutInflater = LayoutInflater.from(context);
        //mInfoArrayList = infolist;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
