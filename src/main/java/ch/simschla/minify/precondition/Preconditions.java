package ch.simschla.minify.precondition;

public final class Preconditions {

	private Preconditions() {
	}

	public static <T> T checkNotNull(T obj) {
		if(obj == null) {
			throw new NullPointerException();
		}
		return obj;
	}

	public static <T extends Comparable> T checkLessThan(T actualValue, T upperBorder) {
		checkNotNull(actualValue);
		checkNotNull(upperBorder);
		if(actualValue.compareTo(upperBorder) < 0) {
			return actualValue;
		}
		throw new IllegalArgumentException(actualValue + " should be less than " + upperBorder + " but isn't.");
	}
}
