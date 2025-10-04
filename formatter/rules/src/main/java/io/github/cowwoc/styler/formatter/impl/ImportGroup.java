package io.github.cowwoc.styler.formatter.impl;

import java.util.List;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Represents a group of imports that share common characteristics.
 * <p>
 * Import groups are used to organize imports according to configuration rules.
 * Each group has a name (e.g., "java", "third-party", "project") and contains
 * a list of imports that belong to that group.
 * <p>
 * Groups maintain the sort order of their imports as determined by the grouping
 * and sorting logic.
 * <p>
 * <b>Immutability:</b> This class is immutable once constructed.
 *
 * @since {@code 1}.{@code 0}.{@code 0}
 * @author Import Organizer Team
 */
public final class ImportGroup
{
	private final String name;
	private final List<ImportAnalyzer.ImportInfo> imports;

	/**
	 * Creates an import group with the specified name and imports.
	 *
	 * @param name the group name (e.g., "java", "third-party", "static")
	 * @param imports the imports in this group, in sorted order
	 * @throws NullPointerException if name or imports is {@code null}
	 */
	public ImportGroup(String name, List<ImportAnalyzer.ImportInfo> imports)
	{
		requireThat(name, "name").isNotNull();
		requireThat(imports, "imports").isNotNull();

		this.name = name;
		this.imports = List.copyOf(imports); // Defensive copy for immutability
	}

	/**
	 * Returns the name of this import group.
	 *
	 * @return the group name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the imports in this group.
	 *
	 * @return an immutable list of imports in sorted order
	 */
	public List<ImportAnalyzer.ImportInfo> getImports()
	{
		return imports;
	}

	/**
	 * Checks if this group is empty.
	 *
	 * @return {@code true} if this group has no imports
	 */
	public boolean isEmpty()
	{
		return imports.isEmpty();
	}

	/**
	 * Returns the number of imports in this group.
	 *
	 * @return the size of this group
	 */
	public int size()
	{
		return imports.size();
	}

	@Override
	public String toString()
	{
		return "ImportGroup{name='" + name + "', count=" + imports.size() + "}";
	}
}
