package stream.notesapp.filter;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import stream.notesapp.R;
import stream.notesapp.filter.model.Filter;

public class FilterMaterialSearchView extends FrameLayout implements RecyclerItemClickListener.OnItemClickListener {

    private RecyclerView mRvFilter;
    private FilterRvAdapter mFilterRvAdapter;
    private OnFilterViewListener mOnFilterViewListener;
    private TextView mTvNoFilter;
    private boolean isContainFilter;
    private String TAG = "Filter";

    public FilterMaterialSearchView(Context context) {
        super(context, null);
    }

    public FilterMaterialSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FilterMaterialSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initStyle(attrs, defStyleAttr);
    }

    private void initStyle(AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Msv, defStyleAttr, 0);

        if (a != null) {
            if (a.hasValue(R.styleable.Msv_msvIsContainEnabled)) {
                isContainFilter = a.getBoolean(R.styleable.Msv_msvIsContainEnabled, false);
            }

            a.recycle();
        }
    }

    public List<Filter> getFilter(){
        return mFilterRvAdapter.getFilters();
    }

    private void initView(){
        LayoutInflater.from(getContext()).inflate(R.layout.msv_filter, this, true);

        mRvFilter = (RecyclerView) findViewById(R.id.rv_filter);
        mTvNoFilter = (TextView)  findViewById(R.id.tv_no_filter);

        mFilterRvAdapter = new FilterRvAdapter(getContext());
        mRvFilter.setHasFixedSize(false);
        mRvFilter.setAdapter(mFilterRvAdapter);
        mRvFilter.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), this));

        // Griglia
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false);
        mRvFilter.setLayoutManager(gridLayoutManager);
        mFilterRvAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (mFilterRvAdapter.getItemCount() == 0){
                    mTvNoFilter.setVisibility(VISIBLE);
                } else {
                    mTvNoFilter.setVisibility(GONE);
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mFilterRvAdapter.getItemCount() == 0){
                    mTvNoFilter.setVisibility(VISIBLE);
                } else {
                    mTvNoFilter.setVisibility(GONE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mFilterRvAdapter.getItemCount() == 0){
                    mTvNoFilter.setVisibility(VISIBLE);
                } else {
                    mTvNoFilter.setVisibility(GONE);
                }
            }
        });
    }

    public void addFilter(Filter filter) {
        mFilterRvAdapter.addFilter(filter);
    }

    public void showSearch(boolean animate) {
        mFilterRvAdapter.clear();
    }

    public void closeSearch() {
        mFilterRvAdapter.clear();
    }

    public boolean isFilterVisible(){
        return mRvFilter.getVisibility() == VISIBLE;
    }

    public int getFilterHeight(){
       return mRvFilter.getHeight();
    }

    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        Log.d(TAG, "onEditorAction: " + actionId);

        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            return onEditorAction(textView, actionId, keyEvent);
        }
        return false;
    }

    @Override
    public void onItemClick(RecyclerView rv, View view, int position) {
        Log.d(TAG, "onItemClick: position:" + position);

        final Filter filter = mFilterRvAdapter.removeFilter(position);

        if (mFilterRvAdapter.getItemCount() == 0) {
        }

        if (mOnFilterViewListener != null) {
            mOnFilterViewListener.onFilterRemoved((Filter) filter);
        }

        if (mOnFilterViewListener != null) {
            mOnFilterViewListener.onFilterChanged(mFilterRvAdapter.getFilters());
        }
    }

    public void setOnFilterViewListener(OnFilterViewListener mOnFilterViewListener) {
        this.mOnFilterViewListener = mOnFilterViewListener;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof FilterMSVSavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        FilterMSVSavedState mSavedState = (FilterMSVSavedState) state;

        super.onRestoreInstanceState(mSavedState.getSuperState());

        mFilterRvAdapter.setFilters(mSavedState.filters);
    }

    @Override
    protected Parcelable onSaveInstanceState() {

        FilterMSVSavedState savedState = new FilterMSVSavedState(super.onSaveInstanceState());
        savedState.filters = mFilterRvAdapter.getFilters();

        return savedState;
    }

    class FilterMSVSavedState extends BaseSavedState {

        public List<Filter> filters;

        public FilterMSVSavedState(Parcelable superState) {
            super(superState);
        }
    }
}
