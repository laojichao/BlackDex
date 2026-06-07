package top.niunaijun.blackbox.utils;

import java.util.Arrays;
import java.util.Objects;

/**
 * 数组操作工具类。
 * <p>
 * 提供数组的裁剪、追加、查找、包含判断等常用操作。
 * 支持泛型数组、int基本类型数组和Class类型数组。
 * 主要用于虚拟化框架中的方法参数替换和类型匹配场景。
 */
public class ArrayUtils {

	/**
	 * 将数组裁剪到指定大小。
	 *
	 * @param array 源数组
	 * @param size  目标大小
	 * @param <T>   数组元素类型
	 * @return 裁剪后的新数组；如果源数组为null或size为0返回null；
	 *         如果大小相等则返回原数组
	 */
	public static<T> T[] trimToSize(T[] array, int size) {
		if (array == null || size == 0) {
			return null;
		} else if (array.length == size) {
			return array;
		} else {
			return Arrays.copyOf(array, size);
		}
	}

	/**
	 * 向Object数组末尾追加一个元素。
	 *
	 * @param array 源数组
	 * @param item  要追加的元素
	 * @return 包含新元素的新数组
	 */
	public static Object[] push(Object[] array, Object item)
	{
		Object[] longer = new Object[array.length + 1];
		System.arraycopy(array, 0, longer, 0, array.length);
		longer[array.length] = item;
		return longer;
	}

	/**
	 * 判断泛型数组中是否包含指定元素。
	 *
	 * @param array 数组
	 * @param value 要查找的值
	 * @param <T>   元素类型
	 * @return 包含返回true
	 */
	public static <T> boolean contains(T[] array, T value) {
		return indexOf(array, value) != -1;
	}

	/**
	 * 判断int数组中是否包含指定值。
	 *
	 * @param array 数组
	 * @param value 要查找的值
	 * @return 包含返回true，数组为null返回false
	 */
	public static boolean contains(int[] array, int value) {
		if (array == null) return false;
		for (int element : array) {
			if (element == value) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return first index of {@code value} in {@code array}, or {@code -1} if
	 * not found.
	 */
	public static <T> int indexOf(T[] array, T value) {
		if (array == null) return -1;
		for (int i = 0; i < array.length; i++) {
			if (Objects.equals(array[i], value)) return i;
		}
		return -1;
	}

	/**
	 * 查找指定类型在Class数组中首次出现的位置（使用引用相等比较）。
	 *
	 * @param array Class数组
	 * @param type  要查找的Class类型
	 * @return 首次出现的索引，未找到返回-1
	 */
	public static int protoIndexOf(Class<?>[] array, Class<?> type) {
		if (array == null) return -1;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == type) return i;
		}
		return -1;
	}

	/**
	 * 查找Object数组中第一个属于指定Class类型的元素索引。
	 *
	 * @param array Object数组
	 * @param type  目标Class类型
	 * @return 首次匹配的索引，未找到返回-1
	 */
	public static int indexOfFirst(Object[] array, Class<?> type) {
		if (!isEmpty(array)) {
			int N = -1;
			for (Object one : array) {
				N++;
				if (one != null && type == one.getClass()) {
					return N;
				}
			}
		}
		return -1;
	}

	/**
	 * 从指定位置开始查找Class类型在数组中首次出现的位置（使用引用相等比较）。
	 *
	 * @param array    Class数组
	 * @param type     要查找的Class类型
	 * @param sequence 起始搜索位置
	 * @return 匹配的索引，未找到返回-1
	 */
	public static int protoIndexOf(Class<?>[] array, Class<?> type, int sequence) {
		if (array == null) {
			return -1;
		}
		while (sequence < array.length) {
			if (type == array[sequence]) {
				return sequence;
			}
			sequence++;
		}
		return -1;
	}


	/**
	 * 从指定位置开始查找Object数组中属于指定Class类型实例的元素索引（使用instanceof判断）。
	 *
	 * @param array    Object数组
	 * @param type     目标Class类型
	 * @param sequence 起始搜索位置
	 * @return 匹配的索引，未找到返回-1
	 */
	public static int indexOfObject(Object[] array, Class<?> type, int sequence) {
		if (array == null) {
			return -1;
		}
		while (sequence < array.length) {
			if (type.isInstance(array[sequence])) {
				return sequence;
			}
			sequence++;
		}
		return -1;
	}


	/**
	 * 查找Object数组中第N个属于指定精确Class类型的元素索引。
	 *
	 * @param array    Object数组
	 * @param type     精确匹配的Class类型（使用==比较）
	 * @param sequence 第N次出现（从1开始计数）
	 * @return 匹配的索引，未找到返回-1
	 */
	public static int indexOf(Object[] array, Class<?> type, int sequence) {
		if (!isEmpty(array)) {
			int N = -1;
			for (Object one : array) {
				N++;
				if (one != null && one.getClass() == type) {
					if (--sequence <= 0) {
						return N;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * 查找Object数组中最后一个属于指定精确Class类型的元素索引。
	 *
	 * @param array Object数组
	 * @param type  精确匹配的Class类型
	 * @return 最后一个匹配的索引，未找到返回-1
	 */
	public static int indexOfLast(Object[] array, Class<?> type) {
		if (!isEmpty(array)) {
			for (int N = array.length; N > 0; N--) {
				Object one = array[N - 1];
				if (one != null && one.getClass() == type) {
					return N - 1;
				}
			}
		}
		return -1;
	}

	/**
	 * 判断数组是否为null或空。
	 *
	 * @param array 数组
	 * @param <T>   元素类型
	 * @return 数组为null或长度为0返回true
	 */
	public static <T> boolean isEmpty(T[] array) {
		return array == null || array.length == 0;
	}

	/**
	 * 获取Object数组中第一个属于指定Class类型的元素。
	 *
	 * @param args  Object数组
	 * @param clazz 目标Class类型
	 * @param <T>   返回值类型
	 * @return 第一个匹配的元素，未找到返回null
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getFirst(Object[] args, Class<?> clazz) {
		int index = indexOfFirst(args, clazz);
		if (index != -1) {
			return (T) args[index];
		}
		return null;
	}


	/**
	 * 验证数组的偏移量和计数是否有效。
	 *
	 * @param arrayLength 数组总长度
	 * @param offset      起始偏移量
	 * @param count       元素计数
	 * @throws ArrayIndexOutOfBoundsException 如果偏移量或计数越界
	 */
	public static void checkOffsetAndCount(int arrayLength, int offset, int count) throws ArrayIndexOutOfBoundsException {
		if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
			throw new ArrayIndexOutOfBoundsException(offset);
		}
	}
}
