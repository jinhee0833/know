package com.helloants.mm.helloants1.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.activity.BsEachList;
import com.helloants.mm.helloants1.data.type.BSType;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;


public class debtAdapter extends BaseAdapter {
    private ArrayList<BSType> mList;

    public debtAdapter() {
        mList = new ArrayList<BSType>();
    }

    @Override
    public int getCount() {
        return mList.size();
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
        final int pos = position;
        final Context context = parent.getContext();
        if (convertView == null) {
            // 리스트가 길어지면서 현재 화면에 보이지 않는 아이템은 converView가 null인 상태로 들어 옴
            // view가 null일 경우 커스텀 레이아웃을 얻어 옴
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_firstview_list2, parent, false);
        }
        TextView AssetName = (TextView) convertView.findViewById(R.id.txv_name_fvlist);
        TextView AssetPrice = (TextView) convertView.findViewById(R.id.txv_price_fvlist);

        final String name = mList.get(pos).getName();
        try {
            AssetName.setText(name.substring(0, name.length() - 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        AssetPrice.setText(Currency.getInstance(Locale.KOREA).getSymbol() + " " + String.format("%,d", mList.get(pos).getValue()));
        if (mList.size() - pos == 1) {
            AssetName.setTypeface(null, Typeface.BOLD);
            AssetPrice.setTypeface(null, Typeface.BOLD);
            AssetName.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Large);
            AssetPrice.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Medium);
            AssetName.setTextColor(Color.parseColor("#FF0066"));
            AssetPrice.setTextColor(Color.parseColor("#FF0066"));
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mList.size() - pos == 1){}else{
                    Intent intent = new Intent(context, BsEachList.class);
                    intent.putExtra("name", name);
                    context.startActivity(intent);
                }
            }
        });

        return convertView;
    }


    public void setList(ArrayList<BSType> List) {
        this.mList = List;
    }

}