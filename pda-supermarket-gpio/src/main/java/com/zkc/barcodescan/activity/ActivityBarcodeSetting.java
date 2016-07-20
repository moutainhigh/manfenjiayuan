package com.zkc.barcodescan.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.manfenjiayuan.pda_supermarket.R;
import com.zkc.Service.CaptureService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityBarcodeSetting extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_barcodesetting);
        setListAdapter(new SimpleAdapter(this, getData("simple-list-item-2"), android.R.layout.simple_list_item_2, new String[]{"title", "description"}, new int[]{android.R.id.text1, android.R.id.text2}));
    }

    /**
     * ��List���ѡ��ʱ����
     */
    protected void onListItemClick(ListView listView, View v, int position, long id) {
        switch (position) {
            case 0:
                if (CaptureService.serialPort != null) {
                    CaptureService.serialPort.Write(CaptureService.defaultSetting1D);
                    Toast toast = Toast.makeText(this, getResources().getString(R.string.action_setsuccess), Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
            case 1:
                if (CaptureService.serialPort != null) {
                    CaptureService.serialPort.Write(CaptureService.dataTypeFor1D);
                    Toast toast = Toast.makeText(this, getResources().getString(R.string.action_setsuccess), Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * ����SimpleAdapter�ĵڶ�����������ΪList<Map<?,?>>
     *
     * @param title
     * @return
     */
    private List<Map<String, String>> getData(String title) {
        List<Map<String, String>> listData = new ArrayList<>();

        Map<String, String> map = new HashMap<>();
        map.put("title", getResources().getString(R.string.action_reset));
        map.put("description", getResources().getString(R.string.action_reset_desc));
        listData.add(map);

        map = new HashMap<>();
        map.put("title", getResources().getString(R.string.action_datatype));
        map.put("description", getResources().getString(R.string.action_datatype_desc));
        listData.add(map);

        return listData;
    }
}
