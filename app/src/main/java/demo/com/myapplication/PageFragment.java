package demo.com.myapplication;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import demo.com.myapplication.adapter.OnLoadMoreListener;
import demo.com.myapplication.model.Phone;

/**
 * Created by leobui on 10/24/2017.
 */

public class PageFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    private RecyclerView mRecyclerView;
    private List<Phone> mPhones = new ArrayList<>();
    private PhoneAdapter mPhoneAdapter;

    public static PageFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    // Inflate the fragment layout we defined above for this fragment
    // Set the associated text for the title
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        for (int i = 0; i < 30; i++) {
            Phone phone = new Phone();
            phone.setName("Iphone " + i);
            phone.setDescription("Iphone" + i + "@apple");
            phone.setPrice(30000000);
            mPhones.add(phone);
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mPhones.get(position) == null){
                    return 2;
                }
                return 1;
            }
        });
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mPhoneAdapter = new PhoneAdapter();
        mRecyclerView.setAdapter(mPhoneAdapter);

        mPhoneAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                mPhones.add(null);
                mPhoneAdapter.notifyItemInserted(mPhones.size() - 1);

                //Load more data for reyclerview
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //Remove loading item
                        mPhones.remove(mPhones.size() - 1);
                        mPhoneAdapter.notifyItemRemoved(mPhones.size());

                        //Load data
                        int index = mPhones.size();
                        int end = index + 20;
                        for (int i = index; i < end; i++) {
                            Phone phone = new Phone();
                            phone.setName("Iphone " + i);
                            phone.setPrice(29000000);
                            phone.setDescription("alibaba" + i + "@gmail.com");
                            mPhones.add(phone);
                        }
                        mPhoneAdapter.notifyDataSetChanged();
                        mPhoneAdapter.setLoaded();
                    }
                }, 5000);
            }
        });
        return view;
    }
    static class PhoneViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName;
        public TextView txtPrice;
        public TextView txtOldPrice;
        public ImageView imgThumb;
        public PhoneViewHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.txt_name);
            txtPrice = (TextView) itemView.findViewById(R.id.txt_price);
            txtOldPrice = (TextView) itemView.findViewById(R.id.txt_old_price);
            imgThumb = (ImageView) itemView.findViewById(R.id.img_thumb);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
        }
    }

    class PhoneAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;

        private OnLoadMoreListener mOnLoadMoreListener;

        private boolean isLoading;
        private int visibleThreshold = 5;
        private int lastVisibleItem, totalItemCount;

        public PhoneAdapter() {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            });
        }

        public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
            this.mOnLoadMoreListener = mOnLoadMoreListener;
        }

        @Override
        public int getItemViewType(int position) {
            return mPhones.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_phone_item, parent, false);
                return new PhoneViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_loading_item, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof PhoneViewHolder) {
                Phone phone = mPhones.get(position);
                PhoneViewHolder phoneViewHolder = (PhoneViewHolder) holder;
                phoneViewHolder.txtName.setText(phone.getName());
                phoneViewHolder.txtPrice.setText(phone.getPrice()+" VND");

                phoneViewHolder.imgThumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getActivity(), DetailPhoneActivity.class);
                        startActivity(i);
                    }
                });
            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemCount() {
            return mPhones == null ? 0 : mPhones.size();
        }

        public void setLoaded() {
            isLoading = false;
        }
    }
}
