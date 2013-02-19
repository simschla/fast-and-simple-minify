package ch.simschla.minify.js;

import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class JsMinTest {


	@Test
	public void blockCommentsAreRemoved() throws Exception {
		assertThatjs("/* remove me */\nvar a=3;")
				.does(not(containsString("remove me")))
				.does(not(containsString("/*")))
				.does(not(containsString("*/")));

	}

	@Test
	public void lineCommentsAreRemoved() throws Exception {
		final String lineCommentCode = "var a=3;\n" +
				"// remove me\n" +
				"var b=3;";

		assertThatjs(lineCommentCode)
				.does(not(containsString("remove me")))
				.does(not(containsString("//")))
				.does(containsString("var a=3;"))
				.does(containsString("var b=3;"));

	}

	@Test
	public void simpleStatementsAreKept() throws Exception {
		String simpleStatement = "var a=3;";
		assertThatjs(simpleStatement).isMinifiedTo(simpleStatement);
	}

	@Test
	public void linebreaksAreRemoved() throws Exception {
		String multilineStatements = "var a=3;\n\nvar b=3;\n\nvar c=3;";
		assertThatjs(multilineStatements).isMinifiedTo("var a=3;var b=3;var c=3;");
	}

	@Test
	public void multipleSpacesAreRemoved() throws Exception {
		String multiSpaceStatement = "var    a    =3;";
		assertThatjs(multiSpaceStatement).isMinifiedTo("var a=3;");
	}

	@Test
	public void functionBlocksAreOnlined() throws Exception {
		String function = "function() {\n" +
				"var a=3;\n" +
				"}";
		assertThatjs(function).isMinifiedTo("function(){var a=3;}");
	}

	@Test
	public void unterminatedBlockCommentsResultInError() throws Exception {
		String unterminated = "/* comment but not correctly terminated /";
		assertThatjs(unterminated).failsMinification();
	}

	@Test
	public void windowsLineBreaksAreRemoved() throws Exception {
		String windowsBreaks = "var a=3;\r\nvar b=3;";
		assertThatjs(windowsBreaks).does(not(containsString("\r")));
	}

	@Test
	public void tabsAreRemoved() throws Exception {
		String tabs = "\t\tvar a=3;\n\t\tvar b=3;";
		assertThatjs(tabs).does(not(containsString("\t")));
	}

	@Test
	public void doubleQuotedStringsAreUntouched() throws Exception {
		String stringWithSpaces = "string   with  spaces";
		String doubleQuotedStringAssignment = "var a=\"" + stringWithSpaces + "\";";
		assertThatjs(doubleQuotedStringAssignment).isMinifiedTo(doubleQuotedStringAssignment);
	}

	@Test
	public void singleQuotedStringsAreUntouched() throws Exception {
		String stringWithSpaces = "string   with  spaces";
		String singleQuotedStringAssignment = "var a='" + stringWithSpaces + "';";
		assertThatjs(singleQuotedStringAssignment).isMinifiedTo(singleQuotedStringAssignment);
	}

	@Test
	public void unterminatedStringLiteralsLeadToError() throws Exception {
		assertThatjs("var a=\"unterminated string literal;").failsMinification();
	}

	@Test
	public void quotedStringLiteralsMayContainControlCharacters() throws Exception {
		assertThatjs("var a=\"line 1\\line 2\"").isMinifiedTo("var a=\"line 1\\line 2\"");
	}

	@Test
	public void blocksStayBlocks() throws Exception {
		String block = "{var a=3;}";
		assertThatjs(block).isMinifiedTo(block);
	}

	@Test
	public void operationsAreUnharmed() throws Exception {
		String operation = "var a=b+\"string 2\"";
		assertThatjs(operation).isMinifiedTo(operation);
	}

	@Test
	public void regexLiteralsAreUnharmed() throws Exception {
		String regex = "var re=/ab+c/;";
		assertThatjs(regex).isMinifiedTo(regex);
	}

	@Test
	public void regexLiteralsWithSetsAreUnharmed() throws Exception {
		String regex = "var re=/ab+c[a-zA-Z\\r\\n\\t]/;";
		assertThatjs(regex).isMinifiedTo(regex);
	}

	@Test
	public void regexLiteralsWithUnterminatedSetsFailToMinimize() throws Exception {
		String regex = "var re=/ab+c[a-zA-Z\\r\\n\\t/;";
		assertThatjs(regex).failsMinification();
	}

	@Test
	public void regexLiteralsContainingSlashesFailToMinimize() throws Exception {
		String regex = "var re=/ab+c//;";
		assertThatjs(regex).failsMinification();
	}

	@Test
	public void regexLiteralsWithoutTerminationFailToMinimize() throws Exception {
		String regex = "var re=/ab+c\\;";
		assertThatjs(regex).failsMinification();
	}

	private JsMinTestAssert assertThatjs(String jsToMinify) {
		return new JsMinTestAssert(jsToMinify);
	}

	private static class JsMinTestAssert {

		private final String jsToMinify;

		private String actuallyMinifiedText;

		private String expectedMinifiedText;

		boolean minified = false;

		public JsMinTestAssert(String jsToMinify) {
			this.jsToMinify = jsToMinify;
		}


		public JsMinTestAssert isMinifiedTo(String expectedMinifiedText) {
			this.expectedMinifiedText = expectedMinifiedText;
			minify();
			assertThat(actuallyMinifiedText, equalTo('\n'+this.expectedMinifiedText));
			return this;
		}

		public JsMinTestAssert does(Matcher<String> matcher) {
			minify();
			assertThat(actuallyMinifiedText, matcher);
			return this;
		}

		private void minify() {
			try {
				if (minified) {
					return; //only once
				}
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				JsMin.builder()
						.inputStream(new ByteArrayInputStream(jsToMinify.getBytes()))
						.outputStream(byteArrayOutputStream)
						.build()
						.minify();
				this.actuallyMinifiedText = byteArrayOutputStream.toString();
			} finally {
				minified = true;
			}
		}

		public JsMin.JsMinException failsMinification() {
			if(minified) {
				throw new IllegalStateException("repeatedMinificationCalls for testing failing is not supported");
			}
			try {
				minify();
				fail("expected minification to fail but went through!");
			} catch (JsMin.JsMinException e) {
				//expected
				return e;
			}
			throw new IllegalStateException("should never be reached");
		}
	}
}
