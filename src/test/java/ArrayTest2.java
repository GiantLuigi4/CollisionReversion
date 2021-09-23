import tfc.collisionreversion.utils.CustomArrayList;

public class ArrayTest2 {
	public static void main(String[] args) {
		String[] array = new String[]{"h", "e", "o", null, null};
		System.arraycopy(array, 2, array, 4, 1);
		
		CustomArrayList<String> list1 = new CustomArrayList<>();
		CustomArrayList<String> list2 = new CustomArrayList<>();
		
		list1.add("hi");
		list2.add("how");
		list2.add("are");
		list2.add("you?");
		
		list1.addAll(list2);
		
		System.out.println(list1);
	}
}
