package io.github.cowwoc.styler.benchmarks.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Manages sample files and real-world project downloads for benchmarks.
 * <p>
 * This manager caches downloaded project sources in {@code ~/.styler/benchmark-cache/} with a 30-day
 * expiration. Extracted Java files are cached locally to minimize repeated downloads and network
 * overhead during benchmark runs.
 */
public class BenchmarkResourceManager
{
	private static final Path CACHE_DIR = Paths.get(System.getProperty("user.home"), ".styler", "benchmark-cache");
	private static final long CACHE_VALIDITY_MS = 30L * 24 * 60 * 60 * 1000;
	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

	/**
	 * Real-world projects available for benchmarking.
	 */
	public enum Project
	{
		/**
		 * Spring Framework source code.
		 */
		SPRING_FRAMEWORK("spring-framework", "org.springframework", "spring-core", "6.1.0"),

		/**
		 * Google Guava utilities library.
		 */
		GUAVA("guava", "com.google.guava", "guava", "33.0.0-jre"),

		/**
		 * JUnit 5 testing framework.
		 */
		JUNIT5("junit5", "org.junit.jupiter", "junit-jupiter-engine", "5.10.0");

		private final String displayName;
		private final String groupId;
		private final String artifactId;
		private final String version;

		Project(String displayName, String groupId, String artifactId, String version)
		{
			this.displayName = displayName;
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
		}

		public String getDisplayName()
		{
			return displayName;
		}

		public String getGroupId()
		{
			return groupId;
		}

		public String getArtifactId()
		{
			return artifactId;
		}

		public String getVersion()
		{
			return version;
		}
	}

	/**
	 * Retrieves Java source files for a project, downloading and caching as needed.
	 *
	 * @param projectName name of the project (spring-framework, guava, or junit5)
	 * @return list of Java file paths
	 * @throws IllegalArgumentException if {@code projectName} is unknown
	 * @throws IOException              if download or extraction fails
	 * @throws InterruptedException     if the download is interrupted
	 */
	public static List<Path> getProjectFiles(String projectName) throws IOException, InterruptedException
	{
		requireThat(projectName, "projectName").isNotEmpty();

		Project project = parseProjectName(projectName);
		if (project == null)
		{
			throw new IllegalArgumentException("Unknown project: " + projectName +
				". Supported projects: spring-framework, guava, junit5");
		}

		return getOrDownloadProject(project);
	}

	/**
	 * Downloads a project from Maven Central if not cached.
	 *
	 * @param project the project to download
	 * @return list of extracted Java source file paths
	 */
	private static List<Path> getOrDownloadProject(Project project) throws IOException, InterruptedException
	{
		Path projectDir = CACHE_DIR.resolve(project.getDisplayName());
		Path cacheMetaFile = projectDir.resolve(".cache-time");

		// Check if cache is valid
		if (isCacheValid(projectDir, cacheMetaFile))
		{
			return getProjectSourceFiles(projectDir);
		}

		// Download and extract
		Files.createDirectories(projectDir);
		downloadAndExtractProject(project, projectDir);
		updateCacheMetadata(cacheMetaFile);

		return getProjectSourceFiles(projectDir);
	}

	private static boolean isCacheValid(Path projectDir, Path cacheMetaFile)
	{
		if (!Files.exists(projectDir) || !Files.exists(cacheMetaFile))
		{
			return false;
		}

		try
		{
			long lastModified = Files.getLastModifiedTime(cacheMetaFile).toMillis();
			long now = System.currentTimeMillis();
			return (now - lastModified) < CACHE_VALIDITY_MS;
		}
		catch (IOException _)
		{
			return false;
		}
	}

	private static void downloadAndExtractProject(Project project, Path targetDir)
		throws IOException, InterruptedException
	{
		String mavenCentralUrl = buildMavenCentralUrl(project);
		HttpRequest request = HttpRequest.newBuilder(URI.create(mavenCentralUrl)).
			GET().
			build();

		HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());

		if (response.statusCode() != 200)
		{
			throw new IOException("Failed to download " + project.getDisplayName() + ": HTTP " + response.statusCode());
		}

		extractZip(response.body(), targetDir);
	}

	private static String buildMavenCentralUrl(Project project)
	{
		return String.format("https://repo1.maven.org/maven2/%s/%s/%s/%s-sources.jar",
			project.getGroupId().replace(".", "/"),
			project.getArtifactId(),
			project.getVersion(),
			project.getArtifactId() + "-" + project.getVersion());
	}

	private static void extractZip(byte[] zipData, Path targetDir) throws IOException
	{
		try (ZipInputStream zis = new ZipInputStream(new java.io.ByteArrayInputStream(zipData)))
		{
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null)
			{
				if (entry.isDirectory())
					continue;

				if (!entry.getName().endsWith(".java"))
					continue;

				Path file = targetDir.resolve(entry.getName());
				Files.createDirectories(file.getParent());

				try (FileOutputStream fos = new FileOutputStream(file.toFile()))
				{
					zis.transferTo(fos);
				}
			}
		}
	}

	private static List<Path> getProjectSourceFiles(Path projectDir) throws IOException
	{
		List<Path> files = new ArrayList<>();
		Files.walk(projectDir).
			filter(Files::isRegularFile).
			filter(p -> p.toString().endsWith(".java")).
			forEach(files::add);
		return files;
	}

	private static void updateCacheMetadata(Path cacheMetaFile) throws IOException
	{
		Files.writeString(cacheMetaFile, Instant.now().toString());
	}

	private static Project parseProjectName(String projectName)
	{
		return switch (projectName.toLowerCase())
		{
			case "spring-framework", "spring" -> Project.SPRING_FRAMEWORK;
			case "guava" -> Project.GUAVA;
			case "junit5", "junit" -> Project.JUNIT5;
			default -> null;
		};
	}
}
