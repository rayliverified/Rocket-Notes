package stream.rocketnotes.filter;

import stream.rocketnotes.filter.model.Filter;
import java.util.List;

public interface OnFilterViewListener {
    void onFilterAdded(Filter filter);

    void onFilterRemoved(Filter filter);

    void onFilterChanged(List<Filter> list);
}
