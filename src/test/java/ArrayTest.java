import tfc.collisionreversion.utils.CustomArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ArrayTest {
	public static void main(String[] args) {
		CustomArrayList<Object> list = new CustomArrayList<>();
		
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("a");
		list.add("b");
		
		System.out.println(list.size());
		list.compact();
		System.out.println(list.size());
		
		list.remove("a");
		System.out.println(list.size());
		list.remove("a");
		System.out.println(list.size());
		list.remove("a");
		System.out.println(list.size());
		System.out.println(list.get(list.size() - 1));
		System.out.println(list.get(list.size() - 2));
		
		list.addAll(5, Arrays.asList("a", "b", "c", "d"));
		System.out.println(list.size());
		System.out.println(list.get(5));
		System.out.println(list.get(6));
		System.out.println(list.get(7));
		System.out.println(list.get(8));
		System.out.println(list.size());
		
		for (Object o : list.subList(5, 9)) {
			System.out.println(o);
		}
		
//		list.sort((s1, s2)->{return s1.toString().compareTo(s2.toString());});
		list.compact();
		System.out.println(new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f")).toString());
		list.sort(Comparator.comparing(Object::toString));
		System.out.println(list.toString());
	}
}
