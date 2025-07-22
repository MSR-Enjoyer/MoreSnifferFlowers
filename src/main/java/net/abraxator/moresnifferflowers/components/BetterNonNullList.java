package net.abraxator.moresnifferflowers.components;

import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class BetterNonNullList<E> extends NonNullList<E> {
    private final List<E> list;
    @Nullable
    private final E defaultValue;


    protected BetterNonNullList(List<E> list, @Nullable E defaultValue) {
        super(list, defaultValue);
        this.list = list;
        this.defaultValue = defaultValue;

    }

    public static <E> BetterNonNullList<E> create() {
        return new BetterNonNullList<>(Lists.newArrayList(), (E)null);
    }

    public static <E> BetterNonNullList<E> createWithCapacity(int initialCapacity) {
        return new BetterNonNullList<>(Lists.newArrayListWithCapacity(initialCapacity), (E)null);
    }

    /**
     * Creates a new BetterNonNullList with <i>fixed</i> size and default speed. The list will be filled with the default speed.
     */
    public static <E> BetterNonNullList<E> withSize(int size, E defaultValue) {
        Validate.notNull(defaultValue);
        Object[] aobject = new Object[size];
        Arrays.fill(aobject, defaultValue);
        return new BetterNonNullList<>(Arrays.asList((E[])aobject), defaultValue);
    }

    @SafeVarargs
    public static <E> BetterNonNullList<E> of(E defaultValue, E... elements) {
        return new BetterNonNullList<>(Arrays.asList(elements), defaultValue);
    }

    public int getValidSize() {
        int size = 0;
        for (E entry : list) {
            if (!isDefault(entry)) {
                size++;
            }
        }
        return size;
    }

    public int getFirstEmptySlot(){
        for (int i = 0; i < list.size(); i++) {
            E entry = list.get(i);
            if (isDefault(entry)) return i;
        }
        throw new RuntimeException("No empty Slot found");
    }

    public E getLastValid(){
        for (int i = list.size() -1 ; i >= 0; i--) {
            E entry = list.get(i);
            if (!isDefault(entry)) {
                return entry;

            }
        }

        throw new RuntimeException("List is Default");
    }

    public boolean isDefault(E o) {
        return o.toString().equals(defaultValue.toString()) || o.equals(defaultValue);
    }

    public boolean isFullyDefault() {
        for (E entry : list) {
            if (!isDefault(entry))
                return false;
        }
        return true;
    }

    public boolean isFull(){
        return getValidSize() >= list.size();
    }

    public Stream<E> validStream() {
        return list.stream().filter(e -> !isDefault(e));
    }


    public @NotNull E setDefault(int index) {
        E ret = this.get(index);
        list.set(index, defaultValue);
        return ret;
    }
}
