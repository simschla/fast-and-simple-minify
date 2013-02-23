package ch.simschla.minify.streams;

import java.io.Closeable;
import java.io.IOException;

public final class Streams {

	private Streams() {
		// no instance desired
	}


	public static void close(Closeable closeable) {
		if (closeable == null) {
			return; //nothing to do
		}
		try {
			closeable.close();
		} catch (IOException e) {
			// ignore
		}
	}
}
