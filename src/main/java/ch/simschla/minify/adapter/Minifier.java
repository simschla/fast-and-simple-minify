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
			CssMin.Builder builder = CssMin.builder();
			if(inputStream != null) {
				builder = builder.inputStream(inputStream);
			}
			if(outputStream != null) {
				builder = builder.outputStream(outputStream);
			}
			if(charset != null) {
				builder = builder.charset(charset);
			}
			if(customHeader != null) {
				builder = builder.customHeader(customHeader);
			}
			final CssMin cssMin = builder.build();
			cssMin.minify();
		}
	},

	JS("js", "_js", "bones", "jake", "jsfl", "jsm", "jss", "jsx", "pac", "sjs", "ssjs") {
		@Override
		public void minify(InputStream inputStream, OutputStream outputStream, Charset charset, String customHeader) {
			JsMin.Builder builder = JsMin.builder();
			if (inputStream != null) {
				builder = builder.inputStream(inputStream);
			}
			if (outputStream != null) {
				builder = builder.outputStream(outputStream);
			}
			if (charset != null) {
				builder = builder.charset(charset);
			}
			if (customHeader != null) {
				builder = builder.customHeader(customHeader);
			}
			final JsMin jsMin = builder.build();
			jsMin.minify();
		}
	},;

	private final FilenameFilter fileNameFilter;

	private Minifier(final String... acceptedFileTypes) {
		this.fileNameFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name == null) {
					return false;
				}
				for (String acceptedFileType : acceptedFileTypes) {
					if (name.endsWith("." + acceptedFileType)) {

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
