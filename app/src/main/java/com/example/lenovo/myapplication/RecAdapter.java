package com.example.lenovo.myapplication;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Lenovo on 2019/8/13.
 */

public class RecAdapter extends RecyclerView.Adapter<RecAdapter.ViewHolder> {
    Context context;
    List<CBean> list;
    MainActivity mainActivity;
    public RecAdapter(Context context, List<CBean> list, MainActivity mainActivityClass) {
        this.context = context;
        this.list = list;
        mainActivity=mainActivityClass;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_item_rec, parent, false);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int heightPixels = metrics.heightPixels;
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = heightPixels / 83 * 10;
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
            String busSchemesAttributes = list.get(position).getBusSchemesAttributes();
            if (busSchemesAttributes .equals("101") ) {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorDeongaree));
            } else {
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorLightblue));
            }
            holder.textXian.setText(list.get(position).getBusLu());
            holder.textChe.setText(list.get(position).getBusName());
            String busTime = list.get(position).getBusTime();
            long time = Long.parseLong(busTime);
            Log.e("tags","发车时间:"+time);
            holder.textTimed.setText(getFaTime(time*1000));
            Log.e("LZB","发车点数"+getFaTime(time*1000));
            holder.textPeo.setText(list.get(position).getBusPeople());
            holder.textAn.setText(list.get(position).getBusScheme());
            holder.textDan.setText(list.get(position).getBusOnePoint() + "分钟");

    }


    @Override
    public int getItemCount() {
        if (list != null) {
            Log.e("LZB" ,"适配中集合长度 = " + list.size());
            return list.size();
        }
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textXian, textChe, textTimed, textPeo, textDan;
        MyTextView textAn;
        public ViewHolder(View itemView) {
            super(itemView);
            textXian = itemView.findViewById(R.id.text_xian);
            textChe = itemView.findViewById(R.id.text_che);
            textTimed = itemView.findViewById(R.id.text_timed);
            textPeo = itemView.findViewById(R.id.text_peo);
            textAn = itemView.findViewById(R.id.text_an);
            textDan = itemView.findViewById(R.id.text_dan);
        }
    }

    //时间戳转化为时分
    public String getFaTime(long time) {
        Date data = new Date(time);
        SimpleDateFormat time_type = new SimpleDateFormat("hh:mm");
        String format = time_type.format(data);
        return format;
    }
}

