package tfc.collisionreversion.utils;

import java.util.*;

// somehow this managed to be 2 ms faster than Vector with 98304 elements, so uh, I'm keeping it
public class CustomArrayList<T> implements List<T> {
	T[] elements;
	int length = 0;
	
	public CustomArrayList() {
		elements = (T[]) new Object[0];
	}
	
	@Override
	public int size() {
		return length;
	}
	
	@Override
	public boolean isEmpty() {
		return length == 0;
	}
	
	@Override
	public boolean contains(Object o) {
		for (int i = 0; i < length; i++) {
			T element = elements[i];
			if (element.equals(o)) return true;
		}
		return false;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			int indx = 0;
			
			@Override
			public boolean hasNext() {
				return indx < length;
			}
			
			@Override
			public T next() {
				return elements[indx++];
			}
		};
	}
	
	@Override
	public Object[] toArray() {
		return Arrays.copyOf(elements, length);
	}
	
	@Override
	public <T1> T1[] toArray(T1[] a) {
		if (a.length < length)
			return (T1[]) Arrays.copyOf(elements, length, a.getClass());
		
		System.arraycopy(elements, 0, a, 0, length);
		
		if (a.length > length)
			a[length] = null;
		
		return a;
	}
	
	@Override
	public boolean add(T t) {
		ensureCapacity();
		elements[length++] = t;
		return true;
	}
	
	public static int growthRate = 1;
	public static int minGrowth = 50;
	
	private void ensureCapacity() {
		int alen = elements.length;
		if (alen == 0) elements = (T[]) new Object[minGrowth];
		else if (alen <= (length + 1)) elements = Arrays.copyOf(elements, length + (length * growthRate));
	}
	
	@Override
	public boolean remove(Object o) {
		int indx = -1;
		for (int i = 0; i < length; i++) {
			T element = elements[i];
			if (element == o || element.equals(o)) {
				elements[i] = null;
				indx = i;
				break;
			}
		}
		if (indx == -1) return false;
		length--;
		compact(indx);
		return true;
	}
	
	public void compact(int indxRemove) {
		int arrLen = elements.length;
		if (Math.min(length, arrLen--) - indxRemove >= 0)
			System.arraycopy(elements, indxRemove + 1, elements, indxRemove, Math.min(length, elements.length - 1) - indxRemove);
//		if (elements.length == length) return;
		if (arrLen > length + 60) compact();
//		elements = Arrays.copyOf(elements, length);
	}
	
	public void compact() {
		if (elements.length == length) return;
		elements = Arrays.copyOf(elements, length);
	}
	
	public void push(int indx, int amt) {
		if (elements.length <= length + amt) elements = Arrays.copyOf(elements, length + amt);
		length = Math.max(length + amt, length);
//		for (int i = length - 1; i >= indx + amt; i--) {
//			elements[i] = elements[i - amt];
//			elements[i - amt] = null;
//		}
//		System.out.println(elements.length - (indx + amt) - (length - amt));
		System.arraycopy(elements, indx, elements, indx + amt, (length - (indx + amt)));
		for (int i = indx; i < indx + amt; i++) {
			elements[i] = null;
		}
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) if (!contains(o)) return false;
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		if (c.isEmpty()) return false;
		T[] array = c.toArray((T[]) new Object[0]);
//		for (int i = 0; i < array.length; i++) {
//			elements[i + length] = array[i];
//		}
		int sz = c.size();
		int index = length;
		push(length, sz);
		System.arraycopy(array, 0, elements, index, sz);
//		System.arraycopy(array, 0, elements, length, length + array.length);
//		for (T t : c) add(t);
		return true;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		if (c.isEmpty()) return false;
		int sz = c.size();
		push(index, sz);
