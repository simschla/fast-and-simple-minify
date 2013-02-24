package ch.simschla.minify.cli.parsing;

import java.util.*;

import static ch.simschla.minify.precondition.Preconditions.checkLessThan;
import static ch.simschla.minify.precondition.Preconditions.checkNotNull;

public class CliArguments {

	public static final String OPTION_PREFIX = "-";

	private final List<String> arguments = new ArrayList<String>(2);
	private final Map<String, String> options = new LinkedHashMap<String, String>(3);

	public CliArguments(String[] cliArgs) {
		this(Arrays.asList(cliArgs));
	}

	public CliArguments(List<String> cliArgs) {
		parseArgs(argListAsStack(cliArgs));
	}

	private Stack<String> argListAsStack(List<String> cliArgs) {
		Stack<String> argStack = new Stack<String>();
		List<String> argsReversed = reverse(cliArgs);

		for (String arg : argsReversed) {
			argStack.push(arg);
		}

		return argStack;
	}

	private List<String> reverse(List<String> cliArgs) {
		List<String> argsReversed = new ArrayList<String>(cliArgs);
		Collections.reverse(argsReversed);
		return argsReversed;
	}

	private void parseArgs(Stack<String> cliArgs) {

		while (!cliArgs.isEmpty()) {
			String argument = cliArgs.pop();

			if(isOption(argument)) {
				final String optionName = getOptionName(argument);
				final String optionValue = getOptionValue(cliArgs);
				this.options.put(optionName, optionValue);
			} else {
				this.arguments.add(argument);
			}
		}
	}

	private String getOptionValue(Stack<String> cliArgs) {
		if(cliArgs.isEmpty()) {
			return null;
		}
		return cliArgs.pop();
	}

	private boolean isOption(String argument) {
		return checkNotNull(argument).startsWith(OPTION_PREFIX);
	}

	private String getOptionName(String argument) {
		return checkNotNull(argument).substring(OPTION_PREFIX.length());
	}


	public String option(String optionName) {
		checkNotNull(optionName);
		return this.options.get(optionName);
	}

	public boolean hasOption(String optionName) {
		checkNotNull(optionName);
		return this.options.containsKey(optionName);
	}

	public int optionCount() {
		return this.options.size();
	}

	public int argumentCount() {
		return this.arguments.size();
	}

	public String argument(int index) {
		checkLessThan(index, argumentCount());
		return this.arguments.get(index);
	}
}
