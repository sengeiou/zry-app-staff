package com.zhongmei.bty.customer.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhongmei.yunfu.R;
import com.zhongmei.bty.basemodule.customer.bean.CustomerBaseBean;


public class CustomerPopWindowAdapter extends BaseAdapter {

    private List<CustomerBaseBean> mList;
    private Context mContext;

    public CustomerPopWindowAdapter(Context context, List<CustomerBaseBean> list) {
        mContext = context;
        if (null == list) {
            mList = new ArrayList<CustomerBaseBean>();
        } else {
            mList = list;
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {

        return arg0;
    }


    @Override
    public View getView(int pos, View convertView, ViewGroup arg2) {

        Holder holder = null;

        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.customer_pop_list_item, null);
            holder = new Holder();
            holder.tv = (TextView) convertView.findViewById(R.id.groupname);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.tv.setText(getName(pos));

        return convertView;
    }

    private class Holder {
        TextView tv;
    }

    public String getId(int pos) {
        return mList.get(pos).getId();
    }

    public String getName(int pos) {
        return mList.get(pos).getName();
    }

}
