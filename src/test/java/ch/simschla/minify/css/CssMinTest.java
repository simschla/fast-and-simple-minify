package ch.simschla.minify.css;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CssMinTest {

	@Test
	public void selectorShouldPreserveSpaces() throws Exception {
		final String selectorWithSpace = "a b";
		assertThatCss(selectorWithSpace).isMinifiedTo(selectorWithSpace);
	}

	@Test
	public void keywordDeclarationWithoutSemicolonGetsSemicolon() throws Exception {
		assertThatCss("@import lala\n").isMinifiedTo("@import lala;");
	}

	@Test
	public void commentsShouldBeEaten() throws Exception {
		assertThatCss("/*i am a comment*/").isMinifiedTo("");
	}

	@Test
	public void singleSpacesBeforeBlocksShouldBeRemoved() throws Exception {
		assertThatCss("LALAL {} LALAL  {}").isMinifiedTo("LALAL{} LALAL {}");
	}

	@Test
	public void spacesShouldBeStrippedFromBetweenDeclarations() throws Exception {
		assertThatCss("body {line-height:1.5; background-color:#fff}").isMinifiedTo("body{line-height:1.5;background-color:#fff}");
	}

	@Test
	public void closingSemicolonsShouldBeStrippedIfAtTheImmediateEndOfABlock() throws Exception {
		assertThatCss("{background-color:#fff;}").isMinifiedTo("{background-color:#fff}");
	}

	@Test
	public void semicolonsShouldNotBePlacedOutsideOfBlockGivenNoSemicolonAtEndOfDeclarationAtEndOfBlock() throws Exception {
		assertThatCss("body {font-family:\"Trebuchet MS\",Helvetica,sans-serif}").isMinifiedTo("body{font-family:\"Trebuchet MS\",Helvetica,sans-serif}");
	}

	@Test
	public void multiLineDeclarationsShouldNotBeLost() throws Exception {
		final String multilineDeclarations = "body {\n" +
				"    line-height:1.5;\n" +
				"    background: transparent\n" +
				"        url(images/newwiz_back.gif.xhtml) repeat-x\n" +
				"        0 0;\n" +
				"   \tfont-family:\"Trebuchet MS\",Helvetica,sans-serif;\n" +
				"}\n";
		String expectedMinifiedText = "body{line-height:1.5;background: transparent url(images/newwiz_back.gif.xhtml) repeat-x 0 0;font-family:\"Trebuchet MS\",Helvetica,sans-serif;}";
		assertThatCss(multilineDeclarations).isMinifiedTo(expectedMinifiedText);
	}

	private CssMinTestAssert assertThatCss(String cssToMinify) {
		return new CssMinTestAssert(cssToMinify);
	}

	private static class CssMinTestAssert {

		private final String cssToMinify;

		private String actuallyMinifiedText;

		private String expectedMinifiedText;

		public CssMinTestAssert(String cssToMinify) {
			this.cssToMinify = cssToMinify;
		}


		public void isMinifiedTo(String expectedMinifiedText) {
			this.expectedMinifiedText = expectedMinifiedText;
			minify();
			assertThat(actuallyMinifiedText, equalTo(this.expectedMinifiedText));
		}

		private void minify() {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			CssMin.builder()
					.inputStream(new ByteArrayInputStream(cssToMinify.getBytes()))
					.outputStream(byteArrayOutputStream)
					.build()
					.minify();
			this.actuallyMinifiedText = byteArrayOutputStream.toString();
		}
	}
}
