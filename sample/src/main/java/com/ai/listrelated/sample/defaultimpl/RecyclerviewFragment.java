package com.ai.listrelated.sample.defaultimpl;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ai.listrelated.adapter.recyclerview.CommonAdapter;
import com.ai.listrelated.adapter.recyclerview.base.ViewHolder;
import com.ai.listrelated.adapter.recyclerview.wrapper.LoadMoreWrapper;
import com.ai.listrelated.loadmore.LoadMoreRecyclerViewContainer;
import com.ai.listrelated.loadmore.iface.LoadMoreContainer;
import com.ai.listrelated.loadmore.iface.LoadMoreHandler;
import com.ai.listrelated.refresh.RefreshDefaultHandler;
import com.ai.listrelated.refresh.RefreshLayout;
import com.ai.listrelated.sample.LoadStateView;
import com.ai.listrelated.sample.R;
import com.ai.listrelated.sample.ReplyBean;
import com.ai.listrelated.ui.fragment.BaseLazyFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>Project:</b> ListRelated <br>
 * <b>Create Date:</b> 2017/1/8 <br>
 * <b>Author:</b> qy <br>
 * <b>Address:</b> qingyongai@gmail.com <br>
 * <b>Description:</b> ListViewFragment <br>
 */
public class RecyclerviewFragment extends BaseLazyFragment implements SwipeRefreshLayout.OnRefreshListener, LoadMoreHandler {

    private RefreshLayout mRefreshLayout;
    private LoadMoreRecyclerViewContainer mRecyclerViewContainer;
    private RecyclerView mRecyclerView;
    private LoadStateView mStateView;

    public static final int FIRST_PAGE_NUM = 1;
    private volatile int mCurrentPage = FIRST_PAGE_NUM;
    private volatile int mTotalPage = ReplyBean.TOTAL_PAGE;

    private List<ReplyBean> mDatas = new ArrayList<>();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private LoadMoreWrapper mLoadMoreWrapper;

    /**
     * 测试加载失败使用的
     */
    private int tryCount = 0;

    public static RecyclerviewFragment getInstance(Bundle data) {
        RecyclerviewFragment fragment = new RecyclerviewFragment();
        if (data != null) {
            fragment.setArguments(data);
        }
        return fragment;
    }

    @NonNull
    @Override
    public View onInflaterRootView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.load_more_recycler_view, container, false);
    }

    @Override
    public void onFindViews(View rootview) {
        mRefreshLayout = (RefreshLayout) rootview.findViewById(R.id.refresh_layout);
        mRecyclerViewContainer = (LoadMoreRecyclerViewContainer) rootview.findViewById(R.id.load_more_recycler_view_container);
        mRecyclerView = (RecyclerView) rootview.findViewById(R.id.load_more_recycler_view);
        mStateView = (LoadStateView) rootview.findViewById(R.id.load_more_state_view);

        // 这些配置直接拷贝过去用即可
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(
                R.color.red_first, R.color.red_second,
                R.color.red_third, R.color.fourth);
        mRefreshLayout.setRefreshHandler(new RefreshDefaultHandler(), mRecyclerView);

        // 这些配置直接拷贝去用即可
        mRecyclerViewContainer.setAutoLoadMore(true);
        mRecyclerViewContainer.setLoadMoreHandler(this);
        // mRecyclerViewContainer.showLoadAllFinishView(true);

        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0) {
                    return layoutManager.getSpanCount();
                } else {
                    return 1;
                }
            }
        });
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new GridItemDecoration(15, layoutManager.getSpanSizeLookup()));

//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
//        mRecyclerView.setLayoutManager(linearLayoutManager);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
//                DividerItemDecoration.VERTICAL_LIST));
    }

    @Override
    public void onBindContent() {
        CommonAdapter<ReplyBean> adapter = new CommonAdapter<ReplyBean>(getActivity(),
                R.layout.item_layout, mDatas) {
            @Override
            protected void convert(ViewHolder holder, ReplyBean replyBean, int position) {
                holder.setText(R.id.text, replyBean.getContent());
                holder.setImageResource(R.id.image, replyBean.getImageid());
            }
        };
        mLoadMoreWrapper = new LoadMoreWrapper(adapter);

        mRecyclerView.setAdapter(mLoadMoreWrapper);

        // 必须这设置这个之后设置加载更多的布局View
        mRecyclerViewContainer.setRecyclerViewAdapter(mLoadMoreWrapper);
        // 添加加载更多View利用的是adapter，所以必须先给mRecyclerViewContainer设置好adapter
        mRecyclerViewContainer.useDefaultFooter();

        mStateView.setType(LoadStateView.LOAD_EMPTY);
        reqFirstPageData();
    }

    @Override
    public void onRefresh() {
        reqFirstPageData();
    }

    @Override
    public void onLoadMore(LoadMoreContainer loadMoreContainer) {
        mCurrentPage++;
        reqData(true);
    }

    /**
     * 请求第一页数据
     */
    private void reqFirstPageData() {
        // 第一页的时候显示loading和下拉刷新
        mRefreshLayout.setRefreshing(true);
        mCurrentPage = FIRST_PAGE_NUM;
        reqData(false);
    }

    /**
     * 请求数据
     *
     * @param loadMore 是否是loadMore
     */
    private void reqData(final boolean loadMore) {
        if (mCurrentPage > mTotalPage) return;
        ReplyBean.getData(mHandler, mCurrentPage,
                new ReplyBean.OnDataCallBack<List<ReplyBean>>() {
                    @Override
                    public void onCallback(List<ReplyBean> data) {
                        if (loadMore) {
                            // 加载第三页的时候制造一个错误
                            if (mCurrentPage == 3) {
                                tryCount++;
                                if (tryCount < 3) {
                                    mCurrentPage--;
                                    mRecyclerViewContainer.loadMoreError();
                                    return;
                                } else {
                                    tryCount = 0;
                                }
                            }
                            mDatas.addAll(data);
                            // mLoadMoreWrapper.notifyDataSetChanged();
                            mLoadMoreWrapper.notifyItemRangeInserted(mLoadMoreWrapper.getRealItemCount(), data.size());
                        } else {
                            mDatas.clear();
                            mDatas.addAll(data);
                            mLoadMoreWrapper.notifyDataSetChanged();
                        }
                        // 设置状态
                        mStateView.setType(LoadStateView.LOAD_SUCCESS);
                        mRecyclerViewContainer.loadMoreFinish(mTotalPage > mCurrentPage);
                    }

                    @Override
                    public void onFinish() {
                        mRefreshLayout.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
