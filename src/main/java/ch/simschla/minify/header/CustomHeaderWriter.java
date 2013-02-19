package ch.simschla.minify.header;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static ch.simschla.minify.precondition.Preconditions.checkNotNull;

public final class CustomHeaderWriter {

	private final Charset charset;

	private final OutputStream outputStream;

	private final String commentLinePattern;

	private CustomHeaderWriter(Builder builder) {
		this.charset = builder.charset();
		this.outputStream = builder.outputStream();
		this.commentLinePattern = builder.commentLinePrefix() + "%s" + builder.commentLinePostfix() + '\n';
	}

	public void writeHeader(String header) {
		if(header.isEmpty()) {
			return; //bail out if we don't have anything to write
		}
		List<String> headerLines = splitToLines(header);
		writeHeaderLines(headerLines);
	}

	private void writeHeaderLines(List<String> headerLines) {
		for (String headerLine : headerLines) {
			writeHeaderLine(headerLine);
		}
	}

	private void writeHeaderLine(String headerLine) {
		String commentedHeaderLine = commentWithLineComment(headerLine);
		try {
			this.outputStream.write(commentedHeaderLine.getBytes(this.charset));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String commentWithLineComment(String headerLine) {
		return String.format(this.commentLinePattern, headerLine);
	}

	private List<String> splitToLines(String header) {
		String[] parts = header.split("\r?\n");
		return Arrays.asList(parts);
	}

	public static Builder builder() {
		return new Builder();
	}


	public static final class Builder {

		private Charset charset = Charset.forName("UTF-8");

		private OutputStream outputStream = System.out;

		private String commentLinePrefix = "// ";
		private String commentLinePostfix = "";

		public Charset charset() {
			return this.charset;
		}

		public OutputStream outputStream() {
			return this.outputStream;
		}

		public Builder charset(final Charset charset) {
			this.charset = checkNotNull(charset);
			return this;
		}

		public Builder outputStream(final OutputStream outputStream) {
			this.outputStream = checkNotNull(outputStream);
			return this;
		}

		public String commentLinePrefix() {
			return this.commentLinePrefix;
		}

		public String commentLinePostfix() {
			return this.commentLinePostfix;
		}

		public Builder commentLinePrefix(final String commentLinePrefix) {
			this.commentLinePrefix = checkNotNull(commentLinePrefix);
			return this;
		}

		public Builder commentLinePostfix(final String commentLinePostfix) {
			this.commentLinePostfix = checkNotNull(commentLinePostfix);
			return this;
		}


		public CustomHeaderWriter build() {
			return new CustomHeaderWriter(this);
		}


	}
}
