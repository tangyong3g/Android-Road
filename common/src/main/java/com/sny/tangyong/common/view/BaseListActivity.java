package com.sny.tangyong.common.view;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sny.tangyong.common.R;

import java.util.List;

/**
 * Created by ty_sany@163.com on 2016/6/4.
 */
public class BaseListActivity extends ListActivity {

    protected List<ItemComponentInfo> mItemsInfo;
    protected String[] mUnits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new ArrayAdapter<String>(this,R.layout.basic_item, mUnits));
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String name = findName(v);
        Intent intent = createIntentWithDisplayName(name);
        forward(intent);

        if(R.id.text1 == 1){
            System.out.print(false);
        }

    }

    private String findName(View view) {
        TextView tx = (TextView) view;
        return tx.getText().toString();
    }

    ;

    private Intent createIntentWithDisplayName(String displayName) {

        if (mItemsInfo == null) {
            return null;
        }

        // find target Class with displayName
        Class targetClass = null;

        for (ItemComponentInfo temp : mItemsInfo) {

            String name = temp.mDisplayName;

            if (name.equals(displayName)) {
                targetClass = temp.mItemClass;
                break;
            }
        }

        // create intent and return it
        Intent intent = new Intent();
        intent.setClass(this, targetClass);

        return intent;
    }


    private void forward(Intent intent) {
        startActivity(intent);
    }
}
