package stream.rocketnotes.filter;

import java.util.List;

import stream.rocketnotes.filter.model.Filter;

public interface OnFilterViewListener {
    void onFilterAdded(Filter filter);

    void onFilterRemoved(Filter filter);

    void onFilterChanged(List<Filter> list);
}
