package com.example.hwanik.materialtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hwanik on 2015. 4. 17..
 */
public class FirstPage extends Fragment {
    String objectId;//§3
    String[] imgUrlArray = new String[20]; //§ (1) 생각해볼점.. 배열크기를 20정도로 하면 .ArrayIndexOutOfBoundsException 발
    String[] contentArray = new String[20]; //§ 2
    int count = 0;
    static String imgUrl;
    private ListView listView;
    private List<Item> list = new ArrayList<Item>();
    private MyAdapter myAdapter;
    PullRefreshLayout refreshlayout;
    int pos;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout=(LinearLayout)inflater.inflate(R.layout.test_list, container, false);

        listView = (ListView)linearLayout.findViewById(R.id.recent_lv);
        myAdapter=new MyAdapter(getActivity(),list);
        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { //＆2015.04.03 listview item의 onclick이벤트 [4]
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //§4
                pos=position;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        //intent.putExtra("objectId",list.get(pos).objectId);
                        objectId = list.get(pos).objectId;
                        /////////////////////////////////////////////////////////////////////////////////
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("hi");
                        query.whereEqualTo("objectId",objectId);
                        query.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> parseObjects, ParseException e) {
                                if (e == null) {
                                    for (int i = 0; i < parseObjects.size(); i++) {
                                        Log.d("object size", String.valueOf(parseObjects.size()));
                                        int j = 0;
                                        count = 0;
                                        while (parseObjects.get(i).get("step" + String.valueOf(j) + "image") != null | parseObjects.get(i).get("content" + String.valueOf(j)) != null) { //§ (3)
                                            ParseFile image = (ParseFile) parseObjects.get(i).get("step" + String.valueOf(j) + "image");
                                            imgUrlArray[count] = image.getUrl();
                                            contentArray[count] = parseObjects.get(i).getString("content" + String.valueOf(j));
                                            count++;
                                            j++;
                                        }
                                    }
                                    Intent intent=new Intent(getActivity(), Detail.class);
                                    intent.putExtra("imgUrlArray",imgUrlArray);
                                    intent.putExtra("contentArray",contentArray);
                                    intent.putExtra("count",count);
                                    startActivity(intent);
                                } else {
                                    Log.d("Error",e.getMessage());
                                }
                            }
                        });
                        /////////////////////////////////////////////////////////////////////////////////


                    }
                }, 250);

            }
        });


        refreshlayout= (PullRefreshLayout)linearLayout.findViewById(R.id.swipeRefreshLayout);
        refreshlayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        refreshlayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                load_from_parse();
                // refresh complete
                refreshlayout.setRefreshing(false);
            }
        });
        load_from_parse();


//        TextView page_num=(TextView)linearLayout.findViewById(R.id.page_num);
//        page_num.setText(String.valueOf(1));
        return refreshlayout;
    }
    public void load_from_parse(){
        myAdapter.removeAll();

        // Locate the class table named "Footer" in Parse.com
        ParseQuery<ParseObject> query = ParseQuery.getQuery("hi");

        query.orderByDescending("updatedAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e==null){
                    for(int i=0;i<parseObjects.size();i++ ){
                        Log.d("object size", String.valueOf(parseObjects.size()));
                        int j=0;
                        while(parseObjects.get(i).get("step" + String.valueOf(j) + "image")!=null){
                            ParseFile image=(ParseFile) parseObjects.get(i).get("step" + String.valueOf(j) + "image");

                            imgUrl=image.getUrl();
                            list.add(new Item(imgUrl,parseObjects.get(i).getString("content"+String.valueOf(j)),parseObjects.get(i).getObjectId()));
                            break;//§ j++;    if(j>0)를 삭제했음 어차피 하나만 보여주면 되니까.. 근데 이게아니라 표지이미지(Main_Image를 보여주도록 바꿪줘야함 어차피 ㅋㅋ)
                        }
                    }
                    myAdapter.notifyDataSetChanged();
                }else{
                    Log.d("error",e.getMessage());
                }
            }
        });
    }
    private class MyAdapter extends BaseAdapter {

        private final static int resId = R.layout.list_item;
        private Context context;
        List<Item> list;

        public MyAdapter(Context context, List<Item> list) {
            super();
            this.context = context;
            this.list = list;
        }
        public void removeAll(){   //■2015.04.02 (7)
//            while(myAdapter.getCount()!=0)
//                myAdapter.list.remove(0);
            myAdapter.list.clear();
            myAdapter.notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Item getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            Item item = getItem(position);

            if (v == null) {

                v = getActivity().getLayoutInflater().inflate(resId, null);
                //ImageView iv=(ImageView)v.findViewById(R.id.recent_image);
                //TextView tv = (TextView)v.findViewById(R.id.recent_txt);
            }
            ImageView iv=(ImageView)v.findViewById(R.id.recent_image);
            iv.setBackgroundResource(R.drawable.img_frame);

            TextView tv = (TextView)v.findViewById(R.id.recent_txt);
            tv.setTag(item);
            iv.setTag(item);

            tv.setText(item.txt);
            Picasso.with(context)
                    .load(item.img_url)
                    .into(iv);
            return v;
        }
    }
}