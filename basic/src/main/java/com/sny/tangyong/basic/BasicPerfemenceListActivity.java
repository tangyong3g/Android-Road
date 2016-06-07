package com.sny.tangyong.basic;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * 
 */
public class BasicPerfemenceListActivity extends Activity {

    private ListView mListView;
    private ListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    private void init() {

        mListView = new ListView(getBaseContext());

        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height);

        List<PackageInfo> apps = BaseCommonApp.getInstalledApps();

        mListView.setLayoutParams(layoutParams);
        mListView.setAdapter(new MyAdapter3(apps));

        setContentView(mListView);
    }


    static class ViewHolder {

        TextView text;
        TextView timestamp;
        ImageView icon;
        ProgressBar progress;
        int position;

    }



    class MyAdapter3 extends BaseAdapter {

        List<PackageInfo> mApps;
        LayoutInflater mInflater;

        public MyAdapter3(List<PackageInfo> apps) {
            this.mApps = apps;
            mInflater = getLayoutInflater();
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
        public int getCount() {

            if (mApps != null) return mApps.size();

            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;

            if (view == null) {
                view = mInflater.inflate(R.layout.list_items, parent, false);
            }

            //解决每一项都要检索View里面的元素带来的开支,把这些引用存储在ViewHolder里面，然后放到convertView里面
            if(view.getTag() == null) {
                {
                    ViewHolder holder = new ViewHolder();
                    holder.icon = (ImageView) view.findViewById(R.id.img_app);
                    holder.text = (TextView) view.findViewById(R.id.tx_name);
                    holder.timestamp = (TextView) view.findViewById(R.id.tx_time);

                    view.setTag(holder);
                }
            }

            PackageInfo info = mApps.get(position);
            ViewHolder holder = (ViewHolder)view.getTag();
            holder.text.setText(info.packageName);

            try{
                Drawable drawable  = getPackageManager().getApplicationIcon(info.packageName);
                holder.icon.setImageDrawable(drawable);
            }catch (PackageManager.NameNotFoundException ex){
                ex.printStackTrace();
            }
            return view;
        }
    }

    /**
     * --TODO 未完待续
     *
     */
    class MyAdapter2 extends SimpleAdapter {

        public MyAdapter2(Context context, List<? extends Map<String, ?>> data, @LayoutRes int resource, String[] from,
                @IdRes int[] to) {
            super(context, data, resource, from, to);
        }
    }

    /**
     * // --TODO 未完待续
     *
     */
    class MyAdapter implements ListAdapter {

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        public void registerDataSetObserver(DataSetObserver observer) {

        }

        /**
         * Unregister an observer that has previously been registered with this adapter via
         * {@link #registerDataSetObserver}.
         *
         * @param observer the object to unregister.
         */
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        public int getCount() {
            return 0;
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's data set.
         * @return The data at the specified position.
         */
        public Object getItem(int position) {
            return null;
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we
         *        want.
         * @return The id of the item at the specified position.
         */
        public long getItemId(int position) {
            return 0;
        }

        /**
         * Indicates whether the item ids are stable across changes to the underlying data.
         *
         * @return True if the same id always refers to the same object.
         */
        public boolean hasStableIds() {
            return false;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can
         * either create a View manually or inflate it from an XML layout file. When the View is
         * inflated, the parent View (GridView, ListView...) will apply default layout parameters
         * unless you use
         * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)} to
         * specify a root view and to prevent attachment to the root.
         *
         * @param position The position of the item within the adapter's data set of the item whose
         *        view we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this
         *        view is non-null and of an appropriate type before using. If it is not possible to
         *        convert this view to display the correct data, this method can create a new view.
         *        Heterogeneous lists can specify their number of view types, so that this View is
         *        always of the right type (see {@link #getViewTypeCount()} and
         *        {@link #getItemViewType(int)}).
         * @param parent The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        public View getView(int position, View convertView, ViewGroup parent) {

            return null;
        }


        /**
         * Get the type of View that will be created by {@link #getView} for the specified item.
         *
         * @param position The position of the item within the adapter's data set whose view type we
         *        want.
         * @return An integer representing the type of View. Two views should share the same type if
         *         one can be converted to the other in {@link #getView}. Note: Integers must be in
         *         the range 0 to {@link #getViewTypeCount} - 1. {@link #IGNORE_ITEM_VIEW_TYPE} can
         *         also be returned.
         * @see #IGNORE_ITEM_VIEW_TYPE
         */
        public int getItemViewType(int position) {
            return 0;
        }

        /**
         * <p>
         * Returns the number of types of Views that will be created by {@link #getView}. Each type
         * represents a set of views that can be converted in {@link #getView}. If the adapter
         * always returns the same type of View for all items, this method should return 1.
         * </p>
         * <p>
         * This method will only be called when when the adapter is set on the the
         * {@link AdapterView}.
         * </p>
         *
         * @return The number of types of Views that will be created by this adapter
         */
        public int getViewTypeCount() {
            return 0;
        }


        /**
         * @return true if this adapter doesn't contain any data. This is used to determine whether
         *         the empty view should be displayed. A typical implementation will return
         *         getCount() == 0 but since getCount() includes the headers and footers,
         *         specialized adapters might want a different behavior.
         */
        public boolean isEmpty() {
            return false;
        }


    }


}