//		for (T t : c) elements[index++] = t;
		T[] array = c.toArray((T[]) new Object[0]);
		System.arraycopy(array, 0, elements, index, sz);
		return true;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		if (c.isEmpty()) return false;
		for (Object t : c) remove(t);
		return true;
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		Object[] newList = new Object[c.size()];
		int indx = 0;
		for (Object o : c) {
			int indxOf = indexOf(o);
			if (indxOf != -1) {
				newList[indx++] = elements[indxOf];
			}
		}
		if (c.size() == length && indx == length) return false;
		elements = (T[]) Arrays.copyOf(newList, indx);
		return true;
	}
	
	@Override
	public void clear() {
		elements = (T[]) new Object[0];
		length = 0;
	}
	
	@Override
	public T get(int index) {
		return elements[index];
	}
	
	@Override
	public T set(int index, T element) {
		T obj = elements[index];
		elements[index] = element;
		return obj;
	}
	
	@Override
	public void add(int index, T element) {
		push(index, 1);
		elements[index] = element;
	}
	
	@Override
	public T remove(int index) {
		T obj = elements[index];
		length--;
		compact(index);
		return obj;
	}
	
	@Override
	public int indexOf(Object o) {
		for (int i = 0; i < length; i++) {
			T element = elements[i];
			if (element == o || element.equals(o)) return i;
		}
		return -1;
	}
	
	@Override
	public int lastIndexOf(Object o) {
		for (int i = length - 1; i >= 0; i--) {
			T element = elements[i];
			if (element == o || element.equals(o)) return i;
		}
		return -1;
	}
	
	/**
	 * Returns a list iterator over the elements in this list (in proper
	 * sequence).
	 *
	 * @return a list iterator over the elements in this list (in proper
	 * sequence)
	 */
	@Override
	public ListIterator<T> listIterator() {
		return null;
	}
	
	/**
	 * Returns a list iterator over the elements in this list (in proper
	 * sequence), starting at the specified position in the list.
	 * The specified index indicates the first element that would be
	 * returned by an initial call to {@link ListIterator#next next}.
	 * An initial call to {@link ListIterator#previous previous} would
	 * return the element with the specified index minus one.
	 *
	 * @param index index of the first element to be returned from the
	 *              list iterator (by a call to {@link ListIterator#next next})
	 * @return a list iterator over the elements in this list (in proper
	 * sequence), starting at the specified position in the list
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *                                   ({@code index < 0 || index > size()})
	 */
	@Override
	public ListIterator<T> listIterator(int index) {
		return null;
	}
	
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		T[] objects = Arrays.copyOfRange(elements, fromIndex, toIndex);
		return new CustomArrayList<>(objects);
	}
	
	public CustomArrayList(T[] elements) {
		this.elements = Arrays.copyOf(elements, elements.length);
		this.length = elements.length;
	}
	
	@Override
	public void sort(Comparator<? super T> c) {
		Arrays.sort(elements, 0, length, (Comparator<Object>) c);

//		int lastIndexSorted = length - 1;
//		int firstIndexSorted = 0;
//
//		while (lastIndexSorted != firstIndexSorted) {
//			int tempIndexSorted = lastIndexSorted;
//			int tempFirtIndexSorted = firstIndexSorted;
//
//			lastIndexSorted = 0;
//			firstIndexSorted = 0;
//
//			for (int i = tempFirtIndexSorted; i < Math.max(tempIndexSorted, length - 1); i++) {
//				try {
//					int result = c.compare(elements[i], elements[i + 1]);
//					if (result == 0) continue;
//
//					if (result > 0) {
//						Object first = elements[i];
//						elements[i] = elements[i + 1];
//						elements[i + 1] = first;
//
//						if (firstIndexSorted == 0) firstIndexSorted = i - 1;
//						lastIndexSorted = i;
//					}
//				} catch (Throwable err) {
//					System.out.println(i);
//					throw new RuntimeException(err);
//				}
//			}
//
//			if (firstIndexSorted < 0) firstIndexSorted = 0;
//		}
	}
	
	@Override
	public String toString() {
		compact();
		return Arrays.toString(elements);
	}
}