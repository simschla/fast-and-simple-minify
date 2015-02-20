package ch.simschla.minify.ant;

import ch.simschla.minify.adapter.Minifier;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static ch.simschla.minify.streams.Streams.close;

public class MinifyAntTask extends Task {

	private final DelegatingCopy delegate;
	private final FileUtils fileUtils;

	private String header = "";
	private ConversionType type = ConversionType.auto;

	public MinifyAntTask() {
		delegate = createNewConfiguredDelegate();
		fileUtils = FileUtils.newFileUtils();
		setupDefaultValues();
	}

	private DelegatingCopy createNewConfiguredDelegate() {
		DelegatingCopy copy = new DelegatingCopy();
		copy.setFiltering(false);
		return copy;
	}

	private void setupDefaultValues() {
		this.setEncoding("UTF-8");
		this.setFailOnError(false);
		this.setFlatten(false);
		this.setOverwrite(false);
	}


	private void minify(String fromFile, List<String> toFiles) {

		Minifier minifierForFile = determineMinifier(fromFile);

		if (minifierForFile == null) {
			log("Cannot minify file " + fromFile + " - unsupported file type!", Project.MSG_DEBUG);
			return;
		}

		for (String toFile : toFiles) {
			minifyOneFile(minifierForFile, new File(fromFile), new File(toFile));
		}
	}

	private Minifier determineMinifier(String fromFile) {
		Minifier minifierForFile = null;
		if(type() == ConversionType.auto) {
			minifierForFile = Minifier.forFileName(fromFile);
		} else {
			minifierForFile = Minifier.forFileName("filename." + type().name());
		}
		return minifierForFile;
	}

	private void minifyOneFile(Minifier minifier, File fromFile, File toFile) {
		if(shouldSkipMinification(fromFile, toFile)) {
			return;
		}
		createDirectoriesIfNeeded(toFile);
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(fromFile);
			out = new FileOutputStream(toFile);
			minifier.minify(in, out, charset(), header());
		} catch (FileNotFoundException e) {
			log("Cannot minify file " + fromFile + " to " + toFile + " due to FileNotFoundException: " + e.getMessage(), Project.MSG_WARN);
		} finally {
			close(in);
			close(out);
		}
	}

	private void createDirectoriesIfNeeded(File toFile) {
		final File parentFile = toFile.getParentFile();
		if(parentFile != null && !parentFile.exists()) {
			parentFile.mkdirs();
		}
	}

	private boolean shouldSkipMinification(File fromFile, File toFile) {
		boolean shouldSkip = false;
		if(toFile.exists() && !overwrite()) {
			log("Skipping minification of file " + fromFile + " to " + toFile + ". " + toFile + " already exists and should not be overwritten.", Project.MSG_DEBUG);
			shouldSkip = true;
		}
		if(toFile.equals(fromFile)) {
			log("Skipping minifying file " + fromFile + " onto itself on " + toFile + ".", Project.MSG_VERBOSE);
			shouldSkip = true; // TODO (simon, 21.02.13): maybe use detour via temporary file to allow this?
		}
		return shouldSkip;
	}

	//---- properties

	public void setHeader(String header) {
		this.header = header;
	}

	private String header() {
		return this.header;
	}

	private Charset charset() {
		return Charset.forName(delegate.getEncoding());
	}

	public void setType(ConversionType type) {
		this.type = type;
	}

	private ConversionType type() {
		return this.type;
	}

	//---- delegation methods

	/**
	 * pass through -> will get back to us in {@link this.delegate#doFileOperations()}
	 * @throws BuildException when delegate throws one
	 */
	@Override
	public void execute() throws BuildException {
		delegate.execute();
	}


	// setters and getters for configuration via ant-task

	public void setFile(File file) {
		delegate.setFile(file);
	}

	public void setTofile(File destFile) {
		delegate.setTofile(destFile);
	}

	public void setTodir(File destDir) {
		delegate.setTodir(destDir);
	}

	public void setOverwrite(boolean overwrite) {
		delegate.setOverwrite(overwrite);
	}

	private boolean overwrite() {
		return delegate.overwrite();
	}

	public void setFlatten(boolean flatten) {
		delegate.setFlatten(flatten);
	}

	public void setFailOnError(boolean failonerror) {
		delegate.setFailOnError(failonerror);
	}

	public void setEncoding(String encoding) {
		delegate.setEncoding(encoding);
		delegate.setOutputEncoding(encoding);
	}

	public void addFileset(FileSet set) {
		delegate.addFileset(set);
	}

	public void add(FileNameMapper fileNameMapper) {
		delegate.add(fileNameMapper);
	}

	public Mapper createMapper() throws BuildException {
		return delegate.createMapper();
	}

	public void add(ResourceCollection res) {
		delegate.add(res);
	}

	public void setEnableMultipleMappings(boolean enableMultipleMappings) {
		delegate.setEnableMultipleMappings(enableMultipleMappings);
	}


	//---- override and delegate

	@Override
	public void setProject(Project project) {
		super.setProject(project);
		delegate.setProject(project);
	}

	@Override
	public void setOwningTarget(Target target) {
		super.setOwningTarget(target);
		delegate.setOwningTarget(target);
	}

	@Override
	public void setTaskName(String name) {
		super.setTaskName(name);
		delegate.setTaskName(name);
	}

	@Override
	public void setTaskType(String type) {
		super.setTaskType(type);
		delegate.setTaskType(type);
	}

	@Override
	public void setRuntimeConfigurableWrapper(RuntimeConfigurable wrapper) {
		super.setRuntimeConfigurableWrapper(wrapper);
		delegate.setRuntimeConfigurableWrapper(wrapper);
	}

	@Override
	public void setLocation(Location location) {
		super.setLocation(location);
		delegate.setLocation(location);
	}

	@Override
	public void setDescription(String desc) {
		super.setDescription(desc);
		delegate.setDescription(desc);
	}

	//---- enum to be used in type

	/**
	 * Enum for selecting conversion type. Enum values in lower case for easier reading in ant file.
	 */
	public static enum ConversionType {
		auto, css, js;
	}

	//---- our own version of the copy task to use as a delegator. This allows us to only use as much code as we really need.

	private final class DelegatingCopy extends Copy {

		@Override
		protected void doFileOperations() {
			for (Map.Entry<String, String[]> fileCopyMapEntry : fileCopyMap().entrySet()) {
				MinifyAntTask.this.minify(fileCopyMapEntry.getKey(), Arrays.asList(fileCopyMapEntry.getValue()));
			}
		}

		private Map<String, String[]> fileCopyMap() {
			@SuppressWarnings("unchecked")
			Map<String, String[]> fileCopyMap = this.fileCopyMap;
			return fileCopyMap;
		}

		private boolean overwrite() {
			return this.forceOverwrite;
		}

	}
}
