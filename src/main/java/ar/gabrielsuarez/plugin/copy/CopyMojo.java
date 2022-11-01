package ar.gabrielsuarez.plugin.copy;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "copy", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class CopyMojo extends AbstractMojo {

	@Parameter(property = "from", required = true)
	private String from;

	@Parameter(property = "to", required = true)
	private String to;

	public void execute() throws MojoExecutionException {
		try {
			getLog().info(String.format("from: %s, to: %s", from, to));

			Path from = Paths.get(this.from);
			Boolean fromExists = from.toFile().exists();
			Boolean fromIsDirectory = from.toFile().isDirectory();

			Path to = Paths.get(this.to);
			Boolean toExists = to.toFile().exists();
			Boolean toIsDirectory = to.toFile().isDirectory() || this.to.endsWith("/") || this.to.endsWith("\\");

			if (fromExists) {
				if (fromIsDirectory && (toIsDirectory || !toExists)) {
					copyDirectoryToDirectory(from, to);
				}
				if (!fromIsDirectory && toIsDirectory) {
					copyFileToDirectory(from, to);
				}
				if (!fromIsDirectory && !toIsDirectory) {
					copyFileToFile(from, to);
				}
			}
		} catch (Exception e) {
			throw new MojoExecutionException(e);
		}
	}

	public void copyDirectoryToDirectory(Path from, Path to) throws IOException {
		try (Stream<Path> stream = Files.walk(from)) {
			to.toFile().mkdirs();
			Iterator<Path> i = stream.iterator();
			while (i.hasNext()) {
				Path currentFrom = i.next();
				Path currentTo = to.resolve(from.relativize(currentFrom));
				try {
					Files.copy(currentFrom, currentTo, StandardCopyOption.REPLACE_EXISTING);
				} catch (DirectoryNotEmptyException e) {
				}
			}
		}
	}

	public void copyFileToDirectory(Path from, Path to) throws IOException {
		to.toFile().mkdirs();
		Path currentTo = to.resolve(from.getFileName());
		Files.copy(from, currentTo, StandardCopyOption.REPLACE_EXISTING);
	}

	public void copyFileToFile(Path from, Path to) throws IOException {
		File parent = to.toFile().getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
	}
}
