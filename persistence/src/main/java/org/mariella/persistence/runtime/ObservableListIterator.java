package org.mariella.persistence.runtime;

import java.util.ConcurrentModificationException;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ObservableListIterator<E> implements ListIterator<E> {
    private final ObservableList<E> list;
    private int expectedModCount;
    private int cursor = 0;
    private int lastRet = -1;

    public ObservableListIterator(ObservableList<E> list) {
        super();
        this.list = list;
        this.expectedModCount = list.getModCount();
    }

    public ObservableListIterator(ObservableList<E> list, int index) {
        super();
        this.list = list;
        this.expectedModCount = list.getModCount();
        cursor = index;
    }

    @Override
    public boolean hasNext() {
        return cursor != list.size();
    }

    @Override
    public E next() {
        checkForComodification();
        try {
            E next = list.get(cursor);
            lastRet = cursor++;
            return next;
        } catch (IndexOutOfBoundsException e) {
            checkForComodification();
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        if (lastRet == -1) {
            throw new IllegalStateException();
        }
        checkForComodification();

        try {
            list.remove(lastRet);
            if (lastRet < cursor) {
                cursor--;
            }
            lastRet = -1;
            expectedModCount = list.getModCount();
        } catch (IndexOutOfBoundsException e) {
            throw new ConcurrentModificationException();
        }
    }

    public E previous() {
        checkForComodification();
        try {
            int i = cursor - 1;
            E previous = list.get(i);
            lastRet = cursor = i;
            return previous;
        } catch (IndexOutOfBoundsException e) {
            checkForComodification();
            throw new NoSuchElementException();
        }
    }

    @Override
    public int nextIndex() {
        return cursor;
    }

    @Override
    public int previousIndex() {
        return cursor - 1;
    }

    @Override
    public void set(E e) {
        if (lastRet == -1) {
            throw new IllegalStateException();
        }
        checkForComodification();
        try {
            list.set(lastRet, e);
            expectedModCount = list.getModCount();
        } catch (IndexOutOfBoundsException ex) {
            throw new ConcurrentModificationException();
        }
    }

    @Override
    public void add(E e) {
        checkForComodification();
        try {
            list.add(cursor++, e);
            lastRet = -1;
            expectedModCount = list.getModCount();
        } catch (IndexOutOfBoundsException ex) {
            throw new ConcurrentModificationException();
        }
    }

    @Override
    public boolean hasPrevious() {
        return cursor != 0;
    }

    protected void checkForComodification() {
        if (list.getModCount() != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

}
