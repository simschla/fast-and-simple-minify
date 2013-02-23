package ch.simschla.minify.adapter;

import ch.simschla.minify.ant.MinifyAntTask;
import ch.simschla.minify.css.CssMin;
import ch.simschla.minify.js.JsMin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public enum Minifier {
	CSS("css") {
		@Override
		public void minify(InputStream inputStream, OutputStream outputStream, Charset charset, String customHeader) {
			final CssMin cssMin = CssMin.builder()
					.inputStream(inputStream)
					.outputStream(outputStream)
					.charset(charset)
					.customHeader(customHeader)
					.build();
			cssMin.minify();
		}
	},

	JS("js") {
		@Override
		public void minify(InputStream inputStream, OutputStream outputStream, Charset charset, String customHeader) {
			final JsMin jsMin = JsMin.builder()
					.inputStream(inputStream)
					.outputStream(outputStream)
					.charset(charset)
					.customHeader(customHeader)
					.build();
			jsMin.minify();
		}
	};

	private final FilenameFilter fileNameFilter;

	private Minifier(final String... acceptedFileTypes) {
		this.fileNameFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name == null) {
					return false;
				}
				for (String acceptedFileType : acceptedFileTypes) {
					if(name.endsWith(acceptedFileType)) {
						return true;
					}
				}
				return false;
			}
		};
	}

	public boolean accepts(String file) {
		return this.fileNameFilter.accept(null, file);
	}

	public abstract void minify(InputStream inputStream, OutputStream outputStream, Charset charset, String customHeader);

	public static Minifier forFileName(String filename) {
		for (Minifier minifier : values()) {
			if(minifier.accepts(filename)) {
				return minifier;
			}
		}
		return null;
	}
}
