package ch.simschla.minify.header;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class CustomHeaderWriterTest {

	private static final String customPrefix = "<!-- ";
	private static final String customPostfix = " -->";

	private ByteArrayOutputStream headerTarget;
	private CustomHeaderWriter headerWriter;

	@Before
	public void setUp() throws Exception {
		headerTarget = new ByteArrayOutputStream();
		headerWriter = CustomHeaderWriter.builder().outputStream(headerTarget).commentLinePrefix(customPrefix).commentLinePostfix(customPostfix).build();
	}

	@Test
	public void anEmptyCommentShouldNotBeWritten() throws Exception {
		String written = write("");
		assertThat(written, is(""));
	}

	@Test
	public void aNonEmptyCommentIsWritten() throws Exception {
		String written = write("x");
		assertThat(written, containsString("x"));
	}

	@Test
	public void aSingleLineCommentIsEncapsulatedInPreAndPostfix() throws Exception {
		final String comment = "© by simschla";
		String written = write(comment);
		assertThat(written, startsWith(customPrefix));
		assertThat(written, endsWith(customPostfix + '\n'));
		assertThat(written, containsString(comment));
	}

	@Test
	public void everyLineOfAMultilineCommentIsPreAndPostfixed() throws Exception {
		final String comment = "first line\nsecond line\nthird line";
		final String written = write(comment);

		final String[] lines = written.split("\n");
		assertThat(lines.length, equalTo(3));
		for (String line : lines) {
			assertThat(line, startsWith(customPrefix));
			assertThat(line, endsWith(customPostfix));
		}
	}

	@Test
	public void windowsStyleLineBreaksAreTreatedLikeUnixStyleLineBreaks() throws Exception {
		final String mixedLineBreakComment = "windows break\r\nunix break\nlast line";
		final String written = write(mixedLineBreakComment);

		final String[] lines = written.split("\n");
		assertThat(lines.length, equalTo(3));
		for (String line : lines) {
			assertThat(line, startsWith(customPrefix));
			assertThat(line, endsWith(customPostfix));
		}
	}

	@Test
	public void outputDoesNotContainAnyWindowsStyleLineBreaks() throws Exception {
		final String windowsLineBreaks = "line 1\r\nline 2\r\nline 3\r\n";
		final String written = write(windowsLineBreaks);
		assertThat(written, not(containsString("\r")));
	}

	@Test
	public void singleLineCommentShouldEndWithNewLine() throws Exception {
		final String comment = "line 1";
		final String written = write(comment);
		assertThat(written, endsWith("\n"));
	}

	@Test
	public void multiLineCommentsShouldEndWithNewLine() throws Exception {
		final String comment = "line 1\nline 2";
		final String written = write(comment);
		assertThat(written, endsWith("\n"));

	}

	private String write(String stringToWrite) {
		headerWriter.writeHeader(stringToWrite);
		return headerTarget.toString();
	}
}
