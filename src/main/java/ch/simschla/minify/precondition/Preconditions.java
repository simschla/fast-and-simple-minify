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
}
