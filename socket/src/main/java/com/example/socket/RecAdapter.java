package com.example.socket;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Lenovo on 2019/8/29.
 */

public class RecAdapter extends RecyclerView.Adapter<RecAdapter.ViewHolder>{
    Context context;
    List<UserBean> list;
    ItemListener itemClickListener;
    public void setOnItemClickListener(ItemListener listener){
        itemClickListener=listener;
    }
    interface ItemListener{
        void onItemclick(int i, String ip);
    }
    public RecAdapter(Context context, List<UserBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.layout_rec_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.text_name.setText(list.get(position).getName());
        holder.text_sch.setText(list.get(position).getBusScheme());
        holder.cb.setChecked(list.get(position).ischecked());
//        if(itemClickListener!=null){
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    itemClickListener.onItemclick(position,list.get(position).getIp());
//                    if(list.get(position).ischecked()){
//                        list.get(position).setIschecked(false);
//                    }else{
//                        list.get(position).setIschecked(true);
//                    }
//                }
//            });
//        }
        holder.cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onItemclick(position,list.get(position).getIp());
            }
        });
    }

    @Override
    public int getItemCount() {
        if(list!=null){
            return list.size();
        }
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView text_name,text_sch;
        CheckBox cb;
        public ViewHolder(View itemView) {
            super(itemView);
            cb=itemView.findViewById(R.id.cb);
            text_name= itemView.findViewById(R.id.text_name);
            text_sch= itemView.findViewById(R.id.text_sch);
        }
    }


}
