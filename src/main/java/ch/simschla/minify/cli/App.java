package ch.simschla.minify.cli;

import ch.simschla.minify.adapter.Minifier;
import ch.simschla.minify.cli.parsing.CliArguments;
import ch.simschla.minify.streams.Streams;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static ch.simschla.minify.precondition.Preconditions.checkNotNull;

/**
 * Hello world!
 */
public class App {

	private final Format format;

	private final File fileIn;

	private final File fileOut;

	private final String comment;

	private final Charset commentCharset;

	public static void main(String[] args) {
		CliArguments cliArguments = new CliArguments(args);
		if (wantsHelp(cliArguments)) {
			printUsage();
			System.exit(0);
		}
		try {
			App app = new App(cliArguments);
			app.executeMinify();
		} catch (InvalidCliArgumentException e) {
			printUsage();
			System.exit(1);
		}
	}

	private static boolean wantsHelp(CliArguments args) {
		return args.optionCount() == 1 && args.hasOption("h");
	}

	private static void printUsage() {
		String baseName = "java -jar XXX.jar";
		String params = "[-format FORMAT] [-encoding ENCODING] [-comment COMMENT] [-out OUT_FILE] [IN_FILE]";
		String paramExplanations = new StringBuilder("where:")
				.append('\n')
				.append("FORMAT is one of {auto, css, js} (default: auto)")
				.append('\n')
				.append("ENCODING is a charset to be used to write the comment in (default: UTF-8)")
				.append('\n')
				.append("COMMENT is a string that will be added on start of all minified files (default: empty)")
				.append('\n')
				.append("OUT_FILE is the Filename to write the minified content to (default: write to System.out)")
				.append('\n')
				.append("IN_FILE is the Filename to read and minify (default: read from System.in)")
				.toString();
		System.out.println("Usage: " + baseName + " " + params + "\n" + paramExplanations);
	}



	protected App(CliArguments arguments) {
		this.format = readOptionFormat(arguments);
		this.fileIn = readParamFileIn(arguments);
		this.fileOut = readOptionFileOut(arguments);
		this.comment = readOptionComment(arguments);
		this.commentCharset = readOptionEncoding(arguments);
		checkParamState();
	}

	private Charset readOptionEncoding(CliArguments arguments) {
		if(arguments.hasOption("encoding")) {
			return Charset.forName(arguments.option("encoding"));
		}
		return null;
	}

	private String readOptionComment(CliArguments arguments) {
		if(arguments.hasOption("comment")) {
			return arguments.option("comment");
		}
		return null;
	}

	private Format readOptionFormat(CliArguments arguments) {
		Format format = Format.AUTO;
		if(arguments.hasOption("format")) {
			format = Format.valueOf(arguments.option("format").toUpperCase());
		}
		return format;
	}

	private File readParamFileIn(CliArguments arguments) {
		if(arguments.argumentCount() > 0) {
			//first argument is in-file
			return new File(arguments.argument(0));
		}
		return null; //no in-file
	}

	private File readOptionFileOut(CliArguments arguments) {
		if(arguments.hasOption("out")) {
			return new File(arguments.option("out"));
		}
		return null;  // no out-file
	}

	private void checkParamState() throws InvalidCliArgumentException {
		if(this.format == Format.AUTO) {
			if(this.fileIn == null && this.fileOut == null) {
				throw new InvalidCliArgumentException("format", "Cannot automatically determine format due to no input and output file name available.");
			}

			Minifier inMinifier = null;
			if(this.fileIn != null) {
				inMinifier = minifierForFile(this.fileIn);
			}
			Minifier outMinifier = null;
			if(this.fileOut != null) {
				outMinifier = minifierForFile(this.fileOut);
			}
			if(inMinifier == null && outMinifier == null) {
				throw new InvalidCliArgumentException("format", "Cannot automatically determine format. Please specify.");
			}
			if(inMinifier != null && outMinifier != null && inMinifier != outMinifier) {
				throw new InvalidCliArgumentException("format", "Ambigous determination of minifying format. In- and outfile names do contradict.");
			}
		}
	}

	private void executeMinify() {

		Minifier minifier = findMinifier();

		InputStream inStream = null;
		OutputStream outStream = null;
		try {
			inStream = this.fileIn != null ? Streams.fileInputStream(this.fileIn) : null;
			outStream = this.fileOut != null ? Streams.fileOutputStream(this.fileOut) : null;

			minifier.minify(inStream, outStream, this.commentCharset, this.comment);
		} finally {
			Streams.close(inStream);
			Streams.close(outStream);
		}
	}

	private Minifier findMinifier() {
		if(this.format == Format.AUTO) {
			if(this.fileIn != null) {
				return minifierForFile(this.fileIn);
			}
			if(this.fileOut != null) {
				return minifierForFile(this.fileOut);
			}
			throw new IllegalStateException("Should determine auto format for a state where this is not possible");
		} else {
			return minifierForFormat(this.format);
		}
	}

	private Minifier minifierForFormat(Format format) {
		return Minifier.forFileName("file." + format.name().toLowerCase());
	}

	private Minifier minifierForFile(File file) {
		checkNotNull(file);
		return Minifier.forFileName(file.getName());
	}

	private static class InvalidCliArgumentException extends RuntimeException {

		private InvalidCliArgumentException(String paramName, String message) {
			super(createMessage(paramName, message));
		}

		private static String createMessage(String paramName, String message) {
			return "Parameter error: " + paramName + ": " + message;
		}
	}

	private enum Format {
		AUTO, CSS, JS;
	}
}
