package org.mariella.persistence.runtime;


import java.io.Serializable;
import java.util.*;

@SuppressWarnings("unchecked")
public abstract class ObservableList<T> implements List<T>, Serializable {
	private static final long serialVersionUID = 1L;
	
    private final String propertyName;
    private final List<T> list = new ArrayList<>();
    private int modCount = 0;

    public ObservableList(String propertyName) {
        super();
        this.propertyName = propertyName;
    }

    public int getModCount() {
        return modCount;
    }

    @Override
    public boolean add(T e) {
        if (!contains(e)) {
            list.add(e);
            modCount++;
            fireIndexedPropertyChange(propertyName, size() - 1, null, e);
            return true;
        }
        //noinspection Contract
        return false;
    }

    protected abstract void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue);

    @Override
    public void add(int index, T element) {
        list.add(index, element);
        fireIndexedPropertyChange(propertyName, index, null, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean modified = false;
        for (T e : c) {
            add(e);
            modified = true;
        }
        return modified;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    public void moveAll(int startIndex, int length, int toIndex) {
        if (startIndex + length > list.size())
            throw new IllegalArgumentException();
        if (toIndex >= startIndex && toIndex <= startIndex + length - 1)
            throw new IllegalArgumentException();

        List<T> sublist = list.subList(startIndex, startIndex + length);
        List<T> clone = new ArrayList<>(sublist);
        sublist.clear();

        if (toIndex > startIndex)
            toIndex = toIndex - length + 1;

        list.addAll(toIndex, clone);
        modCount++;
        fireIndexedPropertyChange(propertyName, -1, true, false);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    public void primitiveClear() {
        list.clear();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return listIterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new ObservableListIterator<>(this, index);
    }

    @Override
    public boolean remove(Object o) {
        int idx = indexOf(o);
        if (idx > -1) {
            list.remove(o);
            modCount++;
            fireIndexedPropertyChange(propertyName, idx, o, null);
            return true;
        }
        return false;
    }

    @Override
    public T remove(int index) {
        T object = list.remove(index);
        fireIndexedPropertyChange(propertyName, index, object, null);
        return object;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object e : c) {
            if (remove(e)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T set(int index, T element) {
        T oldValue = list.set(index, element);
        modCount++;
        fireIndexedPropertyChange(propertyName, index, oldValue, element);
        return oldValue;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    // I have no idea why that works but the compiler is happy
    @Override
    public <E> E[] toArray(E[] a) {
        return (E[]) list.toArray(new Object[0]);
    }

    @Override
    public String toString() {
        return list.toString();
    }

    public String getPropertyName() {
        return propertyName;
    }

}
