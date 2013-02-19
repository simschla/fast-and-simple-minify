package ch.simschla.minify.css;

import ch.simschla.minify.header.CustomHeaderWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static ch.simschla.minify.precondition.Preconditions.checkNotNull;

/**
 * This is a java port of Ryan Day's cssmin utility.
 *
 * Currently implemented Version: <a href="https://github.com/soldair/cssmin/blob/95f998395f35ddccd7babd4256ce2d3dbc9f6fb5/cssmin.c">95f998395f35ddccd7babd4256ce2d3dbc9f6fb5</a>
 */
public final class CssMin {

	private static final int EOF = -1;

	private final InputStream inputStream;
	private final OutputStream outputStream;

	private final CustomHeaderWriter headerWriter;
	private final String customHeader;

	private static enum State {
		STATE_FREE, STATE_ATRULE, STATE_SELECTOR, STATE_BLOCK, STATE_DECLARATION, STATE_COMMENT;
	}

	private int theLookahead = EOF;

	private State tmp_state;

	private State state = State.STATE_FREE;

	private State last_state = State.STATE_FREE;

	private boolean in_paren = false;

	private CssMin(Builder builder) {
		this.inputStream = builder.inputStream();
		this.outputStream = builder.outputStream();
		this.customHeader = builder.customHeader();
		this.headerWriter =
				CustomHeaderWriter.builder()
						.charset(builder.charset())
						.outputStream(builder.outputStream())
						.commentLinePrefix("/* ")
						.commentLinePostfix(" */")
						.build();
	}

	public void minify() {
		try {
			writeCustomHeader();
			cssmin();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				outputStream.flush();
			} catch (IOException e) {
				//ignore silently
			}
		}
	}

	private void writeCustomHeader() {
		this.headerWriter.writeHeader(this.customHeader);
	}

	/* cssmin -- minify the css
	removes comments
	removes newlines and line feeds keeping
	removes last semicolon from last property
*/
	private void cssmin() throws IOException {
		for (;;) {
			int c = get();

			if (c == EOF) {
				return;
			}

			c = machine(c);

			if (c != 0) {
				outputStream.write(c);
			}
		}
	}

	/* get -- return the next character from stdin. Watch out for lookahead. If
the character is a control character, translate it to a space or
linefeed.
*/
	private int get() throws IOException {
		int c = theLookahead;
		theLookahead = EOF;
		if (c == EOF) {
			c = inputStream.read();
		}

		if (c >= ' ' || c == '\n' || c == EOF) {
			return c;
		}

		if (c == '\r') {
			return '\n';
		}

		return ' ';
	}

	/* peek -- get the next character without getting it.
*/

	private int peek() throws IOException {
		theLookahead = get();
		return theLookahead;
	}

	/* machine

*/
	private int machine(int c) throws IOException {

		if(state != State.STATE_COMMENT){
			if(c == '/' && peek() == '*'){
				tmp_state = state;
				state = State.STATE_COMMENT;
			}
		}

		switch (state){
			case STATE_FREE:
				if (c == ' ' && c == '\n' ) {
					c = 0;
				} else if (c == '@'){
					state = State.STATE_ATRULE;
					break;
				} else if(c > 0){
					//fprintf(stdout,"one to 3 - %c %i",c,c);
					state = State.STATE_SELECTOR;
				}
			case STATE_SELECTOR:
				if (c == '{') {
					state = State.STATE_BLOCK;
				} else if(c == '\n') {
					c = 0;
				} else if(c == '@'){
					state = State.STATE_ATRULE;
				} else if (c == ' ' && peek() == '{') {
					c = 0;
				}
				break;
			case STATE_ATRULE:
			/* support
				@import etc.
				@font-face{
			*/
				if (c == '\n' || c == ';') {
					c = ';';
					state = State.STATE_FREE;
				} else if(c == '{') {
					state = State.STATE_BLOCK;
				}
				break;
			case STATE_BLOCK:
				if (c == ' ' || c == '\n' ) {
					c = 0;
					break;
				} else if (c == '}') {
					state = State.STATE_FREE;
					//fprintf(stdout,"closing bracket found in block\n");
					break;
				} else {
					state = State.STATE_DECLARATION;
				}
			case STATE_DECLARATION:
				//support in paren because data can uris have ;
				if(c == '('){
					in_paren = true;
				}
				if(!in_paren){

					if( c == ';') {
						state = State.STATE_BLOCK;
						//could continue peeking through white space..
						if(peek() == '}'){
							c = 0;
						}
					} else if (c == '}') {
						//handle unterminated declaration
						state = State.STATE_FREE;
					} else if ( c == '\n') {
						//skip new lines
						c = 0;
					} else if (c == ' ' ) {
						//skip multiple spaces after each other
						if( peek() == c ) {
							c = 0;
						}
					}

				} else if (c == ')') {
					in_paren = false;
				}

				break;
			case STATE_COMMENT:
				if(c == '*' && peek() == '/'){
					theLookahead = EOF;
					state = tmp_state;
				}
				c = 0;
				break;
		}

		return c;
	}

	public static Builder builder() {
		return new Builder();
	}

	//--- inner classes

	public static final class Builder {
		private InputStream inputStream = System.in;
		private OutputStream outputStream = System.out;

		private String customHeader = "";

		private Charset charset = Charset.forName("UTF-8");

		public InputStream inputStream() {
			return this.inputStream;
		}

		public OutputStream outputStream() {
			return this.outputStream;
		}

		public Builder inputStream(final InputStream inputStream) {
			this.inputStream = checkNotNull(inputStream);
			return this;
		}

		public Builder outputStream(final OutputStream outputStream) {
			this.outputStream = checkNotNull(outputStream);
			return this;
		}

		public String customHeader() {
			return this.customHeader;
		}

		public Builder customHeader(final String customHeader) {
			this.customHeader = checkNotNull(customHeader);
			return this;
		}

		public Charset charset() {
			return this.charset;
		}

		public Builder charset(final Charset charset) {
			this.charset = checkNotNull(charset);
			return this;
		}


		public CssMin build() {
			return new CssMin(this);
		}
	}
}
