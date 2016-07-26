package edu.tcnj.phylotrees.mixedweight.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A wrapper class for a list of sets, representing the possible states a character
 * can exist in for a sequence of characters of a species.
 * <p>
 * <p>Since the underlying data structure is a {@link CopyOnWriteArrayList}, care should
 * be taken to correctly use this class:</p>
 * <ul>
 * <li>Do not modify the elements of this object. That means that the {@code Set<S>}s
 * wrapped by this list should not be added or removed from the list. However, the sets
 * themselves can safely be modified (by adding and removing {@code S} objects).</li>
 * <li>When constructing the {@code CharacterList}, pre-create a list of the sets
 * and use the constructor {@link #CharacterList(List)}, as this avoids creating
 * copies for each added element. (This can also be done via {@link Node#sets()})</li>
 * </ul>
 *
 */
public class CharacterList extends CopyOnWriteArrayList<Set<DNABase>> {

    public CharacterList() {
    }

    public CharacterList(List<Set<DNABase>> sets) {
        super(sets);
    }
}
