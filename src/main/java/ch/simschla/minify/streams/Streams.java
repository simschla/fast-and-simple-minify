package ch.simschla.minify.streams;

import java.io.*;

import static ch.simschla.minify.precondition.Preconditions.checkNotNull;

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

	public static InputStream fileInputStream(File fileIn) throws RuntimeException {
		checkNotNull(fileIn);
		if(!fileIn.exists()) {
			throw new RuntimeException(new IOException("File " + fileIn + " does not exist."));
		}
		if(!fileIn.canRead()) {
			throw new RuntimeException(new IOException("File " + fileIn + " cannot be read."));
		}
		try {
			return new FileInputStream(fileIn);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static OutputStream fileOutputStream(File fileOut) {
		checkNotNull(fileOut);
		if(fileOut.exists() && !fileOut.canWrite()) {
			throw new RuntimeException(new IOException("Cannot write to file " + fileOut + "."));
		}
		try {
			return new FileOutputStream(fileOut);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
