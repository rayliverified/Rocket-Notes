package stream.rocketnotes.filter;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import stream.rocketnotes.Constants;
import stream.rocketnotes.R;
import stream.rocketnotes.UpdateMainEvent;
import stream.rocketnotes.filter.model.Filter;

public class FilterMaterialSearchView extends FrameLayout implements RecyclerItemClickListener.OnItemClickListener {

    private RecyclerView mRvFilter;
    private FilterRvAdapter mFilterRvAdapter;
    private OnFilterViewListener mOnFilterViewListener;
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

    public List<Filter> getFilter() {
        return mFilterRvAdapter.getFilters();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.msv_filter, this, true);

        mRvFilter = findViewById(R.id.rv_filter);

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
                if (mFilterRvAdapter.getItemCount() == 0) {
                    closeFilter();
                } else {
                    showFilter();
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mFilterRvAdapter.getItemCount() == 0) {
                    closeFilter();
                } else {
                    showFilter();
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mFilterRvAdapter.getItemCount() == 0) {
                    closeFilter();
                } else {
                    showFilter();
                }
            }
        });
    }

    public void addFilter(Filter filter) {
        mFilterRvAdapter.addFilter(filter);
    }

    public void showFilter() {
        mRvFilter.setVisibility(VISIBLE);
    }

    public void closeFilter() {
        mRvFilter.setVisibility(GONE);
        NotificationSender();
    }

    public void NotificationSender() {
        EventBus.getDefault().post(new UpdateMainEvent(Constants.FILTER));
        Log.d("Notification", Constants.FILTER);
    }


    public boolean isFilterVisible() {
        return mRvFilter.getVisibility() == VISIBLE;
    }

    public int getFilterHeight() {
        return mRvFilter.getHeight();
    }

    @Override
    public void onItemClick(RecyclerView rv, View view, int position) {
        Log.d(TAG, "onItemClick: position:" + position);

        final Filter filter = mFilterRvAdapter.removeFilter(position);

        if (mFilterRvAdapter.getItemCount() == 0) {
            Log.d("Filter", "Closed");
            closeFilter();
        }

        if (mOnFilterViewListener != null) {
            mOnFilterViewListener.onFilterRemoved(filter);
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
