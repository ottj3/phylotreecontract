package edu.tcnj.phylotrees.data;

import com.koloboke.compile.ConcurrentModificationUnchecked;
import com.koloboke.compile.KolobokeMap;

import java.util.Set;

@KolobokeMap
@ConcurrentModificationUnchecked
public abstract class WordCountMap<S> {
    public static WordCountMap withExpectedSize(int expectedSize) {
        return new KolobokeWordCountMap(expectedSize);
    }

    final int defaultValue() {
        return 0;
    }

    public abstract int addValue(S key, int add);

    public abstract int getInt(S key);

    public abstract Set<S> keySet();
}