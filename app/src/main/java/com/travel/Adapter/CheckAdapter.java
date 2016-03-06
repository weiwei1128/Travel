package com.travel.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.travel.R;

/**
 * Created by wei on 2016/1/7.
 */
public class CheckAdapter extends BaseAdapter{

    private Context mcontext;
    private LayoutInflater layoutInflater;

    public CheckAdapter(Context context){
        this.mcontext = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 5;
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
        cell mcell;
        View mview;
        mview = layoutInflater.inflate(R.layout.checkschedule_item,null);
        mcell = new cell((TextView)mview.findViewById(R.id.schedule_no_text),
                (TextView)mview.findViewById(R.id.schedule_where_text),
                (TextView)mview.findViewById(R.id.schedule_addr_text),
                (TextView)mview.findViewById(R.id.schedule_totaltime_text),
                (TextView)mview.findViewById(R.id.schedule_time_text));
        mcell.noTxt.setText((position+1)+"");

        return mview;
    }

    public class cell{
        TextView noTxt,whereTxt,addrTxt,timeTxt,drivetimeTxt;
        public cell(TextView m_noTxt,TextView m_whereTxt,TextView m_addrTxt,
                    TextView m_timeTxt,TextView m_drivetimeTxt){
            this.noTxt = m_noTxt;
            this.whereTxt = m_whereTxt;
            this.addrTxt = m_addrTxt;
            this.timeTxt = m_timeTxt;
            this.drivetimeTxt = m_drivetimeTxt;
        }
    }
}
